package com.devsmart.microdb;


import java.io.Closeable;
import java.io.IOException;

public class Transaction implements Closeable {

    private MicroDB mDB;
    private boolean mSuccess;

    public Transaction(MicroDB db) {
        mDB = db;
    }

    public void markSuccessfull() {
        mSuccess = true;
    }

    public void endTransaction() {
        if(mSuccess) {
            //mDB.commitTransaction();
        } else {
            //mDB.rollbackTransaction();
        }

    }

    @Override
    public void close() throws IOException {
        endTransaction();
    }
}
