package com.devsmart.microdb;


import java.util.UUID;

public class Link<T extends DBObject> {

    private UUID mId;
    private MicroDB mDB;
    private Class<? extends T> mClassType;

    public Link(UUID id, MicroDB db, Class<? extends T> classType) {
        mId = id;
        mDB = db;
        mClassType = classType;
    }

    public UUID getId() {
        return mId;
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
