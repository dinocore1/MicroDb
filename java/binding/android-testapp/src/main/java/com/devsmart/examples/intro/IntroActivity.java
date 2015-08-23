package com.devsmart.examples.intro;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.devsmart.microdb.NativeDriver;

import java.io.IOException;

public class IntroActivity extends Activity {

    private NativeDriver mDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mDriver = NativeDriver.open("mydb.db");
        } catch (IOException e) {
            Log.e("", "", e);
        }

    }
}
