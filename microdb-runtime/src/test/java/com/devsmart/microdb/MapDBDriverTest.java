package com.devsmart.microdb;


import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBValueFactory;
import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.*;

public class MapDBDriverTest {

    private static UUID insert(String type, String name, MapDBDriver driver) throws IOException {
        UBObject obj;
        obj = UBValueFactory.createObject();
        obj.put("type", UBValueFactory.createString(type));
        obj.put("name", UBValueFactory.createString(name));

        final UUID id = driver.genId();
        driver.insert(id, obj);

        driver.incrementLongField("numObjects");
        return id;
    }


    @Test
    public void indexTest() throws IOException {

        DB mapdb = DBMaker.newMemoryDB()
                .make();

        MapDBDriver dbDriver = new MapDBDriver(mapdb);
        dbDriver.addIndex("type", MicroDB.INDEX_OBJECT_TYPE);

        insert("dog", "fido", dbDriver);
        insert("cat", "whiskers", dbDriver);
        insert("cat", "symo", dbDriver);
        insert("cat", "thuggy", dbDriver);
        insert("dog", "mondo", dbDriver);
        insert("dog", "bolt", dbDriver);

        Cursor rows = dbDriver.queryIndex("type", "dog", true, "dog", true);
        int dogCount = 0;
        while(rows.moveToNext()){
            Row r = rows.getRow();
            assertEquals("dog", r.getSecondaryKey());
            dogCount++;
        }
        assertEquals(3, dogCount);

        rows = dbDriver.queryIndex("type", "cat", true, "cat", true);
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
        DB mapdb = DBMaker.newMemoryDB()
                .make();

        MapDBDriver dbDriver = new MapDBDriver(mapdb);
        dbDriver.addIndex("type", MicroDB.INDEX_OBJECT_TYPE);

        insert("dog", "fido", dbDriver);
        insert("cat", "whiskers", dbDriver);
        insert("cat", "symo", dbDriver);
        insert("cat", "thuggy", dbDriver);
        insert("dog", "mondo", dbDriver);
        insert("dog", "bolt", dbDriver);

        Cursor rows = dbDriver.queryIndex("type", "dog", true, "dog", true);
        assertTrue(rows.isBeforeFirst());
        assertFalse(rows.isFirst());
        assertFalse(rows.isLast());
        assertFalse(rows.isAfterLast());
    }

    @Test
    public void testCompact() throws Exception {
        DB mapdb = DBMaker.newTempFileDB()
                .make();

        MapDBDriver dbDriver = new MapDBDriver(mapdb);
        dbDriver.addIndex("type", MicroDB.INDEX_OBJECT_TYPE);

        insert("dog", "fido", dbDriver);
        insert("cat", "whiskers", dbDriver);
        insert("cat", "symo", dbDriver);
        final UUID thuggyUUID = insert("cat", "thuggy", dbDriver);
        insert("dog", "mondo", dbDriver);
        insert("dog", "bolt", dbDriver);

        dbDriver.delete(thuggyUUID);
        dbDriver.commitTransaction();

        dbDriver.compact();

        Cursor rows = dbDriver.queryIndex("type", "dog", true, "dog", true);
        int dogCount = 0;
        while(rows.moveToNext()){
            Row r = rows.getRow();
            assertEquals("dog", r.getSecondaryKey());
            dogCount++;
        }
        assertEquals(3, dogCount);

        rows = dbDriver.queryIndex("type", "cat", true, "cat", true);
        int catCount = 0;
        while(rows.moveToNext()) {
            Row r = rows.getRow();
            assertEquals("cat", r.getSecondaryKey());
            catCount++;
        }
        assertEquals(2, catCount);

        assertEquals(6, dbDriver.incrementLongField("numObjects"));

    }
}
