package com.devsmart.microdb;


import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.HashSet;
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
    private final HashMap<UUID, SoftReference<DBObject>> mLiveObjects = new HashMap<UUID, SoftReference<DBObject>>();
    private final Set<UUID> mDeletedObjects = new HashSet<UUID>();
    private final Queue<WriteCommand> mSaveQueue = new ConcurrentLinkedQueue<WriteCommand>();
    private final ScheduledExecutorService mWriteThread = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "MicroDB Write Thread");
        }
    });
    private AtomicBoolean mAutoSave = new AtomicBoolean(true);

    static final MapFunction<String> INDEX_OBJECT_TYPE = new MapFunction<String>() {
        @Override
        public void map(UBValue value, Emitter<String> emitter) {
            if(value != null && value.isObject()) {
                UBObject obj = value.asObject();
                UBValue typevar = obj.get("type");
                if(typevar != null && typevar.isString()) {
                    emitter.emit(typevar.asString());
                }
            }
        }
    };


    interface WriteCommand {
        void write() throws IOException;
    }

    void enqueWriteCommand(WriteCommand cmd) {
        mSaveQueue.add(cmd);
    }

    private class SaveDBData implements WriteCommand {

        private final UUID mId;
        private final UBObject mData;

        SaveDBData(UUID id, UBObject obj) {
            mId = id;
            mData = obj;
        }

        @Override
        public void write() throws IOException {
            mDriver.update(mId, mData);
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
            mDriver.update(mObject.getId(), data);
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
            final UUID id = mObject.getId();
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

    private static final String METAKEY_DBVERSION = "schema_version";
    private static final String METAKEY_INSTANCE = "instance";

    private void init() throws IOException {

        UBObject metaObj = mDriver.getMeta();
        if(!metaObj.containsKey(METAKEY_INSTANCE)) {
            metaObj.put(METAKEY_INSTANCE, UBValueFactory.createString(UUID.randomUUID().toString()));
            metaObj.put(METAKEY_DBVERSION, UBValueFactory.createInt(mSchemaVersion));
            mDriver.saveMeta(metaObj);
            mDriver.addIndex("type", INDEX_OBJECT_TYPE);
            mCallback.onUpgrade(this, -1, mSchemaVersion);

        } else {
            int currentVersion = metaObj.get(METAKEY_DBVERSION).asInt();
            if(currentVersion < mSchemaVersion) {
                mCallback.onUpgrade(this, currentVersion, mSchemaVersion);
                metaObj.put(METAKEY_DBVERSION, UBValueFactory.createInt(mSchemaVersion));
                mDriver.saveMeta(metaObj);
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
            UUID key = mDriver.insert(data);

            data.put("id", UBValueFactory.createString(key.toString()));
            retval.init(key, data, this);

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
    public synchronized <T extends DBObject> T get(UUID id, Class<T> classType) {
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
                    newObj.init(id, data.asObject(), this);
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

    public <T extends Comparable<?>> void addIndex(String indexName, MapFunction<T> mapFunction) throws IOException {
        mDriver.addIndex(indexName, mapFunction);
    }

    public void addChangeListener(ChangeListener listener) {
        mDriver.addChangeListener(listener);
    }

    public <K extends Comparable<?>, T extends DBObject> ObjectIterator<K, T> queryIndex(String indexName, Class<T> classType) throws IOException {
        KeyIterator<K> keyIt = mDriver.queryIndex(indexName);
        return new ObjectIterator<K, T>(keyIt, this, classType);
    }

    public <T extends DBObject> ObjectIterator<String, T> queryObjects(Class<T> classType) throws IOException {
        KeyIterator<String> keyIt = mDriver.queryIndex("type");
        keyIt.seekTo(classType.getSimpleName());
        return new ObjRangeIterator<T>(keyIt, this, classType);
    }
}
