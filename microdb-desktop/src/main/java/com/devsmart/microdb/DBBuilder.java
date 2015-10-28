package com.devsmart.microdb;


import java.io.File;
import java.io.IOException;

public class DBBuilder {

    private static class NullCallback implements DBCallback {

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
        NativeDriver driver = NativeDriver.open(mDBPath.getAbsolutePath());
        if(driver == null) {
            throw new IOException("could not open db from path: " + mDBPath.getAbsolutePath());
        }
        return new MicroDB(driver, mSchemaVersion, mCallback);

    }


}
