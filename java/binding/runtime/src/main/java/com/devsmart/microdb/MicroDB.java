package com.devsmart.microdb;


import com.devsmart.microdb.ubjson.UBObject;
import com.devsmart.microdb.ubjson.UBValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MicroDB {

    private static final Logger logger = LoggerFactory.getLogger(MicroDB.class);

    //private HashMap<UBValue, SoftReference<DBObject>> mLiveObjects = new HashMap<>()

    private Driver mDriver;
    private boolean mAutoSave = true;

    /**
     * called when a dbobject is being finalized by the GC
     * @param obj
     */
    protected void finalizing(DBObject obj) {
        if(mAutoSave && obj.mDirty) {
            try {
                save(obj);
            } catch (IOException e) {
                logger.error(String.format("error saving object %s", obj), e);
            }
        }

    }

    public <T extends DBObject> T create(Class<T> classType) {
        try {
            if(!classType.getSimpleName().endsWith("_pxy")) {
                String proxyClassName = String.format("%s.%s_pxy", DBObject.class.getPackage().getName(), classType.getSimpleName());
                classType = (Class<T>) Class.forName(proxyClassName);
            }

            T retval = classType.newInstance();

            //TODO: init data
            UBObject data = null;
            retval.init(data, this);
            return retval;
        } catch (Exception e) {
            throw new RuntimeException("", e);
        }
    }

    public <T extends DBObject> T get(UBValue id, Class<T> classType) {
        try {
            //TODO: look in the cache / load the data
            UBObject data = null;
            T retval = classType.newInstance();
            retval.init(data, this);
            return retval;
        } catch (Exception e){
            throw new RuntimeException("", e);
        }
    }

    public void save(DBObject obj) throws IOException {
        UBObject data = new UBObject();
        obj.writeUBObject(data);
        mDriver.save(data);
        obj.mDirty = false;
    }


    public void update(DBObject obj) {

    }

    public void delete(String key) {

    }

    public void delete(DBObject obj) {

    }

    public Transaction beginTransaction() {

        return new Transaction(this);
    }

    void commitTransaction() {

    }

    void rollbackTransaction() {

    }
}
