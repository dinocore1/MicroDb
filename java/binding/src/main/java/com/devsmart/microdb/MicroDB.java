package com.devsmart.microdb;


import com.devsmart.microdb.ubjson.UBValue;

public class MicroDB {


    public <T extends DBObject> T get(UBValue id, Class<T> classType) {
        return null;
    }

    public String insert(DBObject obj) {
        //TODO:
        return null;

    }

    public void update(DBObject obj) {

    }

    public void delete(String key) {

    }

    public void delete(DBObject obj) {

    }

    public Transaction beginTransaction() {

        return new Transaction(this);
    }

    void commitTransaction() {

    }

    void rollbackTransaction() {

    }
}
