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

    public UBValue getId() {
        return mId;
    }
	
}