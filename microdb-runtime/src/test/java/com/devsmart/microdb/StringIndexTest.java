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

public class StringIndexTest {

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
        insert("Patrick", "Vin", dbDriver);
        insert("Greg", "Soucy", dbDriver);
        insert("Alex", "Murphy", dbDriver);

        int numGregs = 0;
        Cursor cursor = index.query(db, "Greg");
        assertEquals(1, cursor.getCount());
        while(cursor.moveToNext()){
            Row r = cursor.getRow();
            String firstName = r.getSecondaryKey();
            assertEquals("Greg", firstName);
            numGregs++;
        }

        assertEquals(1, numGregs);

        int numPauls = 0;
        cursor = index.query(db, "Paul");
        assertEquals(3, cursor.getCount());
        while(cursor.moveToNext()){
            Row r = cursor.getRow();
            String firstName = r.getSecondaryKey();
            assertEquals("Paul", firstName);
            numPauls++;
        }

        assertEquals(3, numPauls);

    }

    @Test
    public void testHeadSet() throws Exception {
        DB mapdb = DBMaker.newMemoryDB()
                .make();

        MapDBDriver dbDriver = new MapDBDriver(mapdb);
        MicroDB db = new MicroDB(dbDriver, 0, new DBBuilder.NullCallback());

        NameIndex index = new NameIndex();
        index.install(db);

        insert("aaa", "Soucy", dbDriver);
        insert("aba", "Simon", dbDriver);
        insert("bbb", "Alexander", dbDriver);
        insert("baa", "Vin", dbDriver);
        insert("ccc", "Soucy", dbDriver);
        insert("caa", "Murphy", dbDriver);

        Cursor cursor = db.queryIndex(index.INDEX_NAME, "ab", true, null, true);
        assertEquals(5, cursor.getCount());

        cursor = db.queryIndex(index.INDEX_NAME, "aba", true, null, true);
        assertEquals(5, cursor.getCount());

        cursor = db.queryIndex(index.INDEX_NAME, "aba", false, null, true);
        assertEquals(4, cursor.getCount());

    }

    @Test
    public void testTailSet() throws Exception {
        DB mapdb = DBMaker.newMemoryDB()
                .make();

        MapDBDriver dbDriver = new MapDBDriver(mapdb);
        MicroDB db = new MicroDB(dbDriver, 0, new DBBuilder.NullCallback());

        NameIndex index = new NameIndex();
        index.install(db);

        insert("aaa", "Soucy", dbDriver);
        insert("aba", "Simon", dbDriver);
        insert("bbb", "Alexander", dbDriver);
        insert("baa", "Vin", dbDriver);
        insert("ccc", "Soucy", dbDriver);
        insert("caa", "Murphy", dbDriver);

        Cursor cursor = db.queryIndex(index.INDEX_NAME, null, true, "b", false);
        assertEquals(2, cursor.getCount());

        cursor = db.queryIndex(index.INDEX_NAME, null, true, "bzz", false);
        assertEquals(4, cursor.getCount());

    }
}
