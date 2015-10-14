package com.devsmart.microdb;


import com.devsmart.ubjson.UBValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

public class NativeIterator implements DBIterator {

    private static final Logger logger = LoggerFactory.getLogger(NativeIterator.class);
    private final StackTraceElement[] mCreateStackTrace;

    private long mNativePtr;

    private native byte[] key();
    private native byte[] primaryKey();
    private native byte[] value();
    private native void destroy();
    private native void seek(byte[] data);

    NativeIterator() {
        mNativePtr = 0;
        mCreateStackTrace = Thread.currentThread().getStackTrace();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (mNativePtr != 0) {
                logger.warn("Iterator not closed. {}", Arrays.toString(mCreateStackTrace));
                close();
            }
        } finally {
            super.finalize();
        }
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
