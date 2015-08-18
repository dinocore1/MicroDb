package com.devsmart.microdb;


public class MicroDB {



    public String insert(MicroDBObject obj) {
        //TODO:
        return null;

    }

    public void update(MicroDBObject obj) {

    }

    public void delete(String key) {

    }

    public void delete(MicroDBObject obj) {
        delete(obj.getKey());
    }

    public Transaction beginTransaction() {

        return new Transaction(this);
    }

    void commitTransaction() {

    }

    void rollbackTransaction() {

    }
}
