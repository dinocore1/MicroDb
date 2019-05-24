package com.devsmart.microdb;


import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class MicroDB
{

    private static final Logger logger = LoggerFactory.getLogger(MicroDB.class);

    private       Driver                                 mDriver;
    private       int                                    mSchemaVersion;
    private       DBCallback                             mCallback;
    private final HashMap<UUID, WeakReference<DBObject>> mLiveObjects     = new HashMap<UUID, WeakReference<DBObject>>();
    private final WriteQueue                             mWriteQueue      = new WriteQueue();
    private       ArrayList<ChangeListener>              mChangeListeners = new ArrayList<ChangeListener>();
    private       Map<String, Constructor>               mConstructorMap;

    @Override
    protected void finalize() throws Throwable
    {
        mWriteQueue.enqueue(createShutdownOperation());
        super.finalize();
    }

    public void shutdown()
    {
        mWriteQueue.enqueue(createShutdownOperation());
        try
        {
            mWriteQueue.mWriteThread.join();
        }
        catch (InterruptedException e)
        {
            logger.warn("", e);
        }
    }

    public enum OperationType
    {
        Write,
        NoOp,
        Shutdown
    }

    abstract static class Operation implements Runnable
    {
        public final OperationType mCommandType;
        private      Exception     mException;
        private      boolean       mCompleted = false;

        Operation(OperationType type)
        {
            mCommandType = type;
        }

        synchronized void complete()
        {
            mCompleted = true;
            notifyAll();
        }

        public synchronized void waitForCompletion()
        {
            while (!mCompleted)
            {
                try
                {
                    wait(1000);
                }
                catch (InterruptedException e)
                {
                    logger.warn("", e);
                }
            }
            if (mException != null)
            {
                Throwables.propagate(mException);
            }
        }

        abstract void doIt() throws IOException;

        @Override
        public void run()
        {
            try
            {
                doIt();
            }
            catch (Exception e)
            {
                logger.error("uncaught exception while performing write operation", e);
                mException = e;
            }
        }
    }

    private class WriteQueue implements Runnable
    {
        private static final long             DEFAULT_WAIT    = 2000;
        private final        Queue<Operation> mOperationQueue = new ConcurrentLinkedQueue<Operation>();
        private              Thread           mWriteThread    = new Thread(this, "MicroDB Write Thread");

        @Override
        public void run()
        {
            while (true)
            {
                Operation op = mOperationQueue.poll();
                if (op == null)
                {
                    waitForNextCommand();
                }
                else
                {
                    try
                    {
                        switch (op.mCommandType)
                        {
                            case Write:
                                op.run();
                                break;

                            case NoOp:
                                break;

                            case Shutdown:
                                logger.info("Write Thread exiting");
                                return;
                        }
                    }
                    finally
                    {
                        op.complete();
                    }
                }
            }


        }

        private synchronized void waitForNextCommand()
        {
            try
            {
                wait(DEFAULT_WAIT);
            }
            catch (InterruptedException e)
            {
                logger.warn("unexpected interrupt", e);
            }
        }


        public void start()
        {
            //mWriteThread.setDaemon(true);
            mWriteThread.start();
        }

        public void enqueue(Operation op)
        {
            mOperationQueue.offer(op);
        }

        public synchronized void kick()
        {
            notify();
        }
    }


    void enqueueOperation(Operation op)
    {
        mWriteQueue.enqueue(op);
    }

    private Operation createShutdownOperation()
    {
        return new Operation(OperationType.Shutdown)
        {
            @Override
            void doIt() throws IOException
            {
            }
        };
    }

    private Operation createNoOp()
    {
        return new Operation(OperationType.NoOp)
        {
            @Override
            void doIt() throws IOException
            {
            }
        };
    }

    private Operation createInsertOperation(final DBObject obj)
    {
        return new Operation(OperationType.Write)
        {
            @Override
            void doIt() throws IOException
            {
                final UUID id;
                UBObject data = UBValueFactory.createObject();
                synchronized (obj)
                {
                    id = obj.getId();
                    obj.beforeWrite();
                    obj.writeToUBObject(data);
                    obj.mDirty = false;
                }

                mDriver.insert(id, data);

                for (ChangeListener listener : mChangeListeners)
                {
                    listener.onAfterInsert(mDriver, id, data);
                }
            }
        };
    }

    private Operation createWriteObject(final DBObject obj)
    {
        return new Operation(OperationType.Write)
        {
            @Override
            void doIt() throws IOException
            {
                final UUID id;
                UBObject data = UBValueFactory.createObject();
                synchronized (obj)
                {
                    id = obj.getId();
                    obj.beforeWrite();
                    obj.writeToUBObject(data);
                    obj.mDirty = false;
                }

                for (ChangeListener listener : mChangeListeners)
                {
                    listener.onBeforeUpdate(mDriver, id, data);
                }

                mDriver.update(id, data);
            }
        };
    }

    private Operation createSaveOperation(final UUID id, final UBValue data)
    {
        return new Operation(OperationType.Write)
        {
            @Override
            void doIt() throws IOException
            {
                mDriver.update(id, data);
            }
        };
    }

    private Operation createDeleteOperation(final UUID objId)
    {
        return new Operation(OperationType.Write)
        {
            @Override
            void doIt() throws IOException
            {
                for (ChangeListener listener : mChangeListeners)
                {
                    listener.onBeforeDelete(mDriver, objId);
                }
                mDriver.delete(objId);
            }
        };
    }

    private Operation createCommitOperation()
    {
        return new Operation(OperationType.Write)
        {
            @Override
            void doIt() throws IOException
            {
                mDriver.commitTransaction();
            }
        };
    }

    private Operation createCompactOperation()
    {
        return new Operation(OperationType.Write)
        {
            @Override
            void doIt() throws IOException
            {
                mDriver.compact();
            }
        };
    }

    private AtomicBoolean mAutoSave = new AtomicBoolean(true);

    static final MapFunction<String> INDEX_OBJECT_TYPE = new MapFunction<String>()
    {
        @Override
        public void map(UBValue value, Emitter<String> emitter)
        {
            if (value != null && value.isObject())
            {
                UBObject obj = value.asObject();
                UBValue typevar = obj.get("type");
                if (typevar != null && typevar.isString())
                {
                    emitter.emit(typevar.asString());
                }
            }
        }
    };

    public Driver getDriver()
    {
        return mDriver;
    }


    MicroDB(Driver driver, int schemaVersion, DBCallback cb, Map<String, Constructor> constructorMap) throws IOException
    {
        mDriver = driver;
        mSchemaVersion = schemaVersion;
        mCallback = cb;
        mConstructorMap = constructorMap;

        mWriteQueue.start();
        init();
    }

    private static final String METAKEY_DBVERSION = "schema_version";
    private static final String METAKEY_INSTANCE  = "instance";

    private void init() throws IOException
    {
        mDriver.addIndex("type", INDEX_OBJECT_TYPE);

        int currentVersion = -1;
        UBObject metaObj = mDriver.getMeta();
        if (!metaObj.containsKey(METAKEY_INSTANCE))
        {
            mDriver.beginTransaction();
            metaObj.put(METAKEY_INSTANCE, UBValueFactory.createString(UUID.randomUUID().toString()));

            mDriver.saveMeta(metaObj);
            mDriver.commitTransaction();
        }
        else
        {
            currentVersion = metaObj.get(METAKEY_DBVERSION).asInt();
        }

        if (currentVersion < mSchemaVersion && mCallback.onNeedsUpgrade(this, currentVersion, mSchemaVersion))
        {
            upgrade();
        } 
        else 
        {
            mDriver.beginTransaction();            
            metaObj.put(METAKEY_DBVERSION, UBValueFactory.createInt(mSchemaVersion));
            mDriver.saveMeta(metaObj);
            mDriver.commitTransaction();
        }
    }

    public void upgrade() throws IOException
    {
        int currentVersion = -1;
        UBObject metaObj = mDriver.getMeta();
        if (metaObj.containsKey(METAKEY_DBVERSION))
        {
            currentVersion = metaObj.get(METAKEY_DBVERSION).asInt();
        }

        mDriver.beginTransaction();
        mCallback.doUpgrade(this, currentVersion, mSchemaVersion);
        metaObj.put(METAKEY_DBVERSION, UBValueFactory.createInt(mSchemaVersion));
        mDriver.saveMeta(metaObj);
        mDriver.commitTransaction();
    }

    public synchronized void close() throws IOException
    {
        flush();
        mLiveObjects.clear();
        mDriver.close();
    }

    /**
     * Saves all DBObjects that are marked dirty
     */
    public void flush()
    {
        synchronized (this)
        {
            for (WeakReference<DBObject> ref : mLiveObjects.values())
            {
                DBObject obj = ref.get();
                if (obj != null)
                {
                    synchronized (obj)
                    {
                        if (obj.mDirty)
                        {
                            mWriteQueue.enqueue(createWriteObject(obj));
                        }
                    }
                }
            }
        }
        sync();
    }

    /**
     * creates and inserts into the database a new object of type {@code classType}.
     *
     * @param classType
     * @param <T>
     * @return newly created object
     */
    public synchronized <T extends DBObject> T insert(Class<T> classType)
    {
        try
        {
            T retval = create(classType);
            final UUID key = mDriver.genId();
            retval.setId(key);

            UBObject data = UBValueFactory.createObject();
            retval.writeToUBObject(data);

            for (ChangeListener l : mChangeListeners)
            {
                l.onBeforeInsert(mDriver, data);
            }

            retval.readFromUBObject(data);

            retval.setDirty();
            mWriteQueue.enqueue(createInsertOperation(retval));
            mLiveObjects.put(key, new WeakReference<DBObject>(retval));

            return retval;

        }
        catch (Exception e)
        {
            throw new RuntimeException("", e);
        }
    }

    /**
     * Constructs a new proxy instance of {@code classType}
     *
     * @param classType
     * @param <T>
     * @return a new object of type T
     */
    public <T extends DBObject> T create(Class<T> classType)
    {
        try
        {
            T retval = classType.newInstance();
            retval.init(this);
            return retval;
        }
        catch (Exception e)
        {
            Throwables.propagate(e);
            return null;
        }
    }

    public interface Constructor<T extends DBObject>
    {
        T build();
    }


    public synchronized <T extends DBObject> T get(UUID id)
    {
        try
        {

            T retval;
            DBObject cached;

            WeakReference<DBObject> ref = mLiveObjects.get(id);
            if (ref != null && (cached = ref.get()) != null)
            {
                retval = (T) cached;
            }
            else
            {

                UBValue data = mDriver.get(id);
                if (data == null)
                {
                    return null;
                }
                else
                {

                    if (!data.isObject())
                    {
                        throw new RuntimeException("database entry with id: " + id + " is not an object");
                    }

                    final String dataType = data.asObject().get("type").asString();
                    retval = (T) mConstructorMap.get(dataType).build();

                    retval.init(this);
                    retval.setId(id);
                    retval.readFromUBObject(data.asObject());
                    retval.afterRead();
                    mLiveObjects.put(id, new WeakReference<DBObject>(retval));
                }
            }

            return retval;
        }
        catch (Exception e)
        {
            throw new RuntimeException("", e);
        }

    }

    /**
     * fetch and load database object with primary key {@code id}.
     *
     * @param id
     * @param shell new object of type
     * @param <T>
     * @return dbobject
     */
    public synchronized <T extends DBObject> T get(UUID id, T shell)
    {
        try
        {

            T retval;
            DBObject cached;

            WeakReference<DBObject> ref = mLiveObjects.get(id);
            if (ref != null && (cached = ref.get()) != null)
            {
                retval = (T) cached;
            }
            else
            {

                UBValue data = mDriver.get(id);
                if (data == null)
                {
                    return null;
                }
                else
                {

                    if (!data.isObject())
                    {
                        throw new RuntimeException("database entry with id: " + id + " is not an object");
                    }

                    shell.init(this);
                    shell.setId(id);
                    shell.readFromUBObject(data.asObject());
                    shell.afterRead();
                    retval = shell;
                    mLiveObjects.put(id, new WeakReference<DBObject>(retval));
                }
            }

            return retval;
        }
        catch (Exception e)
        {
            throw new RuntimeException("", e);
        }
    }

    public UBValue get(String key) throws IOException
    {
        UUID id = UUID.nameUUIDFromBytes(key.getBytes(Charsets.UTF_8));
        return mDriver.get(id);
    }

    public synchronized UBValue getRaw(UUID id) throws IOException
    {
        return mDriver.get(id);
    }

    /**
     * saves/updates {@code obj} to the database. This method is not normally necessary for users to call
     * because database objects will automatically be saved when the garbage collector collects them if
     * they are marked dirty.
     *
     * @param obj the data to be saved
     */
    public Operation save(DBObject obj)
    {
        checkValid(obj);
        Operation op = createWriteObject(obj);
        mWriteQueue.enqueue(op);
        return op;
    }

    public Operation save(String key, UBValue data)
    {
        UUID id = UUID.nameUUIDFromBytes(key.getBytes(Charsets.UTF_8));
        Operation op = createSaveOperation(id, data);
        mWriteQueue.enqueue(op);
        return op;
    }

    private void checkValid(DBObject obj)
    {
        if (obj == null || obj.getDB() != this || obj.getId() == null)
        {
            throw new RuntimeException("DBObject is invalid. DBObjects must be created with MicroDB.insert() method");
        }
    }

    public synchronized Operation delete(DBObject obj)
    {
        checkValid(obj);
        return delete(obj.getId());
    }

    public synchronized Operation delete(UUID objId)
    {
        Operation op = createDeleteOperation(objId);
        mWriteQueue.enqueue(op);
        mLiveObjects.remove(objId);
        return op;
    }

    public Operation commit()
    {
        Operation op = createCommitOperation();
        mWriteQueue.enqueue(op);
        return op;
    }

    public void waitForCompletion(Operation op)
    {
        mWriteQueue.kick();
        op.waitForCompletion();
    }

    /**
     * This method blocks until all queued write operation are completed.
     */
    public void sync()
    {
        //Operation op = createNoOp();
        Operation op = createCommitOperation();
        mWriteQueue.enqueue(op);
        waitForCompletion(op);
    }

    public void compact()
    {
        Operation op = createCompactOperation();
        mWriteQueue.enqueue(op);
        waitForCompletion(op);
    }

    public <T extends Comparable<T>> void addIndex(String indexName, MapFunction<T> mapFunction) throws IOException
    {
        mDriver.addIndex(indexName, mapFunction);
    }

    public void addChangeListener(ChangeListener listener)
    {
        mChangeListeners.add(listener);
    }

    public <T extends Comparable<T>> Cursor queryIndex(String indexName, T min, boolean minInclusive, T max, boolean maxInclusive) throws IOException
    {
        return mDriver.queryIndex(indexName, min, minInclusive, max, maxInclusive);
    }

    public <T extends DBObject> Iterable<T> getAllOfType(final Class<T> classType) throws IOException
    {
        final String className = classType.getSimpleName();

        return new Iterable<T>()
        {
            @Override
            public Iterator<T> iterator()
            {
                try
                {
                    final Cursor cursor = queryIndex("type", className, true, className, true);
                    return new RowIterator<T>(cursor, MicroDB.this, classType);
                }
                catch (IOException e)
                {
                    Throwables.propagate(e);
                    return null;
                }
            }
        };

    }

    private static class RowIterator<T extends DBObject> implements Iterator<T>
    {

        private final MicroDB  mDB;
        private final Class<T> mClassType;
        private       Cursor   mCursor;
        private       Row      mCurrentRow;

        public RowIterator(Cursor cursor, MicroDB db, Class<T> classType)
        {
            mCursor = cursor;
            mDB = db;
            mClassType = classType;
            mCurrentRow = mCursor.get();
        }

        @Override
        public boolean hasNext()
        {
            return mCurrentRow != null;
        }

        @Override
        public T next()
        {
            try
            {
                final UUID objId = mCurrentRow.getPrimaryKey();
                T retval = mDB.get(objId, mClassType.newInstance());

                mCursor.next();
                mCurrentRow = mCursor.get();
                return retval;
            }
            catch (Exception e)
            {
                Throwables.propagate(e);
                return null;
            }
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException("remove not implemented");
        }
    }

    /*
    public <T extends DBObject, K extends Comparable<K>> Iterable<T> queryIndex(String indexName, final Class<T> classType, K min, boolean minInclusive, K max, boolean maxInclusive) throws IOException {
        final Cursor rowCursor = queryIndex(indexName, min, minInclusive, max, maxInclusive);
        return Iterables.transform(rowCursor, new Function<Row, T>() {
            @Override
            public T apply(Row input) {
                try {
                    T shell = classType.newInstance();
                    return get(input.getPrimaryKey(), shell);
                } catch (Exception e) {
                    Throwables.propagate(e);
                    return null;
                }
            }
        });
    }


    */

}
