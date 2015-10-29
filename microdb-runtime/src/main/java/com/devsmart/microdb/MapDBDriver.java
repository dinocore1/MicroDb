package com.devsmart.microdb;


import com.devsmart.ubjson.*;
import org.mapdb.*;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

public class MapDBDriver implements Driver {

    private final DB mMapDB;
    private final Atomic.Var<UBValue> mMetadata;
    private BTreeMap<UUID, UBValue> mObjects;


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

        mMetadata = mMapDB.createAtomicVar("metadata", UBValueFactory.createObject(), SERIALIZER_UBVALUE);
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
    public UUID insert(UBValue value) throws IOException {
        UUID key = UUID.randomUUID();
        while(mObjects.containsKey(key)) {
            key = UUID.randomUUID();
        }

        mObjects.put(key, value);
        return key;
    }

    @Override
    public void update(UUID id, UBValue value) throws IOException {
        mObjects.put(id, value);
    }

    @Override
    public void delete(UUID key) throws IOException {
        mObjects.remove(key);
    }

    @Override
    public <T extends Comparable<?>> KeyIterator<T> queryIndex(String indexName) throws IOException {

        NavigableSet<Fun.Tuple2<T, UUID>> index = mMapDB.getTreeSet(indexName);
        return new MapDBKeyIterator<T>(index);
    }

    private static class MapDBKeyIterator<T extends Comparable<?>> implements KeyIterator<T> {

        private static final UUID MAX_UUID = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
        private static final UUID MIN_UUID = new UUID(Long.MIN_VALUE, Long.MIN_VALUE);

        private final NavigableSet<Fun.Tuple2<T, UUID>> mIndex;
        private Fun.Tuple2<T, UUID> mNextValue;


        MapDBKeyIterator(NavigableSet<Fun.Tuple2<T, UUID>> index) {
            mIndex = index;
            mNextValue = mIndex.first();
        }

        @Override
        public UUID getPrimaryKey() {
            return mNextValue.b;
        }

        @Override
        public void seekTo(T key) {
            mNextValue = mIndex.ceiling(new Fun.Tuple2<T, UUID>(key, null));
        }

        @Override
        public boolean hasNext() {
            return mNextValue != null;
        }

        @Override
        public T next() {
            Fun.Tuple2<T, UUID> retval = mNextValue;
            mNextValue = mIndex.higher(mNextValue);
            return retval.a;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public <T extends Comparable<?>> void addIndex(String indexName, final MapFunction<T> mapFunction) throws IOException {
        NavigableSet<Fun.Tuple2<T, UUID>> index = mMapDB.createTreeSet(indexName)
                .makeOrGet();

        Bind.secondaryKeys(mObjects, index, new Fun.Function2<T[], UUID, UBValue>() {

            MapDBEmitter<T> mEmitter = new MapDBEmitter<T>();

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
    public void beginTransaction() throws IOException {

    }

    @Override
    public void commitTransaction() throws IOException {

    }

    @Override
    public void rollbackTransaction() throws IOException {

    }
}
