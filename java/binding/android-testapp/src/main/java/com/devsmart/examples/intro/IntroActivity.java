package com.devsmart.examples.intro;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.devsmart.examples.intro.model.Person;
import com.devsmart.microdb.DBBuilder;
import com.devsmart.microdb.MicroDB;
import com.devsmart.microdb.NativeDriver;
import com.google.common.base.Stopwatch;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class IntroActivity extends Activity {

    private MicroDB mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        try {
            mDatabase = DBBuilder.builder(new File(Environment.getExternalStorageDirectory(), "mydb.db")).build();


            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    Stopwatch sw = Stopwatch.createStarted();

                    //LinkedList<Person> myList = new LinkedList<Person>();
                    for(int i=0;i<100000;i++) {
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

                    return null;
                }
            };
            task.execute();


        } catch (IOException e) {
            Log.e("", "", e);
        }

    }
}
