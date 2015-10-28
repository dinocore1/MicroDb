package com.devsmart.examples.intro;

import android.os.Environment;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.devsmart.examples.intro.model.Address;
import com.devsmart.examples.intro.model.Person;
import com.devsmart.microdb.DBBuilder;
import com.devsmart.microdb.DBCallback;
import com.devsmart.microdb.DBIterator;
import com.devsmart.microdb.MicroDB;
import com.devsmart.microdb.ObjIterator;
import com.devsmart.ubjson.UBValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class IndexTest extends AndroidTestCase {

    private static final Logger logger = LoggerFactory.getLogger(IndexTest.class);


    private MicroDB mDatabase;

    private static void deleteTree(File root) {
        if(root.isDirectory()) {
            for(File f : root.listFiles()) {
                if(f.isFile()) {
                    f.delete();
                } else {
                    deleteTree(f);
                }
            }
        }
        root.delete();
    }

    @Override
    protected void setUp() throws Exception {

        File dbpath = new File(Environment.getExternalStorageDirectory(), "test.db");
        if(dbpath.exists()) {
            deleteTree(dbpath);
        }

        mDatabase = DBBuilder.builder(dbpath)
                .schemaVersion(0)
                .callback(new DBCallback() {
                    @Override
                    public void onUpgrade(MicroDB db, int oldVersion, int newVersion) throws IOException {
                        switch (newVersion) {
                            case 0:
                                assertTrue(db.addIndex(IDX_AGE, "if(obj.age != null) { emit(obj.age) }"));
                                assertTrue(db.addIndex(IDX_LASTNAME, "if(obj.lastName != null) { emit(obj.lastName) }"));
                                break;
                        }
                    }
                })
                .build();

        super.setUp();

    }

    private static final String IDX_AGE = "person_age";
    private static final String IDX_LASTNAME = "person_lastName";

    @Override
    protected void tearDown() throws Exception {
        mDatabase.close();
        super.tearDown();
    }

    @MediumTest
    public void testInvalidIndex() throws Exception {
        DBIterator it = mDatabase.queryIndex("invalid_bljsd");
        assertNull(it);
    }

    @MediumTest
    public void testIndex() throws Exception {

        Person person = mDatabase.create(Person.class);
        final UBValue santaId = person.getId();
        person.setFirstName("Santa");
        person.setLastName("Clause");
        person.setAge(50);
        person.save();

        person = mDatabase.create(Person.class);
        final UBValue timmyId = person.getId();
        person.setFirstName("Timmy");
        person.setLastName("Duff");
        person.setAge(30);
        person.save();

        mDatabase.flush();

        DBIterator it = mDatabase.queryIndex(IDX_AGE);
        assertTrue(it.valid());
        assertEquals(30, it.getKey().asInt());
        assertEquals(timmyId, it.getPrimaryKey());

        it.next();

        assertTrue(it.valid());
        assertEquals(50, it.getKey().asInt());
        assertEquals(santaId, it.getPrimaryKey());

        it.next();

        assertFalse(it.valid());

        it.close();

        it = mDatabase.queryIndex(IDX_LASTNAME);
        assertTrue(it.valid());
        assertEquals("Clause", it.getKey().asString());
        assertEquals(santaId, it.getPrimaryKey());

        it.next();

        assertTrue(it.valid());
        assertEquals("Duff", it.getKey().asString());
        assertEquals(timmyId, it.getPrimaryKey());

    }
}
