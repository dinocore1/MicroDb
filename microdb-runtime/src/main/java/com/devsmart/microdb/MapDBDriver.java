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

        if (mMapDB.exists("metadata")) {
            mMetadata = mMapDB.getAtomicVar("metadata");
        } else {
            Atomic.Var<? extends UBValue> metadata = mMapDB.createAtomicVar("metadata", UBValueFactory.createObject(), SERIALIZER_UBVALUE);
            mMetadata = (Atomic.Var<UBObject>) metadata;
        }
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
    public UUID genId() {
        UUID key = UUID.randomUUID();
        while (mObjects.containsKey(key)) {
            key = UUID.randomUUID();
        }
        return key;
    }

    @Override
    public void insert(UUID id, UBValue value) throws IOException {

        for (ChangeListener l : mChangeListeners) {
            l.onBeforeInsert(this, value);
        }

        mObjects.put(id, value);

        for (ChangeListener l : mChangeListeners) {
            l.onAfterInsert(this, id, value);
        }

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

        for (ChangeListener l : mChangeListeners) {
            l.onBeforeDelete(this, key);
        }

        mObjects.remove(key);
    }

    private static final UUID MAX_UUID = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
    private static final UUID MIN_UUID = new UUID(Long.MIN_VALUE, Long.MIN_VALUE);

    @Override
    public <T extends Comparable<T>> Cursor queryIndex(String indexName, T min, boolean minInclusive, T max, boolean maxInclusive) throws IOException {
        MapDBCursor<T> retval = new MapDBCursor<T>();
        retval.mDriver = this;
        retval.index = mMapDB.getTreeSet(indexName);

        if (max != null && min != null) {
            retval.min = Fun.t2(min, minInclusive ? MIN_UUID : MAX_UUID);
            retval.max = Fun.t2(max, maxInclusive ? MAX_UUID : MIN_UUID);

        } else if (min != null && max == null) {
            retval.min = Fun.t2(min, minInclusive ? MIN_UUID : MAX_UUID);

        } else if (min == null && max != null) {
            retval.max = (Fun.Tuple2<T, UUID>) Fun.t2(max, maxInclusive ? MAX_UUID : MIN_UUID);
        }

        retval.seekToBegining();

        return retval;
    }

    private static class MapDBCursor<T extends Comparable<T>> implements Cursor {

        MapDBDriver mDriver;
        NavigableSet<Fun.Tuple2<T, UUID>> index;
        Fun.Tuple2<T, UUID> min;
        Fun.Tuple2<T, UUID> max;
        private Fun.Tuple2<T, UUID> mCurrentValue;


        @Override
        public void seekToBegining() {
            if(min != null) {
                mCurrentValue = index.ceiling(min);
            } else {
                mCurrentValue = index.first();
            }
        }

        @Override
        public void seekToEnd() {
            if(max != null) {
                mCurrentValue = index.floor(max);
            } else {
                mCurrentValue = index.last();
            }
        }

        @Override
        public boolean next() {
            mCurrentValue = index.higher(mCurrentValue);
            return mCurrentValue != null && (max != null && mCurrentValue.compareTo(max) <= 0);
        }

        @Override
        public boolean prev() {
            mCurrentValue = index.lower(mCurrentValue);
            return mCurrentValue != null && (min != null && mCurrentValue.compareTo(min) >= 0);
        }

        @Override
        public Row get() {
            return new MapDBRow<T>(mDriver, mCurrentValue);
        }
    }

    private static class MapDBRow<T extends Comparable<T>> implements Row {

        private final MapDBDriver mDriver;
        final Fun.Tuple2<T, UUID> mTuple;
        UBValue mValue;

        public MapDBRow(MapDBDriver driver, Fun.Tuple2<T, UUID> tuple) {
            mDriver = driver;
            mTuple = tuple;
        }

        @Override
        public UUID getPrimaryKey() {
            return mTuple.b;
        }

        @Override
        public T getSecondaryKey() {
            return mTuple.a;
        }

        @Override
        public UBValue getValue() {
            if (mValue == null) {
                try {
                    mValue = mDriver.get(getPrimaryKey());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return mValue;
        }
    }

    @Override
    public <T extends Comparable<T>> void addIndex(String indexName, final MapFunction<T> mapFunction) throws IOException {
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

    private static class MapDBEmitter<T extends Comparable<T>> implements Emitter<T> {

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
        mMapDB.commit();
    }

    @Override
    public void rollbackTransaction() throws IOException {
        mMapDB.rollback();
    }
}
