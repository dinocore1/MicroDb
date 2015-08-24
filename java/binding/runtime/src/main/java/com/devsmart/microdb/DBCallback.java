package com.devsmart.microdb;


import java.io.IOException;

public interface DBCallback {

    void onUpgrade(int oldVersion, int newVersion) throws IOException;
}
