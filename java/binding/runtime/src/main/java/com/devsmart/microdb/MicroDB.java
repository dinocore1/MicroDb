package com.devsmart.microdb;


import com.devsmart.microdb.ubjson.UBObject;
import com.devsmart.microdb.ubjson.UBValue;
import com.devsmart.microdb.ubjson.UBValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Queue;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
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
    private final Queue<WriteCommand> mSaveQueue = new ConcurrentLinkedQueue<WriteCommand>();
    private final ScheduledExecutorService mWriteThread = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "MicroDB Write Thread");
        }
    });
    private AtomicBoolean mAutoSave = new AtomicBoolean(true);


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
            mDriver.delete(mObject.getId());
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
            mCallback.onUpgrade(this, -1, mSchemaVersion);
            metaObj.put(METAKEY_DBVERSION, UBValueFactory.createInt(mSchemaVersion));
            mSaveQueue.offer(new SaveDBData(metaObj));
            mDriver.addIndex("type", "emit(obj.type)");
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

    public synchronized <T extends DBObject> T get(UBValue id, Class<T> classType) {
        try {

            T retval;
            DBObject cached;

            SoftReference<DBObject> ref = mLiveObjects.get(id);
            if(ref != null && (cached = ref.get()) != null){
                retval = (T)cached;
            } else {
                UBObject data = mDriver.get(id).asObject();
                T newObj = classType.newInstance();
                newObj.init(data, this);
                retval = newObj;
                mLiveObjects.put(id, new SoftReference<DBObject>(retval));

            }

            return retval;
        } catch (Exception e){
            throw new RuntimeException("", e);
        }
    }

    public synchronized void save(DBObject obj) {
        mSaveQueue.offer(new SaveObject(obj));
    }

    public synchronized void delete(DBObject obj) {
        mSaveQueue.offer(new DeleteObject(obj));
        mLiveObjects.remove(obj.getId());
    }

    public void sync() {
        try {
            processWriteQueue().get();
        } catch (Exception e) {
            logger.error("sync operation ended with exception", e);
        }
    }

    public DBIterator queryIndex(String indexName) throws IOException {
        return mDriver.queryIndex(indexName);
    }

    public <T extends DBObject> ObjIterator<T> getAll(Class<T> classType) throws IOException {
        return new ObjIterator<T>(queryIndex("type"), this, classType);
    }
}
