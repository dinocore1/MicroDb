package com.devsmart.examples.intro;

import android.os.Environment;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.devsmart.examples.intro.model.Address;
import com.devsmart.examples.intro.model.Person;
import com.devsmart.microdb.DBBuilder;
import com.devsmart.microdb.DBIterator;
import com.devsmart.microdb.MicroDB;
import com.devsmart.microdb.ObjIterator;
import com.devsmart.microdb.ubjson.UBValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Test1 extends AndroidTestCase {

    private static final Logger logger = LoggerFactory.getLogger(Test1.class);


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
                .build();

        super.setUp();

    }

    @Override
    protected void tearDown() throws Exception {

        mDatabase.close();

        super.tearDown();
    }

    @MediumTest
    public void testInsert() throws Exception {

        Address address = mDatabase.create(Address.class);
        address.setAddressLine("53 Grove Street");

        Person person = mDatabase.create(Person.class);
        person.setLastName("Jerry");
        person.setLastName("Sinfield");

        address = mDatabase.create(Address.class);
        address.setAddressLine("230 Farmer Steet");

        person = mDatabase.create(Person.class);
        person.setLastName("Martha");
        person.setLastName("Steward");

        mDatabase.flush();

        {
            DBIterator it = mDatabase.queryIndex("type");
            while (it.valid()) {
                UBValue key = it.getKey();

                logger.info("key: {}", key);

                it.next();
            }
        }

        {
            ObjIterator<Person> it = mDatabase.getAll(Person.class);
            int count = 0;
            while (it.valid()) {
                person = it.get();
                count++;

                it.next();
            }

            it.close();

            assertEquals(2, count);
        }

        {
            ObjIterator<Address> it = mDatabase.getAll(Address.class);

            int count = 0;
            while (it.valid()) {
                address = it.get();
                count++;

                it.next();
            }

            it.close();

            assertEquals(2, count);
        }




    }
}
