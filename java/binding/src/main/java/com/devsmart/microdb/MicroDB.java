package com.devsmart.microdb;


public class MicroDB {



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
