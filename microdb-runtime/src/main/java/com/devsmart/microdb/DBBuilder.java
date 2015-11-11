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

    private final File mDBPath;
    private DBCallback mCallback = new NullCallback();
    private int mSchemaVersion = 0;

    public static DBBuilder builder(File dbpath) {
        return new DBBuilder(dbpath);
    }

    private DBBuilder(File path) {
        mDBPath = path;
    }

    public DBBuilder callback(DBCallback cb) {
        mCallback = cb;
        return this;
    }

    public DBBuilder schemaVersion(int version) {
        mSchemaVersion = version;
        return this;
    }

    public MicroDB build() throws IOException {
        DB db = DBMaker.newFileDB(mDBPath)
                .make();

        MapDBDriver driver = new MapDBDriver(db);

        return new MicroDB(driver, mSchemaVersion, mCallback);

    }


}
