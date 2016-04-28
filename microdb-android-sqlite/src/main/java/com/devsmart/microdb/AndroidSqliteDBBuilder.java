package com.devsmart.microdb;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.IOException;

public class AndroidSqliteDBBuilder {

    private static final int DB_VERSION = 1;

    private DBCallback mCallback;
    private int mSchemaVersion;

    public static AndroidSqliteDBBuilder builder() {
        return new AndroidSqliteDBBuilder();
    }

    private AndroidSqliteDBBuilder() {

    }

    public AndroidSqliteDBBuilder callback(DBCallback cb) {
        mCallback = cb;
        return this;
    }

    public AndroidSqliteDBBuilder schemaVersion(int version) {
        mSchemaVersion = version;
        return this;
    }

    static void onCreate(SQLiteDatabase db) {
        final String sqlCreateTables = String.format("CREATE TABLE Objects (%s TEXT PRIMARY KEY, %s BLOB);",
                AndroidSqliteDriver.TableObjects.COLUMN_ID, AndroidSqliteDriver.TableObjects.COLUMN_DATA);

        db.execSQL(sqlCreateTables);

    }

    public MicroDB build(File path) throws IOException {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(path, null);
        final int dbVersion = db.getVersion();
        if(dbVersion == 0) {
            onCreate(db);
        }

        AndroidSqliteDriver driver = new AndroidSqliteDriver(db);
        return new MicroDB(driver, mSchemaVersion, mCallback);
    }

}
