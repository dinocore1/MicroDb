package com.devsmart.microdb;


import com.devsmart.microdb.ubjson.UBReader;
import com.devsmart.microdb.ubjson.UBValue;
import com.devsmart.microdb.ubjson.UBWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class NativeDriver implements Driver {

    static {
        System.loadLibrary("microdb-jni");
    }


    private long mNativePtr;
    private native static boolean open(String dbpath, NativeDriver driver);
    public native void close();

    private native byte[] load(byte[] key);
    private native byte[] save(byte[] data);

    private NativeDriver() {}

    public static NativeDriver open(String dbpath) throws IOException {
        NativeDriver driver = new NativeDriver();
        if(!open(dbpath, driver)){
            throw new IOException("could not open db with path: " + dbpath);
        }
        return driver;
    }

    private static byte[] toByteArray(UBValue value) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UBWriter writer = new UBWriter(out);
        writer.write(value);
        writer.close();
        return out.toByteArray();
    }

    private static UBValue fromByteArray(byte[] data) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        UBReader reader = new UBReader(in);
        return reader.read();
    }




    @Override
    public UBValue load(UBValue key) throws IOException {
        byte[] keyData = toByteArray(key);
        byte[] valueData = load(keyData);
        return fromByteArray(valueData);
    }

    @Override
    public UBValue save(UBValue data) throws IOException {
        return null;
    }

    @Override
    public void delete(UBValue key) throws IOException {

    }

    @Override
    public DBIterator queryIndex(String indexName) throws IOException {
        return null;
    }

    @Override
    public void addIndex(String indexName, String indexQuery) throws IOException {

    }

    @Override
    public void deleteIndex(String indexName) {

    }
}
