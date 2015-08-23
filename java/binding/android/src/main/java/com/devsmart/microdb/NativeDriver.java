package com.devsmart.microdb;


import com.devsmart.microdb.ubjson.UBValue;

public class NativeDriver implements Driver {

    static {
        System.loadLibrary("libmicrodb");
        System.loadLibrary("libmicrodb-jni");
    }

    @Override
    public UBValue get(UBValue key) {
        return null;
    }

    @Override
    public void put(UBValue key, UBValue value) {

    }
}
