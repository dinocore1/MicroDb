package com.devsmart.microdb;


import com.devsmart.ubjson.*;
import org.mapdb.*;

import java.io.*;
import java.util.*;

public class MapDBDriver implements Driver {

    final DB mMapDB;
    final Atomic.Var<UBObject> mMetadata;
    BTreeMap<UUID, UBValue> mObjects;

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
        mObjects.put(id, value);
    }

    @Override
    public void update(UUID id, UBValue value) throws IOException {
        mObjects.put(id, value);
    }

    @Override
    public void delete(UUID key) throws IOException {
        mObjects.remove(key);
    }

    private static final UUID MAX_UUID = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
    private static final UUID MIN_UUID = new UUID(Long.MIN_VALUE, Long.MIN_VALUE);

    @Override
    public <T extends Comparable<T>> Cursor queryIndex(String indexName, T min, boolean minInclusive, T max, boolean maxInclusive) throws IOException {
        MapDBCursor<T> retval = new MapDBCursor<T>();
        retval.mDriver = this;

        NavigableSet<Fun.Tuple2<T, UUID>> index = mMapDB.getTreeSet(indexName);

        if (max != null && min != null) {
            retval.min = Fun.t2(min, minInclusive ? MIN_UUID : MAX_UUID);
            retval.max = Fun.t2(max, maxInclusive ? MAX_UUID : MIN_UUID);
            retval.index = index.subSet(retval.min, minInclusive, retval.max, maxInclusive);

        } else if (min != null && max == null) {
            retval.min = Fun.t2(min, minInclusive ? MIN_UUID : MAX_UUID);
            retval.index = index.tailSet(retval.min, minInclusive);

        } else if (min == null && max != null) {
            retval.max = Fun.t2(max, maxInclusive ? MAX_UUID : MIN_UUID);
            retval.index = index.headSet(retval.max, maxInclusive);
        } else {
            retval.index = index;
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
        private int mPosition;


        @Override
        public void seekToBegining() {
            if(!index.isEmpty()) {
                mCurrentValue = index.first();
                mPosition = 0;
            }
        }

        @Override
        public void seekToEnd() {
            mCurrentValue = index.last();
            mPosition = getCount();
        }

        @Override
        public int getPosition() {
            return mPosition;
        }

        @Override
        public boolean moveToPosition(int pos) {
            int currentPos;
            while( (currentPos = getPosition()) != pos) {
              if(currentPos < pos) {
                  next();
              } else {
                  prev();
              }
            }
            return true;
        }

        @Override
        public boolean next() {
            mCurrentValue = index.higher(mCurrentValue);
            mPosition++;
            return mCurrentValue != null;
        }

        @Override
        public boolean prev() {
            mCurrentValue = index.lower(mCurrentValue);
            mPosition--;
            return mCurrentValue != null;
        }

        @Override
        public Row get() {
            if(mCurrentValue == null) {
                return null;
            } else {
                return new MapDBRow<T>(mDriver, mCurrentValue);
            }
        }

        @Override
        public int getCount() {
            return index.size();
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
            if(mKeys.isEmpty()) {
                return null;
            }
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
