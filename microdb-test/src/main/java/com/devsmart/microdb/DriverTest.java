package com.devsmart.microdb;


import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBValueFactory;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.UUID;


public abstract class DriverTest {


    private static UUID insert(String type, String name, Driver driver) throws IOException {
        UBObject obj;
        obj = UBValueFactory.createObject();
        obj.put("type", UBValueFactory.createString(type));
        obj.put("name", UBValueFactory.createString(name));

        final UUID id = driver.genId();
        driver.insert(id, obj);
        return id;
    }

    public abstract Driver createNewDriver();


    @Test
    public void indexTest() throws IOException {

        Driver driver = createNewDriver();
        driver.addIndex("type", MicroDB.INDEX_OBJECT_TYPE);

        insert("dog", "fido", driver);
        insert("cat", "whiskers", driver);
        insert("cat", "symo", driver);
        insert("cat", "thuggy", driver);
        insert("dog", "mondo", driver);
        insert("dog", "bolt", driver);

        Cursor rows = driver.queryIndex("type", "dog", true, "dog", true);
        int dogCount = 0;
        while(rows.moveToNext()){
            Row r = rows.getRow();
            assertEquals("dog", r.getSecondaryKey());
            dogCount++;
        }
        assertEquals(3, dogCount);

        rows = driver.queryIndex("type", "cat", true, "cat", true);
        int catCount = 0;
        while(rows.moveToNext()) {
            Row r = rows.getRow();
            assertEquals("cat", r.getSecondaryKey());
            catCount++;
        }
        assertEquals(3, catCount);

    }

    @Test
    public void indexStartState() throws Exception {

        Driver driver = createNewDriver();
        driver.addIndex("type", MicroDB.INDEX_OBJECT_TYPE);

        insert("dog", "fido", driver);
        insert("cat", "whiskers", driver);
        insert("cat", "symo", driver);
        insert("cat", "thuggy", driver);
        insert("dog", "mondo", driver);
        insert("dog", "bolt", driver);

        Cursor rows = driver.queryIndex("type", "dog", true, "dog", true);
        assertTrue(rows.isBeforeFirst());
        assertFalse(rows.isFirst());
        assertFalse(rows.isLast());
        assertFalse(rows.isAfterLast());
    }
}
