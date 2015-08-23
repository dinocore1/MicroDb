package com.devsmart.examples.intro;


import android.app.Activity;
import android.os.Bundle;

import com.devsmart.microdb.NativeDriver;

public class IntroActivity extends Activity {

    private NativeDriver mDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDriver = new NativeDriver();

    }
}
