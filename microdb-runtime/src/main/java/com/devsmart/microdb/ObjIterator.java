package com.devsmart.microdb;


import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;

import java.io.Closeable;
import java.io.IOException;

public class ObjIterator<T extends DBObject> implements Closeable {


    private final String mClassTypeName;
    private final DBIterator mIterator;
    private final MicroDB mMicroDB;
    private final Class<T> mClassType;

    ObjIterator(DBIterator iterator, MicroDB db, Class<T> classType) {
        mIterator = iterator;
        mMicroDB = db;
        mClassTypeName = classType.getSimpleName();
        mClassType = classType;
        iterator.seekTo(UBValueFactory.createString(mClassTypeName));
    }

    public boolean valid() {
        UBValue key;
        boolean retval = mIterator.valid();
        if(retval) {
            key = mIterator.getKey();
            retval = key.isString();
            if (retval) {
                retval = mClassTypeName.equals(key.asString());
            }
        }
        return retval;
    }

    public void next() {
        mIterator.next();

    }

    public void prev() {
        mIterator.prev();

    }

    public T get() {
        UBValue primaryKey = mIterator.getPrimaryKey();
        T retval = mMicroDB.get(primaryKey, mClassType);
        return retval;
    }

    @Override
    public void close() throws IOException {
        mIterator.close();
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
