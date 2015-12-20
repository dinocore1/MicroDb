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
import java.util.concurrent.atomic.AtomicBoolean;

public class MicroDB {

    private static final Logger logger = LoggerFactory.getLogger(MicroDB.class);

    private Driver mDriver;
    private int mSchemaVersion;
    private DBCallback mCallback;
    private final HashMap<UUID, SoftReference<DBObject>> mLiveObjects = new HashMap<UUID, SoftReference<DBObject>>();
    private final Set<UUID> mDeletedObjects = new HashSet<UUID>();
    private final WriteQueue mWriteQueue = new WriteQueue();

    public enum OperationType {
        Write,
        NoOp,
        Shutdown
    }

    abstract static class Operation implements Runnable {
        public final OperationType mCommandType;
        private Exception mException;

        Operation(OperationType type) {
            mCommandType = type;
        }

        synchronized void complete() {
            notifyAll();
        }

        public synchronized void waitForCompletion() {
            try {
                wait();
            } catch (InterruptedException e) {
                logger.warn("", e);
            }
        }

        abstract void doIt() throws IOException;

        @Override
        public void run() {
            try {
                doIt();
            } catch (Exception e) {
                mException = e;
            }
        }
    }

    private class WriteQueue implements Runnable {
        private static final long DEFAULT_WAIT = 2000;
        private final Queue<Operation> mOperationQueue = new ConcurrentLinkedQueue<Operation>();
        private Thread mWriteThread = new Thread(this, "MicroDB Write Thread");

        @Override
        public void run() {
            while(true) {
                Operation op = mOperationQueue.poll();
                if(op == null) {
                    waitForNextCommand();
                } else {
                    try {
                        switch (op.mCommandType) {
                            case Write:
                                op.run();
                                break;

                            case NoOp:
                                break;

                            case Shutdown:
                                return;
                        }
                    } finally {
                        op.complete();
                    }
                }
            }

        }

        private synchronized void waitForNextCommand() {
            try {
                wait(DEFAULT_WAIT);
            } catch (InterruptedException e) {
                logger.warn("unexpected interrupt", e);
            }
        }


        public void start() {
            mWriteThread.start();
        }

        public void enqueue(Operation op) {
            mOperationQueue.offer(op);
        }

        public synchronized void kick() {
            notify();
        }
    }


    void enqueueOperation(Operation op) {
        mWriteQueue.enqueue(op);
    }

    private Operation createNoOp() {
        return new Operation(OperationType.NoOp) {
            @Override
            void doIt() throws IOException {
            }
        };
    }

    private Operation createWriteObject(final DBObject obj) {
        return new Operation(OperationType.Write) {
            @Override
            void doIt() throws IOException{
                final UUID id = obj.getId();
                obj.mDirty = false;
                mDriver.delete(id);
                synchronized (MicroDB.this) {
                    mDeletedObjects.remove(id);
                }
            }
        };
    }

    private Operation createSaveOperation(final UUID mId, final UBObject mData) {
        return new Operation(OperationType.Write) {
            @Override
            void doIt() throws IOException {
                mDriver.update(mId, mData);

            }
        };
    }

    private Operation createDeleteOperation(final DBObject obj) {
        return new Operation(OperationType.Write) {
            @Override
            void doIt() throws IOException {
                final UUID id = obj.getId();
                obj.mDirty = false;
                mDriver.delete(id);
                synchronized (MicroDB.this) {
                    mDeletedObjects.remove(id);
                }
            }
        };
    }

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

    public Driver getDriver() {
        return mDriver;
    }


    MicroDB(Driver driver, int schemaVersion, DBCallback cb) throws IOException {
        mDriver = driver;
        mSchemaVersion = schemaVersion;
        mCallback = cb;

        mWriteQueue.start();
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
            mWriteQueue.enqueue(createWriteObject(obj));
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
                    mWriteQueue.enqueue(createWriteObject(obj));
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

            UBObject data = UBValueFactory.createObject();
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
    public void save(DBObject obj) {
        checkValid(obj);
        mWriteQueue.enqueue(createWriteObject(obj));
    }

    private void checkValid(DBObject obj) {
        if(obj == null || obj.getDB() != this || obj.getId() == null) {
            throw new RuntimeException("DBObject is invalid. DBObjects must be create with MicroDB.create() methods");
        }
    }

    public synchronized void delete(DBObject obj) {
        checkValid(obj);
        mDeletedObjects.add(obj.getId());
        mWriteQueue.enqueue(createDeleteOperation(obj));
        mLiveObjects.remove(obj.getId());
    }

    /**
     * This method blocks until all queued write operation are completed.
     */
    public void sync() {
        Operation op = createNoOp();
        mWriteQueue.enqueue(op);
        mWriteQueue.kick();
        op.waitForCompletion();
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
