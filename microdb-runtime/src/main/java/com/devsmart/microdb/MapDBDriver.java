package com.devsmart.microdb;


import com.devsmart.ubjson.*;
import org.mapdb.*;

import java.io.*;
import java.util.*;

public class MapDBDriver implements Driver {

    final DB mMapDB;
    final Atomic.Var<UBObject> mMetadata;
    BTreeMap<UUID, UBValue> mObjects;
    private ArrayList<ChangeListener> mChangeListeners = new ArrayList<ChangeListener>();


    public static class UBValueSerializer implements Serializer<UBValue>, Serializable {

        @Override
        public void serialize(DataOutput out, UBValue value) throws IOException {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            UBWriter writer = new UBWriter(bout);
            writer.write(value);
            writer.close();

            byte[] buff = bout.toByteArray();
            out.writeInt(buff.length);
            out.write(buff);
        }

        @Override
        public UBValue deserialize(DataInput in, int available) throws IOException {
            final int size = in.readInt();
            byte[] buff = new byte[size];
            in.readFully(buff);

            UBReader reader = new UBReader(new ByteArrayInputStream(buff));
            UBValue retval = reader.read();
            reader.close();
            return retval;
        }

        @Override
        public int fixedSize() {
            return -1;
        }
    }

    public static final Serializer<UBValue> SERIALIZER_UBVALUE = new UBValueSerializer();

    public MapDBDriver(DB mapdb) {
        mMapDB = mapdb;
        mObjects = mMapDB.createTreeMap("objects")
                .keySerializerWrap(Serializer.UUID)
                .valueSerializer(SERIALIZER_UBVALUE)
                .valuesOutsideNodesEnable()
                .comparator(BTreeMap.COMPARABLE_COMPARATOR)
                .makeOrGet();

        Atomic.Var<? extends UBValue> metadata = mMapDB.createAtomicVar("metadata", UBValueFactory.createObject(), SERIALIZER_UBVALUE);
        mMetadata = (Atomic.Var<UBObject>)metadata;
    }

    @Override
    public void close() {
        mMapDB.close();
    }

    @Override
    public UBObject getMeta() throws IOException {
        return mMetadata.get().asObject();
    }

    @Override
    public void saveMeta(UBObject obj) throws IOException {
        mMetadata.set(obj);
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        mChangeListeners.add(changeListener);
    }


    @Override
    public UBValue get(UUID key) throws IOException {
        return mObjects.get(key);
    }

    @Override
    public UUID insert(UBValue value) throws IOException {

        for(ChangeListener l : mChangeListeners) {
            l.onBeforeInsert(this, value);
        }

        UUID key = UUID.randomUUID();
        while(mObjects.containsKey(key)) {
            key = UUID.randomUUID();
        }

        mObjects.put(key, value);

        for(ChangeListener l : mChangeListeners) {
            l.onAfterInsert(this, key, value);
        }

        return key;
    }

    @Override
    public void update(UUID id, UBValue value) throws IOException {

        for (ChangeListener l : mChangeListeners) {
            l.onBeforeUpdate(this, id, value);
        }

        mObjects.put(id, value);
    }

    @Override
    public void delete(UUID key) throws IOException {

        for(ChangeListener l : mChangeListeners) {
            l.onBeforeDelete(this, key);
        }

        mObjects.remove(key);
    }

    @Override
    public <T extends Comparable<?>> Cursor<T> queryIndex(String indexName) throws IOException {

        NavigableSet<Fun.Tuple2<T, UUID>> index = mMapDB.getTreeSet(indexName);
        return new MapDBKeyIterator<T>(index);
    }

    private static class MapDBKeyIterator<K extends Comparable<K>> implements Cursor<K> {

        private static final UUID MAX_UUID = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
        private static final UUID MIN_UUID = new UUID(Long.MIN_VALUE, Long.MIN_VALUE);

        private final MapDBDriver mDriver;
        private final NavigableSet<Fun.Tuple2<K, UUID>> mIndex;
        private long mPosition = -1;
        private Fun.Tuple2<K, UUID> mRow;


        MapDBKeyIterator(MapDBDriver driver, NavigableSet<Fun.Tuple2<K, UUID>> index) {
            mDriver = driver;
            mIndex = index;
        }

        @Override
        public boolean isBeforeFirst() {
            return mPosition == -1;
        }

        @Override
        public boolean isAfterLast() {
            return mPosition == -2;
        }

        @Override
        public boolean isFirst() {
            return mPosition == 0;
        }

        @Override
        public boolean isLast() {
            return mIndex.higher(mRow) == null;
        }

        @Override
        public boolean moveToFirst() {
            mRow = mIndex.first();
            mPosition = 0;
            return mRow != null;
        }

        @Override
        public boolean moveToLast() {
            mRow = mIndex.last();
            mPosition = mIndex.size() - 1;
            return mRow != null;
        }

        @Override
        public boolean moveToNext() {
            if(isBeforeFirst()) {
                return moveToFirst();
            } else if(isAfterLast(){
                return false;
            } else {
                mRow = mIndex.higher(mRow);
                boolean retval = mRow != null;
                if(retval) {
                    mPosition++;
                } else {
                    mPosition = -2;
                }
                return retval;
            }
        }

        @Override
        public boolean moveToPrevious() {
            if(isAfterLast()){
                return moveToLast();
            } else if(isBeforeFirst()) {
                return false;
            } else {
                mRow = mIndex.lower(mRow);
                boolean retval = mRow != null;
                if(retval) {
                    mPosition--;
                } else {
                    mPosition = -1;
                }
                return retval;
            }
        }

        @Override
        public UUID getPrimaryKey() {
            return mRow.b;
        }

        @Override
        public K getSecondaryKey() {
            return mRow.a;
        }

        @Override
        public UBValue getValue() {
            try {
                return mDriver.get(getPrimaryKey());
            } catch (IOException e) {
                logger.error("", e);
            }
        }

    }

    @Override
    public <T extends Comparable<?>> void addIndex(String indexName, final MapFunction<T> mapFunction) throws IOException {
        NavigableSet<Fun.Tuple2<T, UUID>> index = mMapDB.createTreeSet(indexName)
                .makeOrGet();

        Bind.secondaryKeys(mObjects, index, new Fun.Function2<T[], UUID, UBValue>() {

            final MapDBEmitter<T> mEmitter = new MapDBEmitter<T>();

            @Override
            public T[] run(UUID uuid, UBValue ubValue) {
                synchronized (mEmitter) {
                    mEmitter.clear();
                    mapFunction.map(ubValue, mEmitter);
                    return mEmitter.getKeys();
                }
            }
        });
    }

    private static class MapDBEmitter<T extends Comparable<?>> implements Emitter<T> {

        ArrayList<T> mKeys = new ArrayList<T>(3);

        public void clear() {
            mKeys.clear();
        }


        @Override
        public void emit(T key) {
            mKeys.add(key);
        }


        public T[] getKeys() {
            T[] retval = (T[]) new Comparable[mKeys.size()];
            retval = mKeys.toArray(retval);
            return retval;
        }
    }

    @Override
    public void deleteIndex(String indexName) {
        mMapDB.delete(indexName);

    }

    @Override
    public long incrementLongField(String fieldName) {
        return mMapDB.getAtomicLong(fieldName).getAndIncrement();
    }

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
