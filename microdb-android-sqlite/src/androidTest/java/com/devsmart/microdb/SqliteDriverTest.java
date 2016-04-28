package com.devsmart.microdb;

import android.database.sqlite.SQLiteDatabase;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;

import java.io.File;
import java.util.UUID;


public class SqliteDriverTest extends InstrumentationTestCase {


    @MediumTest
    public void testWriteDatabase() throws Exception {
        final File testdbfile = new File("/sdcard/microdbtest/test.db");
        testdbfile.getParentFile().mkdirs();
        if(testdbfile.exists()) {
            testdbfile.delete();
        }


        SQLiteDatabase sqldb = SQLiteDatabase.openOrCreateDatabase(testdbfile, null);
        AndroidSqliteDBBuilder.onCreate(sqldb);

        AndroidSqliteDriver driver = new AndroidSqliteDriver(sqldb);

        UBObject obj = UBValueFactory.createObject();
        obj.put("my key", UBValueFactory.createString("hello world"));
        obj.put("my int", UBValueFactory.createInt(5));

        final UUID key = UUID.randomUUID();
        driver.insert(key, obj);

        final UBValue retval = driver.get(key);
        assertNotNull(retval);

    }
}
