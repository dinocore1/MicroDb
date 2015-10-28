package com.devsmart.microdb;

import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBValue;

import java.io.IOException;

public class DBObject {

    private UBValue mId;
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

    protected void init(UBObject data, MicroDB db) {
        mId = data.get("id");
        mDB = db;
        mDirty = false;
    }

    public void writeUBObject(UBObject data) {
        if(mId != null) {
            data.put("id", mId);
        }
    }

    public UBValue getId() {
        return mId;
    }

    public void save() throws IOException {
        if(mDB == null) {
            throw new RuntimeException("DBObject does not reference a database");
        }
        mDB.save(this);
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