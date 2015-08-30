package com.devsmart.examples.intro;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.devsmart.examples.intro.model.Person;
import com.devsmart.microdb.DBBuilder;
import com.devsmart.microdb.DBIterator;
import com.devsmart.microdb.MicroDB;
import com.devsmart.microdb.ObjIterator;
import com.devsmart.microdb.ubjson.UBValue;
import com.google.common.base.Stopwatch;

import java.io.File;
import java.io.IOException;

public class IntroActivity extends Activity {

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        try {
            final File dbPath = new File(Environment.getExternalStorageDirectory(), "mydb.db");
            deleteTree(dbPath);
            mDatabase = DBBuilder.builder(dbPath).build();


            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    Stopwatch sw = Stopwatch.createStarted();

                    //LinkedList<Person> myList = new LinkedList<Person>();
                    for(int i=0;i<10000;i++) {
                        Person newPersion = mDatabase.create(Person.class);
                        newPersion.setFirstName("Santa");
                        newPersion.setLastName("Clause");
                        //myList.add(newPersion);
                    }

                    Log.i("", "create took: " + sw);

                    try {
                        mDatabase.flush();
                    } catch (IOException e) {
                        Log.e("", "", e);
                    }

                    Log.i("", "save took: " + sw);

                    try {
                        DBIterator it = mDatabase.queryIndex("type");
                        while(it.valid()) {

                            UBValue key = it.getKey();
                            UBValue primaryKey = it.getPrimaryKey();

                            Log.i("", String.format("type: %s key %s", key, primaryKey));

                            it.next();
                        }

                        ObjIterator<Person> it2 = mDatabase.getAll(Person.class);
                        while(it2.valid()) {
                            Person p = it2.get();

                            it2.next();
                        }

                    } catch (IOException e) {
                        Log.e("", "", e);
                    }

                    return null;
                }
            };
            task.execute();


        } catch (IOException e) {
            Log.e("", "", e);
        }

    }
}
