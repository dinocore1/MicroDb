package com.devsmart.microdb;


import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class DBBuilder {

    static class NullCallback implements DBCallback {

        @Override
        public boolean onNeedsUpgrade(MicroDB db, int oldVersion, int newVersion)
        {
            return false;
        }

        @Override
        public void doUpgrade(MicroDB db, int oldVersion, int newVersion) throws IOException {

        }
    }

    private DBCallback mCallback = new NullCallback();
    private int mSchemaVersion = 0;
    private Integer mCacheSize;
    private Map<String, MicroDB.Constructor> mConstructorMap;

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

    public DBBuilder cacheSize(int cacheSize) {
        mCacheSize = cacheSize;
        return this;
    }

    public DBBuilder constructorsMap(Map<String, MicroDB.Constructor> constructors) {
        mConstructorMap = constructors;
        return this;
    }

    public MicroDB build(File path) throws IOException {
        DBMaker mapdbBuilder = DBMaker.newFileDB(path);
        if(mCacheSize != null) {
            mapdbBuilder.cacheSize(mCacheSize);
        }
        DB db = mapdbBuilder.make();

        MapDBDriver driver = new MapDBDriver(db);

        return new MicroDB(driver, mSchemaVersion, mCallback, mConstructorMap);

    }

    public MicroDB buildMemoryDB() throws IOException {
        DB db = DBMaker.newMemoryDirectDB()
                .transactionDisable()
                .make();

        MapDBDriver driver = new MapDBDriver(db);
        return new MicroDB(driver, mSchemaVersion, mCallback, mConstructorMap);
    }


}
