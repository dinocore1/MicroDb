package com.devsmart.microdb;


import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MicroDB {

    private static final Logger logger = LoggerFactory.getLogger(MicroDB.class);

    private Driver mDriver;
    private int mSchemaVersion;
    private DBCallback mCallback;
    private final HashMap<UBValue, SoftReference<DBObject>> mLiveObjects = new HashMap<UBValue, SoftReference<DBObject>>();
    private final Set<UBValue> mDeletedObjects = new HashSet<UBValue>();
    private final Queue<WriteCommand> mSaveQueue = new ConcurrentLinkedQueue<WriteCommand>();
    private final ScheduledExecutorService mWriteThread = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "MicroDB Write Thread");
        }
    });
    private AtomicBoolean mAutoSave = new AtomicBoolean(true);
    private Set<WeakReference<DBIterator>> mIterators = new HashSet<WeakReference<DBIterator>>();
    private ReferenceQueue<DBIterator> mItQueue = new ReferenceQueue<DBIterator>();


    private interface WriteCommand {
        void write() throws IOException;
    }

    private class SaveDBData implements WriteCommand {

        private final UBObject mData;

        SaveDBData(UBObject obj) {
            mData = obj;
        }

        @Override
        public void write() throws IOException {
            mDriver.beginTransaction();

            if(mData.containsKey("id")) {
                mDriver.delete(mData.get("id"));
            }
            mDriver.insert(mData);

            mDriver.commitTransaction();
        }
    }

    private class SaveObject implements WriteCommand {

        private final DBObject mObject;

        SaveObject(DBObject object) {
            mObject = object;
        }

        @Override
        public void write() throws IOException {
            UBObject data = new UBObject();
            mObject.writeUBObject(data);

            mDriver.beginTransaction();

            if(data.containsKey("id")) {
                mDriver.delete(data.get("id"));
            }
            mDriver.insert(data);

            mDriver.commitTransaction();
            mObject.mDirty = false;
        }
    }

    private class DeleteObject implements WriteCommand {

        private final DBObject mObject;

        DeleteObject(DBObject obj) {
            mObject = obj;
        }

        @Override
        public void write() throws IOException {
            final UBValue id = mObject.getId();
            mObject.mDirty = false;
            mDriver.delete(id);
            synchronized (MicroDB.this) {
                mDeletedObjects.remove(id);
            }
        }
    }

    private Future<?> processWriteQueue() {
        return mWriteThread.submit(mSave);
    }

    private final Runnable mSave = new Runnable() {
        @Override
        public void run() {
            int numCommands = 0;
            WriteCommand cmd;
            while ((cmd = mSaveQueue.poll()) != null) {
                try {
                    cmd.write();

                    if(++numCommands == 100) {
                        logger.info("executed {} commands", numCommands);
                        numCommands = 0;
                    }
                } catch (Throwable t) {
                    logger.error("error writing to db", t);
                }
            }
            if(numCommands > 0) {
                logger.info("executed {} commands", numCommands);
            }
        }
    };

    MicroDB(Driver driver, int schemaVersion, DBCallback cb) throws IOException {
        mDriver = driver;
        mSchemaVersion = schemaVersion;
        mCallback = cb;

        mWriteThread.scheduleWithFixedDelay(mSave, 2, 2, TimeUnit.SECONDS);
        init();
    }

    private static final String METAKEY = "dbmeta";
    private static final String METAKEY_DBVERSION = "schema_version";

    private void init() throws IOException {

        UBValue key = UBValueFactory.createString(METAKEY);

        UBValue storedValue = mDriver.get(key);
        if(storedValue == null) {
            UBObject metaObj = new UBObject();
            metaObj.put("id", key);
            metaObj.put(METAKEY_DBVERSION, UBValueFactory.createInt(mSchemaVersion));
            mSaveQueue.offer(new SaveDBData(metaObj));
            mDriver.addIndex("type", "if(obj.type != null) { emit(obj.type) }");
            mCallback.onUpgrade(this, -1, mSchemaVersion);

        } else {
            UBObject metaObj = storedValue.asObject();
            int currentVersion = metaObj.get(METAKEY_DBVERSION).asInt();
            if(currentVersion < mSchemaVersion) {
                mCallback.onUpgrade(this, currentVersion, mSchemaVersion);
                metaObj.put(METAKEY_DBVERSION, UBValueFactory.createInt(mSchemaVersion));
                mSaveQueue.offer(new SaveDBData(metaObj));
            }
        }

    }



    /**
     * called when a dbobject is being finalized by the GC
     * @param obj
     */
    protected void finalizing(DBObject obj) {
        if(mAutoSave.get() && obj.mDirty){
            mSaveQueue.offer(new SaveObject(obj));
        }
        synchronized (this) {
            mLiveObjects.remove(obj.getId());
        }

    }

    public synchronized void close() throws IOException {
        flush();
        closeAllIterators();
        mLiveObjects.clear();
        mDriver.close();
    }

    /**
     * Saves all DBObjects that are marked dirty
     * @throws IOException
     */
    public void flush() throws IOException {
        synchronized (this) {
            for (SoftReference<DBObject> ref : mLiveObjects.values()) {
                DBObject obj = ref.get();
                if (obj != null && obj.mDirty) {
                    mSaveQueue.offer(new SaveObject(obj));
                }
            }
        }
        sync();
    }

    /**
     * create a new object of type {@code classType}.
     * @param classType
     * @param <T>
     * @return newly created object
     */
    public synchronized <T extends DBObject> T create(Class<T> classType) {
        try {
            if(!classType.getSimpleName().endsWith("_pxy")) {
                String proxyClassName = String.format("%s.%s_pxy", DBObject.class.getPackage().getName(), classType.getSimpleName());
                classType = (Class<T>) Class.forName(proxyClassName);
            }

            T retval = classType.newInstance();

            UBObject data = new UBObject();
            //UBValue key = mDriver.insert(data);
            UBValue key = UBValueFactory.createString(UUID.randomUUID().toString());
            data.put("id", key);
            retval.init(data, this);

            mLiveObjects.put(key, new SoftReference<DBObject>(retval));

            return retval;

        } catch (Exception e) {
            throw new RuntimeException("", e);
        }
    }

    /**
     * fetch and load database object with primary key {@code id}.
     * @param id
     * @param classType they type of database object with {@code id}
     * @param <T>
     * @return dbobject
     */
    public synchronized <T extends DBObject> T get(UBValue id, Class<T> classType) {
        if(mDeletedObjects.contains(id)) {
            return null;
        }
        try {

            T retval;
            DBObject cached;

            SoftReference<DBObject> ref = mLiveObjects.get(id);
            if(ref != null && (cached = ref.get()) != null){
                retval = (T)cached;
            } else {

                if(!classType.getSimpleName().endsWith("_pxy")) {
                    String proxyClassName = String.format("%s.%s_pxy", DBObject.class.getPackage().getName(), classType.getSimpleName());
                    classType = (Class<T>) Class.forName(proxyClassName);
                }

                UBValue data = mDriver.get(id);
                if(data == null) {
                    return null;
                } else {

                    if(!data.isObject()) {
                        throw new RuntimeException("database entry with id: " + id + " is not an object");
                    }
                    T newObj = classType.newInstance();
                    newObj.init(data.asObject(), this);
                    retval = newObj;
                    mLiveObjects.put(id, new SoftReference<DBObject>(retval));
                }
            }

            return retval;
        } catch (Exception e){
            throw new RuntimeException("", e);
        }
    }

    /**
     * saves/updates {@code obj} to the database. This method is not normally necessary for users to call
     * because database objects will automatically be saved when the garbage collector collects them if
     * they are marked dirty.
     * @param obj the data to be saved
     */
    public synchronized void save(DBObject obj) {
        mSaveQueue.offer(new SaveObject(obj));
    }

    public synchronized void delete(DBObject obj) {
        mDeletedObjects.add(obj.getId());
        mSaveQueue.offer(new DeleteObject(obj));
        mLiveObjects.remove(obj.getId());
    }

    /**
     * This method blocks until all queued write operation are completed.
     */
    protected void sync() {
        try {
            processWriteQueue().get();
        } catch (Exception e) {
            logger.error("sync operation ended with exception", e);
        }
    }

    private void processDeadIterators() {
        Reference<? extends DBIterator> ref;
        while((ref = mItQueue.poll()) != null) {
            mIterators.remove(ref);
            ref.clear();
        }
    }

    private synchronized void closeAllIterators() {
        processDeadIterators();
        Iterator<WeakReference<DBIterator>> itit = mIterators.iterator();
        while(itit.hasNext()) {
            WeakReference<DBIterator> ref = itit.next();
            DBIterator it = ref.get();
            if(it != null) {
                try {
                    it.close();
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
            itit.remove();
        }
    }

    public boolean addIndex(String indexName, String indexScript) throws IOException {
        return mDriver.addIndex(indexName, indexScript);
    }

    public synchronized DBIterator queryIndex(String indexName) throws IOException {
        processDeadIterators();

        DBIterator iterator = mDriver.queryIndex(indexName);
        if(iterator != null) {
            mIterators.add(new WeakReference<DBIterator>(iterator, mItQueue));
        }

        return iterator;
    }

    public synchronized <T extends DBObject> T getOne(Class<T> classType, String indexName, String value) throws IOException {
        return getOne(classType, indexName, UBValueFactory.createString(value));
    }

    public synchronized <T extends DBObject> T getOne(Class<T> classType, String indexName, UBValue key) throws IOException {
        DBIterator it = queryIndex(indexName);
        if(it == null) {
            logger.warn("no index named: '{}'", indexName);
            return null;
        }
        try {
            it.seekTo(key);
            if (it.valid() && it.getKey().equals(key)) {
                UBValue id = it.getPrimaryKey();
                return get(id, classType);
            } else {
                return null;
            }
        } finally {
            it.close();
        }
    }

    public <T extends DBObject> ObjIterator<T> getAll(Class<T> classType) throws IOException {
        return new ObjIterator<T>(queryIndex("type"), this, classType);
    }
}
