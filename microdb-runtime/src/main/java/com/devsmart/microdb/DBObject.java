package com.devsmart.microdb;

import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;

import java.io.IOException;
import java.util.UUID;

public class DBObject {

    private UUID mId;
    private MicroDB mDB;
    protected boolean mDirty;

    @Override
    protected void finalize() throws Throwable {
        try {
            if(mDB != null) {
                mDB.finalizing(this);
            }

        } finally {
            super.finalize();
        }
    }

    protected void init(UUID id, MicroDB microDB) {
        mId = id;
        mDB = microDB;
        mDirty = false;
    }

    protected void init(UUID id, UBObject data, MicroDB db) {
        mId = id;
        mDB = db;
        mDirty = false;
    }

    public void writeUBObject(UBObject data) {
        if(mId != null) {
            data.put("id", UBValueFactory.createString(mId.toString()));
        }
    }

    public UUID getId() {
        return mId;
    }

    public void save() throws IOException {
        if(mDB == null) {
            throw new RuntimeException("DBObject does not reference a database");
        }
        if(mId == null) {
            throw new RuntimeException("DBObject does not have an ID");
        }
        mDB.save(this);
    }

    public MicroDB getDB() {
        return mDB;
    }

    public void delete() throws IOException {
        if(mDB == null) {
            throw new RuntimeException("DBObject does not reference a database");
        }
        mDB.delete(this);
    }

    protected void setDirty() {
        mDirty = true;
    }
}