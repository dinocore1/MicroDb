package com.devsmart.examples.intro;


import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.devsmart.microdb.NativeDriver;

import java.io.File;
import java.io.IOException;

public class IntroActivity extends Activity {

    private NativeDriver mDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mDriver = NativeDriver.open(new File(Environment.getExternalStorageDirectory(), "mydb.db").getAbsolutePath());
        } catch (IOException e) {
            Log.e("", "", e);
        }

    }
}
