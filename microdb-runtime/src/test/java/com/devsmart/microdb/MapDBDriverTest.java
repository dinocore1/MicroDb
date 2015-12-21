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

        Cursor<Comparable<?>> it = dbDriver.queryIndex("type");
        it.seekTo("dog");
        int dogCount = 0;
        while(it.hasNext()) {
            if("dog".equals(it.next())){
                dogCount++;
            }
        }
        assertEquals(3, dogCount);

        it.seekTo("cat");
        int catCount = 0;
        while(it.hasNext()) {
            if("cat".equals(it.next())){
                catCount++;
            }
        }
        assertEquals(3, catCount);

    }
}
