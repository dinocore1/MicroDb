package com.devsmart.microdb;


import com.devsmart.IOUtils;
import com.devsmart.microdb.ubjson.UBReader;
import com.devsmart.microdb.ubjson.UBValue;
import com.devsmart.microdb.ubjson.UBWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class NativeDriver implements Driver {

    private static final Logger logger = LoggerFactory.getLogger(NativeDriver.class);
    private static final String OS_NAME = System.getProperty("os.name");


    private static boolean isWindows() {
        return OS_NAME.contains("Windows");
    }

    private static boolean isLinux() {
        return OS_NAME.contains("Linux") || OS_NAME.contains("LINUX");
    }

    private static void loadNativeLibFromResources(String resourcePath) throws IOException {
        File tmp = File.createTempFile("lib", null);
        tmp.deleteOnExit();
        IOUtils.pump(NativeDriver.class.getResourceAsStream(resourcePath), new FileOutputStream(tmp));
        System.loadLibrary(tmp.getAbsolutePath());
    }

    static {
        try {
            if (isWindows()) {
                loadNativeLibFromResources("/microdb-jni-windows.dll");
            } else {
                loadNativeLibFromResources("/microdb-jni-linux.so");
            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }


    private long mNativePtr;
    private native static boolean open(String dbpath, NativeDriver driver);
    public native void close();

    private native byte[] get(byte[] key);
    private native byte[] insert(byte[] data);
    private native void delete(byte[] key);
    private native boolean queryIndex(String indexName, NativeIterator it);

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
        if(queryIndex(indexName, retval)){
            return retval;
        } else {
            return null;
        }
    }

    @Override
    public native boolean addIndex(String indexName, String indexQuery) throws IOException;

    @Override
    public native void deleteIndex(String indexName);

    @Override
    public void beginTransaction() throws IOException {

    }

    @Override
    public void commitTransaction() throws IOException {

    }

    @Override
    public void rollbackTransaction() throws IOException {

    }
}
