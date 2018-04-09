package com.devsmart.microdb;


import com.devsmart.ubjson.*;
import com.google.common.reflect.Reflection;
import org.mapdb.*;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class MapDBDriver implements Driver {

    DB mMapDB;
    Atomic.Var<UBObject> mMetadata;
    BTreeMap<UUID, UBValue> mObjects;
    private Map<String, IndexObject> mIndicies = new HashMap<String, IndexObject>();

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
            byte[] buff = new byte[(available-4)];
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
        init(mapdb);
    }

    private void init(DB mapdb) {
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

    public DB getDB() {
        return mMapDB;
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
        NavigableSet<Fun.Tuple2<T, UUID>> index = mMapDB.getTreeSet(indexName);

        if (max != null && min != null) {
            index = index.subSet(Fun.t2(min, minInclusive ? MIN_UUID : MAX_UUID), minInclusive,
                    Fun.t2(max, maxInclusive ? MAX_UUID : MIN_UUID), maxInclusive);

        } else if (min != null && max == null) {
            index = index.tailSet(Fun.t2(min, minInclusive ? MIN_UUID : MAX_UUID), minInclusive);

        } else if (min == null && max != null) {
            index = index.headSet(Fun.t2(max, maxInclusive ? MAX_UUID : MIN_UUID), maxInclusive);
        }

        return new MapDBCursor<T>(this, index);
    }

    private static class MapDBCursor<T extends Comparable<T>> implements Cursor {

        static final int BEFORE_FIRST = -1;
        static final int AFTER_LAST = -2;

        private final MapDBDriver mDriver;
        private final NavigableSet<Fun.Tuple2<T, UUID>> mIndex;
        private int mPosition;
        private Fun.Tuple2<T, UUID> mCurrentValue;

        MapDBCursor(MapDBDriver driver, NavigableSet<Fun.Tuple2<T, UUID>> index) {
            mDriver = driver;
            mIndex = index;
            mPosition = BEFORE_FIRST;
        }


        @Override
        public boolean moveToFirst() {
            if(!mIndex.isEmpty()) {
                mPosition = 0;
                mCurrentValue = mIndex.first();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean moveToLast() {
            if(!mIndex.isEmpty()) {
                mPosition = getCount() - 1;
                mCurrentValue = mIndex.last();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean moveToNext() {
            if(mPosition == BEFORE_FIRST) {
                mCurrentValue = mIndex.first();
                mPosition = 0;
                return true;
            } else if(mPosition == AFTER_LAST) {
                return false;
            } else {
                try {
                    mCurrentValue = mIndex.higher(mCurrentValue);
                    if(mCurrentValue == null) {
                        return false;
                    }
                    mPosition++;
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }

        @Override
        public boolean moveToPrevious() {
            if(mPosition == AFTER_LAST) {
                mCurrentValue = mIndex.last();
                mPosition = getCount() - 1;
                return true;
            } else if(mPosition == BEFORE_FIRST) {
                return false;
            } else {
                try {
                    mCurrentValue = mIndex.lower(mCurrentValue);
                    if(mCurrentValue == null) {
                        return false;
                    }
                    mPosition--;
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }

        @Override
        public boolean move(int pos) {
            return false;
        }

        @Override
        public boolean isFirst() {
            return mPosition == 0;
        }

        @Override
        public boolean isLast() {
            return mPosition == getCount() - 1;
        }

        @Override
        public boolean isBeforeFirst() {
            return mPosition == BEFORE_FIRST;
        }

        @Override
        public boolean isAfterLast() {
            return mPosition == AFTER_LAST;
        }

        @Override
        public int getPosition() {
            return mPosition;
        }

        @Override
        public int getCount() {
            return mIndex.size();
        }

        @Override
        public Row getRow() {
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

    private class IndexObject<T extends Comparable<T>> {
        public final String name;
        MapFunction<T> mapFunction;

        private Bind.MapListener mListener;

        public IndexObject(String name, final MapFunction<T> mapFunction) {
            this.name = name;
            this.mapFunction = mapFunction;
        }

        void install() {
            if(mListener != null) {
                mObjects.modificationListenerRemove(mListener);
            }

            NavigableSet<Fun.Tuple2<T, UUID>> index = mMapDB.createTreeSet(name)
                    .makeOrGet();

            Fun.Function2<T[], UUID, UBValue> microDBMapFunction = new Fun.Function2<T[], UUID, UBValue>() {

                final MapDBEmitter<T> mEmitter = new MapDBEmitter<T>();

                @Override
                public T[] run(UUID uuid, UBValue ubValue) {
                    synchronized (mEmitter) {
                        mEmitter.clear();
                        mapFunction.map(ubValue, mEmitter);
                        return mEmitter.getKeys();
                    }
                }
            };

            mListener = createIndexListener(mObjects, index, microDBMapFunction);
            mObjects.modificationListenerAdd(mListener);
        }

        void reindex() {
            NavigableSet<Fun.Tuple2<T, UUID>> index = mMapDB.createTreeSet(name)
                    .makeOrGet();
            index.clear();

            Fun.Function2<T[], UUID, UBValue> fun = createMapDBFunction();

            if(index.isEmpty()){
                for(Map.Entry<UUID,UBValue> e:mObjects.entrySet()){
                    T[] k2 = fun.run(e.getKey(), e.getValue());
                    if(k2 != null)
                        for(T k22 :k2)
                            index.add(Fun.t2(k22, e.getKey()));
                }
            }
        }

        private Fun.Function2<T[], UUID, UBValue> createMapDBFunction() {
            return new Fun.Function2<T[], UUID, UBValue>() {

                final MapDBEmitter<T> mEmitter = new MapDBEmitter<T>();

                @Override
                public T[] run(UUID uuid, UBValue ubValue) {
                    synchronized (mEmitter) {
                        mEmitter.clear();
                        mapFunction.map(ubValue, mEmitter);
                        return mEmitter.getKeys();
                    }
                }
            };
        }

        private <K,V, K2> Bind.MapListener<K, V> createIndexListener(Bind.MapWithModificationListener<K, V> map,
                                                                     final Set<Fun.Tuple2<K2, K>> secondary,
                                                                     final Fun.Function2<K2[], K, V> fun) {
            return new Bind.MapListener<K, V>() {
                @Override
                public void update(K key, V oldVal, V newVal) {
                    if (newVal == null) {
                        //removal
                        K2[] k2 = fun.run(key, oldVal);
                        if (k2 != null)
                            for (K2 k22 : k2)
                                secondary.remove(Fun.t2(k22, key));
                    } else if (oldVal == null) {
                        //insert
                        K2[] k2 = fun.run(key, newVal);
                        if (k2 != null)
                            for (K2 k22 : k2)
                                secondary.add(Fun.t2(k22, key));
                    } else {
                        //update, must remove old key and insert new
                        K2[] oldk = fun.run(key, oldVal);
                        K2[] newk = fun.run(key, newVal);
                        if (oldk == null) {
                            //insert new
                            if (newk != null)
                                for (K2 k22 : newk)
                                    secondary.add(Fun.t2(k22, key));
                            return;
                        }
                        if (newk == null) {
                            //remove old
                            for (K2 k22 : oldk)
                                secondary.remove(Fun.t2(k22, key));
                            return;
                        }

                        Set<K2> hashes = new HashSet<K2>();
                        Collections.addAll(hashes, oldk);

                        //add new non existing items
                        for (K2 k2 : newk) {
                            if (!hashes.contains(k2)) {
                                secondary.add(Fun.t2(k2, key));
                            }
                        }
                        //remove items which are in old, but not in new
                        for (K2 k2 : newk) {
                            hashes.remove(k2);
                        }
                        for (K2 k2 : hashes) {
                            secondary.remove(Fun.t2(k2, key));
                        }
                    }
                }
            };
        }
    }

    @Override
    public <T extends Comparable<T>> void addIndex(String indexName, final MapFunction<T> mapFunction) throws IOException {
        synchronized (mIndicies) {
            IndexObject index = mIndicies.get(indexName);
            if (index == null) {
                index = new IndexObject(indexName, mapFunction);
                mIndicies.put(indexName, index);
                index.install();
            }
        }
    }

    @Override
    public void recomputeIndex(String indexName) {
        synchronized (mIndicies) {
            IndexObject index = mIndicies.get(indexName);
            if (index != null) {
                index.reindex();
            }
        }
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

    @Override
    public void compact() throws IOException {

        try {
            mMapDB.commit();
            Store store = Store.forDB(mMapDB);

            if(!(store instanceof StoreDirect)) {
                store.compact();
                return;
            }

            File indexFile;
            File physFile;

            Field indexField = StoreDirect.class.getDeclaredField("index");
            indexField.setAccessible(true);
            Field physField = StoreDirect.class.getDeclaredField("phys");
            physField.setAccessible(true);

            indexFile = ((Volume) indexField.get(store)).getFile();
            physFile = ((Volume) physField.get(store)).getFile();

            final File compactFile = new File(indexFile.getPath() + ".comp2" );
            if(compactFile.exists()) {
                compactFile.delete();
            }

            {
                File compactPhysicalFile = new File(indexFile.getPath() + ".comp2.p");
                if(compactPhysicalFile.exists()) {
                    compactPhysicalFile.delete();
                }
            }


            DB newDB = DBMaker.newFileDB(compactFile)
                    .transactionDisable()
                    .make();


            BTreeMap newObject = newDB.createTreeMap("objects")
                    .keySerializerWrap(Serializer.UUID)
                    .valueSerializer(SERIALIZER_UBVALUE)
                    .valuesOutsideNodesEnable()
                    .comparator(BTreeMap.COMPARABLE_COMPARATOR)
                    .makeOrGet();

            ArrayList<AddToIndex> indicesFun = new ArrayList<AddToIndex>();
            for (Map.Entry<String, IndexObject> entry : mIndicies.entrySet()) {
                NavigableSet<Fun.Tuple2<?, UUID>> index = newDB.createTreeSet(entry.getKey()).makeOrGet();
                indicesFun.add(new AddToIndex(entry.getValue(), index));
            }

            newDB.createAtomicVar("metadata", mMetadata.get(), SERIALIZER_UBVALUE);
            for(Map.Entry<String, Object> entry : mMapDB.getAll().entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();
                if(value instanceof Atomic.Long) {
                    newDB.createAtomicLong(name, ((Atomic.Long)value).longValue());
                }
            }

            for (Map.Entry<UUID, UBValue> entry : mObjects.entrySet()) {
                UUID key = entry.getKey();
                UBValue value = entry.getValue();
                newObject.put(key, value);
                for (AddToIndex addToIndex : indicesFun) {
                    addToIndex.index(key, value);
                }
            }

            File indexFile2 = ((Volume)indexField.get(Store.forDB(newDB))).getFile();
            File physFile2 = ((Volume)physField.get(Store.forDB(newDB))).getFile();

            newDB.close();
            mMapDB.close();

            com.google.common.io.Files.move(physFile2, physFile);
            com.google.common.io.Files.move(indexFile2, indexFile);
            //Files.move(physFile2.toPath(), physFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            //Files.move(indexFile2.toPath(), indexFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

            init(DBMaker.newFileDB(indexFile).make());

            for(IndexObject indexObject : mIndicies.values()) {
                indexObject.install();
            }

        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private static class AddToIndex {

        private final NavigableSet<Fun.Tuple2<?, UUID>> mIndex;
        private final Fun.Function2<Object[], UUID, UBValue> mapFun;

        public AddToIndex(IndexObject indexObject, NavigableSet<Fun.Tuple2<?, UUID>> index) {
            mIndex = index;
            mapFun = indexObject.createMapDBFunction();
        }

        public void index(UUID uuid, UBValue value) {
            Object[] k2 = mapFun.run(uuid, value);
            if(k2 != null) {
                for(Object k22 : k2) {
                    mIndex.add(Fun.t2(k22, uuid));
                }
            }
        }
    }

}
