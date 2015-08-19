package com.devsmart.microdb;

public class DBObject {

    private MicroDB mDB;
    private String mKey;

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