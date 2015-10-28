package com.devsmart.microdb;


import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;

public class Link<T extends DBObject> {

    private UBValue mId;
    private MicroDB mDB;
    private Class<? extends T> mClassType;

    public Link(UBValue id, MicroDB db, Class<? extends T> classType) {
        mId = id;
        mDB = db;
        mClassType = classType;
    }

    public UBValue getId() {
        return mId;
    }

    public T get() {
        if(mId == null || mId.isNull()) {
            return null;
        } else {
            return mDB.get(mId, mClassType);
        }
    }

    public void set(T value) {
        if(value == null) {
            clear();
        } else {
            mId = value.getId();
        }
    }

    public void clear() {
        mId = UBValueFactory.createNull();
    }
}
