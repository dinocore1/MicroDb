package com.devsmart.microdb;

import com.devsmart.microdb.ubjson.UBObject;

public class DBObject {

    private MicroDB mDB;
    private String mKey;
    protected UBObject mData;

    public String getKey() {
        return mKey;
    }

    public void save() {
        mDB.update(this);
    }

    public void delete() {
        mDB.delete(this);
    }


	
}