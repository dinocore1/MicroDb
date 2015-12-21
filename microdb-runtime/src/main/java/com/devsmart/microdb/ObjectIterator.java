package com.devsmart.microdb;

import java.util.Iterator;
import java.util.UUID;


public class ObjectIterator<K extends Comparable<?>, T extends DBObject> implements Iterator<T> {

    final Cursor<K> mCursor;
    final MicroDB mDb;
    final Class<T> mClassType;

    public ObjectIterator(Cursor<K> cursor, MicroDB db, Class<T> classType) {
        mCursor = cursor;
        mDb = db;
        mClassType = classType;
    }

    public void seekTo(K key) {
        mCursor.seekTo(key);
    }

    @Override
    public boolean hasNext() {
        return mCursor.hasNext();
    }

    @Override
    public T next() {
        mCursor.next();
        UUID id = mCursor.getPrimaryKey();
        return mDb.get(id, mClassType);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
