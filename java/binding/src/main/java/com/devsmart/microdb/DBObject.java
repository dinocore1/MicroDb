package com.devsmart.microdb;

import com.devsmart.microdb.ubjson.UBObject;

public class DBObject {

    private MicroDB mDB;
    protected UBObject mData;

    private String id;

    void init(UBObject data, MicroDB db) {
        mData = data;
        mDB = db;

        id = mData.get("id").asString();
    }

    public String getId() {
        return id;
    }

    public void save() {
        mDB.update(this);
    }

    public void delete() {
        mDB.delete(this);
    }


	
}