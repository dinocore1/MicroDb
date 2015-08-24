package com.devsmart.microdb;


import com.devsmart.microdb.ubjson.UBObject;
import com.devsmart.microdb.ubjson.UBValue;
import com.devsmart.microdb.ubjson.UBValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MicroDB {

    private static final Logger logger = LoggerFactory.getLogger(MicroDB.class);

    private Driver mDriver;
    private int mSchemaVersion;
    private DBCallback mCallback;
    private final HashMap<UBValue, SoftReference<DBObject>> mLiveObjects = new HashMap<UBValue, SoftReference<DBObject>>();
    private boolean mAutoSave = true;

    MicroDB(Driver driver, int schemaVersion, DBCallback cb) throws IOException {
        mDriver = driver;
        mSchemaVersion = schemaVersion;
        mCallback = cb;

        init();
    }

    private static final String METAKEY = "dbmeta";
    private static final String METAKEY_DBVERSION = "schema_version";

    private void init() throws IOException {

        UBValue key = UBValueFactory.createString(METAKEY);

        UBObject metaObj = mDriver.load(key);
        if(metaObj == null) {
            metaObj = new UBObject();
            metaObj.set("id", key);
            mCallback.onUpgrade(this, -1, mSchemaVersion);
            metaObj.set(METAKEY_DBVERSION, UBValueFactory.createInt(mSchemaVersion));
            mDriver.save(metaObj);
        } else {
            int currentVersion = metaObj.get(METAKEY_DBVERSION).asInt();
            if(currentVersion < mSchemaVersion) {
                mCallback.onUpgrade(this, currentVersion, mSchemaVersion);
                metaObj.set(METAKEY_DBVERSION, UBValueFactory.createInt(mSchemaVersion));
                mDriver.save(metaObj);
            }
        }

    }

    /**
     * called when a dbobject is being finalized by the GC
     * @param obj
     */
    protected synchronized void finalizing(DBObject obj) {
        if(mAutoSave && obj.mDirty) {
            try {
                save(obj);
            } catch (IOException e) {
                logger.error(String.format("error saving object %s", obj), e);
            }
        }
        mLiveObjects.remove(obj.getId());

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
    public synchronized void flush() throws IOException {
        for(SoftReference<DBObject> ref : mLiveObjects.values()) {
            DBObject obj = ref.get();
            if(obj != null) {
                save(obj);
            }
        }
    }

    public synchronized <T extends DBObject> T create(Class<T> classType) {
        try {
            if(!classType.getSimpleName().endsWith("_pxy")) {
                String proxyClassName = String.format("%s.%s_pxy", DBObject.class.getPackage().getName(), classType.getSimpleName());
                classType = (Class<T>) Class.forName(proxyClassName);
            }

            T retval = classType.newInstance();

            UBObject data = new UBObject();
            UBValue key = mDriver.save(data);
            data.set("id", key);
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
                UBObject data = mDriver.load(id);
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

    public synchronized void save(DBObject obj) throws IOException {
        UBObject data = new UBObject();
        obj.writeUBObject(data);
        mDriver.save(data);
        obj.mDirty = false;
    }



    public synchronized void delete(DBObject obj) throws IOException {
        mLiveObjects.remove(obj.getId());
        mDriver.delete(obj.getId());
    }

    public Transaction beginTransaction() {

        return new Transaction(this);
    }

    void commitTransaction() {

    }

    void rollbackTransaction() {

    }
}
