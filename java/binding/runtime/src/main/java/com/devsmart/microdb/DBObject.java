package com.devsmart.microdb;

import com.devsmart.microdb.ubjson.UBObject;
import com.devsmart.microdb.ubjson.UBValue;

public class DBObject {

    private UBValue mId;
    private MicroDB mDB;
    protected boolean mDirty;

    protected void init(UBObject data, MicroDB db) {
        mId = data.get("id");
        mDB = db;
        mDirty = false;
    }

    public void writeUBObject(UBObject data) {
        if(mId != null) {
            data.set("id", mId);
        }
    }

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

    public UBValue getId() {
        return mId;
    }
	
}