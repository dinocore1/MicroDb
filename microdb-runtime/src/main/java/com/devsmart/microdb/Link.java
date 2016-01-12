package com.devsmart.microdb;


import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;

import java.util.UUID;

public class Link<T extends DBObject> {

    private UUID mId;
    final private DBObject mDBObj;
    private Class<? extends T> mClassType;

    Link(DBObject obj, Class<? extends T> classType) {
        mDBObj = obj;
        mClassType = classType;
    }

    Link(UBValue id, DBObject obj, Class<? extends T> classType) {
        this(obj, classType);
        if(id != null && id.isString()) {
            mId = UUID.fromString(id.asString());
        } else {
            mId = null;
        }
    }

    Link(UUID id, DBObject obj, Class<? extends T> classType) {
        this(obj, classType);
        mId = id;
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
            return mDBObj.getDB().get(mId, mClassType);
        }
    }

    public void set(T value) {
        if(value == null) {
            mId = null;
        } else {
            mId = value.getId();
        }
        mDBObj.setDirty();
    }

}
