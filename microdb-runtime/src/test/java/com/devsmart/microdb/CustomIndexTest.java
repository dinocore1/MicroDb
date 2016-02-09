package com.devsmart.microdb;


import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.IOException;
import java.util.UUID;

public class CustomIndexTest {

    private static UUID insert(String firstName, String lastName, MapDBDriver driver) throws IOException {
        UBObject obj;
        obj = UBValueFactory.createObject();
        obj.put("type", UBValueFactory.createString("person"));
        obj.put("firstName", UBValueFactory.createString(firstName));
        obj.put("lastName", UBValueFactory.createString(lastName));

        final UUID id = driver.genId();
        driver.insert(id, obj);
        return id;
    }

    private static class NameIndex implements Dataset {

        private static final String INDEX_NAME = "Person.FirstName";
        private static final MapFunction<String> MAP_FUCTION = new MapFunction<String>() {
            @Override
            public void map(UBValue value, Emitter<String> emitter) {
                if(value != null && value.isObject()) {
                    UBObject obj = value.asObject();
                    UBValue firstName = obj.get("firstName");
                    if(firstName != null && firstName.isString()) {
                        emitter.emit(firstName.asString());
                    }
                }

            }
        };

        @Override
        public void install(MicroDB db) throws IOException {
            db.addIndex(INDEX_NAME, MAP_FUCTION);
        }

        public Cursor query(MicroDB db, String value) throws IOException {
            return db.queryIndex(INDEX_NAME, value, true, value, true);
        }
    }

    @Test
    public void testCreateCustomIndex() throws Exception {
        DB mapdb = DBMaker.newMemoryDB()
                .make();

        MapDBDriver dbDriver = new MapDBDriver(mapdb);
        MicroDB db = new MicroDB(dbDriver, 0, new DBBuilder.NullCallback());

        NameIndex index = new NameIndex();
        index.install(db);

        insert("Paul", "Soucy", dbDriver);
        insert("Paul", "Simon", dbDriver);
        insert("Paul", "Alexander", dbDriver);
        insert("Greg", "Soucy", dbDriver);
        insert("Alex", "Murphy", dbDriver);

        int numGregs = 0;
        Cursor cursor = index.query(db, "Greg");
        assertEquals(1, cursor.getCount());
        do {
            Row r = cursor.get();
            String firstName = r.getSecondaryKey();
            assertEquals("Greg", firstName);
            numGregs++;
        } while(cursor.next());

        assertEquals(1, numGregs);

        int numPauls = 0;
        cursor = index.query(db, "Paul");
        assertEquals(3, cursor.getCount());
        do {
            Row r = cursor.get();
            String firstName = r.getSecondaryKey();
            assertEquals("Paul", firstName);
            numPauls++;
        } while(cursor.next());

        assertEquals(3, numPauls);

    }
}
