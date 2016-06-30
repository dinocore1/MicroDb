package com.devsmart.microdb;


import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.io.IOException;

public class MapDBDBBuilder {

    static class NullCallback implements DBCallback {

        @Override
        public void onUpgrade(MicroDB db, int oldVersion, int newVersion) throws IOException {

        }
    }

    private DBCallback mCallback = new NullCallback();
    private int mSchemaVersion = 0;
    private Integer mCacheSize;

    public static MapDBDBBuilder builder() {
        return new MapDBDBBuilder();
    }

    private MapDBDBBuilder() {
    }

    public MapDBDBBuilder callback(DBCallback cb) {
        mCallback = cb;
        return this;
    }

    public MapDBDBBuilder schemaVersion(int version) {
        mSchemaVersion = version;
        return this;
    }

    public MapDBDBBuilder cacheSize(int cacheSize) {
        mCacheSize = cacheSize;
        return this;
    }

    public MicroDB build(File path) throws IOException {
        DBMaker mapdbBuilder = DBMaker.newFileDB(path);
        if(mCacheSize != null) {
            mapdbBuilder.cacheSize(mCacheSize);
        }
        DB db = mapdbBuilder.make();

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