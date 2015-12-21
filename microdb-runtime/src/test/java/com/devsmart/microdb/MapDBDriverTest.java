package com.devsmart.microdb;


import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBValueFactory;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.IOException;
import java.util.UUID;

public class MapDBDriverTest {

    private static UUID insert(String type, String name, MapDBDriver driver) throws IOException {
        UBObject obj;
        obj = UBValueFactory.createObject();
        obj.put("type", UBValueFactory.createString(type));
        obj.put("name", UBValueFactory.createString(name));

        return driver.insert(obj);
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

        Iterable<Row> rows = dbDriver.queryIndex("type", "dog", true, "dog", true);
        int dogCount = 0;
        for(Row r : rows){
            assertEquals("dog", r.getSecondaryKey());
            dogCount++;
        }
        assertEquals(3, dogCount);

        rows = dbDriver.queryIndex("type", "cat", true, "cat", true);
        int catCount = 0;
        for(Row r : rows) {
            assertEquals("cat", r.getSecondaryKey());
            catCount++;

        }
        assertEquals(3, catCount);

    }
}
