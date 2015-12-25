package com.devsmart.microdb;


import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.io.IOException;

public class DBBuilder {

    static class NullCallback implements DBCallback {

        @Override
        public void onUpgrade(MicroDB db, int oldVersion, int newVersion) throws IOException {

        }
    }

    private DBCallback mCallback = new NullCallback();
    private int mSchemaVersion = 0;

    public static DBBuilder builder() {
        return new DBBuilder();
    }

    private DBBuilder() {
    }

    public DBBuilder callback(DBCallback cb) {
        mCallback = cb;
        return this;
    }

    public DBBuilder schemaVersion(int version) {
        mSchemaVersion = version;
        return this;
    }

    public MicroDB build(File path) throws IOException {
        DB db = DBMaker.newFileDB(path)
                .transactionDisable()
                .make();

        MapDBDriver driver = new MapDBDriver(db);

        return new MicroDB(driver, mSchemaVersion, mCallback);

    }

    public MicroDB buildMemoryDB() throws IOException {
        DB db = DBMaker.newMemoryDirectDB()
                .transactionDisable()
                .make();

        MapDBDriver driver = new MapDBDriver(db);
        return new MicroDB(driver, mSchemaVersion, mCallback);
    }


}
