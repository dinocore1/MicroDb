package com.devsmart.microdb;


import com.devsmart.microdb.ubjson.UBValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class NativeIterator implements DBIterator {

    private static final Logger logger = LoggerFactory.getLogger(NativeIterator.class);

    private long mNativePtr;

    private native byte[] key();
    private native byte[] primaryKey();
    private native byte[] value();
    private native void destroy();
    private native void seek(byte[] data);

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    @Override
    public void seekTo(UBValue key) {
        try {
            byte[] data = NativeDriver.toByteArray(key);
            seek(data);
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    @Override
    public native boolean valid();

    @Override
    public native void next();

    @Override
    public native void prev();

    @Override
    public UBValue getKey() {
        try {
            byte[] data = key();
            return NativeDriver.fromByteArray(data);
        } catch (IOException e) {
            logger.error("", e);
            return null;
        }
    }

    @Override
    public UBValue getPrimaryKey() {
        try {
            byte[] data = primaryKey();
            return NativeDriver.fromByteArray(data);
        } catch (IOException e) {
            logger.error("", e);
            return null;
        }
    }

    @Override
    public UBValue getValue() {
        try {
            byte[] data = value();
            return NativeDriver.fromByteArray(data);
        } catch (IOException e) {
            logger.error("", e);
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        if(mNativePtr != 0) {
            destroy();
        }

    }
}
