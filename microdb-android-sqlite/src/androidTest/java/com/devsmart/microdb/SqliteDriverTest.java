package com.devsmart.microdb;

import android.database.sqlite.SQLiteDatabase;
import android.support.test.runner.AndroidJUnit4;

import org.junit.runner.RunWith;

import java.io.File;

@RunWith(AndroidJUnit4.class)
public class SqliteDriverTest extends DriverTest {


    @Override
    public Driver createNewDriver() {
        final File testdbfile = new File("/sdcard/microdbtest/test.db");
        testdbfile.getParentFile().mkdirs();
        if(testdbfile.exists()) {
            testdbfile.delete();
        }


        SQLiteDatabase sqldb = SQLiteDatabase.openOrCreateDatabase(testdbfile, null);
        AndroidSqliteDBBuilder.onCreate(sqldb);

        AndroidSqliteDriver driver = new AndroidSqliteDriver(sqldb);
        return driver;
    }
}
