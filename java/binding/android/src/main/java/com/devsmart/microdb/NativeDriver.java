package com.devsmart.microdb;


import com.devsmart.microdb.ubjson.UBObject;
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

    private native byte[] get(byte[] key);
    private native byte[] insert(byte[] data);
    private native void delete(byte[] key);
    private native void queryIndex(String indexName, NativeIterator it);

    private NativeDriver() {}

    public static NativeDriver open(String dbpath) throws IOException {
        NativeDriver driver = new NativeDriver();
        if(!open(dbpath, driver)){
            throw new IOException("could not open db with path: " + dbpath);
        }
        return driver;
    }

    static byte[] toByteArray(UBValue value) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UBWriter writer = new UBWriter(out);
        writer.write(value);
        writer.close();
        return out.toByteArray();
    }

    static UBValue fromByteArray(byte[] data) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        UBReader reader = new UBReader(in);
        return reader.read();
    }

    @Override
    public UBValue get(UBValue key) throws IOException {
        byte[] data = get(toByteArray(key));
        UBValue retval = null;
        if(data != null) {
            retval = fromByteArray(data);
        }
        return retval;
    }

    @Override
    public UBValue insert(UBValue value) throws IOException {
        byte[] keydata = insert(toByteArray(value));
        return fromByteArray(keydata);
    }

    @Override
    public void delete(UBValue key) throws IOException {
        delete(toByteArray(key));

    }

    @Override
    public DBIterator queryIndex(String indexName) {
        NativeIterator retval = new NativeIterator();
        queryIndex(indexName, retval);
        return retval;
    }

    @Override
    public native void addIndex(String indexName, String indexQuery) throws IOException;

    @Override
    public native void deleteIndex(String indexName);
}
