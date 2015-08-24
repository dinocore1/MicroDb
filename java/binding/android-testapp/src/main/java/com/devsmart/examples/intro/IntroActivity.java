package com.devsmart.examples.intro;


import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.devsmart.microdb.DBBuilder;
import com.devsmart.microdb.MicroDB;
import com.devsmart.microdb.NativeDriver;

import java.io.File;
import java.io.IOException;

public class IntroActivity extends Activity {

    private MicroDB mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        try {
            mDatabase = DBBuilder.builder(new File(Environment.getExternalStorageDirectory(), "mydb.db")).build();
        } catch (IOException e) {
            Log.e("", "", e);
        }

    }
}
