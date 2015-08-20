package com.devsmart.microdb;


import com.devsmart.microdb.ubjson.UBObject;
import com.devsmart.microdb.ubjson.UBValue;

import java.lang.ref.SoftReference;
import java.util.HashMap;

public class MicroDB {

    private HashMap<UBValue, SoftReference<DBObject>> mLiveObjects = new HashMap<>()


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
