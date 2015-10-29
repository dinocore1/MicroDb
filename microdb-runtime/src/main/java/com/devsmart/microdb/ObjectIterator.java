package com.devsmart.microdb;

import java.util.Iterator;
import java.util.UUID;


public class ObjectIterator<K extends Comparable<?>, T extends DBObject> implements Iterator<DBObject> {

    private final KeyIterator<K> mKeyIterator;
    private final MicroDB mDb;
    private final Class<T> mClassType;

    public ObjectIterator(KeyIterator<K> keyIterator, MicroDB db, Class<T> classType) {
        mKeyIterator = keyIterator;
        mDb = db;
        mClassType = classType;
    }

    public void seekTo(K key) {
        mKeyIterator.seekTo(key);
    }

    @Override
    public boolean hasNext() {
        return mKeyIterator.hasNext();
    }

    @Override
    public DBObject next() {
        mKeyIterator.next();
        UUID id = mKeyIterator.getPrimaryKey();
        return mDb.get(id, mClassType);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
