package com.devsmart.microdb;


import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;

import java.util.UUID;

public class Link<T extends DBObject> {

    private UUID mId;
    private MicroDB mDB;
    private Class<? extends T> mClassType;

    public Link(UBValue id, MicroDB db, Class<? extends T> classType) {
        if(id != null && id.isString()) {
            mId = UUID.fromString(id.asString());
        } else {
            mId = null;
        }
        mDB = db;
        mClassType = classType;
    }

    public UBValue getId() {
        if(mId == null) {
            return UBValueFactory.createNull();
        } else {
            return UBValueFactory.createString(mId.toString());
        }
    }

    public T get() {
        if(mId == null) {
            return null;
        } else {
            return mDB.get(mId, mClassType);
        }
    }

    public void set(T value) {
        if(value == null) {
            mId = null;
        } else {
            mId = value.getId();
        }
    }

}
