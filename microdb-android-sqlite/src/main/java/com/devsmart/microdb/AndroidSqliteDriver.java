package com.devsmart.microdb;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBReader;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;
import com.devsmart.ubjson.UBWriter;
import com.google.common.base.Throwables;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Callable;

public class AndroidSqliteDriver implements Driver {

    private static final String KEY_META = "microdb-meta";

    static class TableObjects {
        final static String NAME = "Objects";
        final static String COLUMN_ID = "id";
        final static String COLUMN_DATA = "data";
    }

    private static final String SELECT_SINGLE_OBJECT_SQL = String.format("SELECT %s FROM %s WHERE %s = ?",
    TableObjects.COLUMN_DATA,
    TableObjects.NAME,
    TableObjects.COLUMN_ID);

    private SQLiteDatabase mDatabase;
    private final SQLiteStatement mUpdateObject;
    private final SQLiteStatement mDeleteObject;
    private final ArrayList<ObjectIndex> mObjectIndicies = new ArrayList<ObjectIndex>();

    private abstract class ObjectIndex<T extends Comparable<T>> implements Emitter<T> {

        MapFunction<T> mMapFunction;
        SQLiteStatement mInsertFunction;
        String mCurrentObjId;

        public ObjectIndex(String indexName, MapFunction<T> mapFunction) {
            mMapFunction = mapFunction;
            mInsertFunction = mDatabase.compileStatement(
                    String.format("INSERT OR REPLACE INTO %s (value, objid) VALUES(?, ?);",
                            indexName));
        }

        public synchronized void map(UUID id, UBValue value) {
            mCurrentObjId = id.toString();
            mMapFunction.map(value, this);
        }

    }

    private class ObjectIndexString extends ObjectIndex<String> {

        public ObjectIndexString(String indexName, MapFunction<String> mapFunction) {
            super(indexName, mapFunction);
        }

        @Override
        public void emit(String key) {
            mInsertFunction.bindString(1, key);
            mInsertFunction.bindString(2, mCurrentObjId);
            mInsertFunction.executeUpdateDelete();
        }
    }

    private class ObjectIndexInt extends ObjectIndex<Long> {

        public ObjectIndexInt(String indexName, MapFunction<Long> mapFunction) {
            super(indexName, mapFunction);
        }

        @Override
        public void emit(Long key) {
            mInsertFunction.bindLong(1, key);
            mInsertFunction.bindString(2, mCurrentObjId);
            mInsertFunction.executeUpdateDelete();
        }
    }

    private class ObjectIndexFloat extends ObjectIndex<Double> {

        public ObjectIndexFloat(String indexName, MapFunction<Double> mapFunction) {
            super(indexName, mapFunction);
        }

        @Override
        public void emit(Double key) {
            mInsertFunction.bindDouble(1, key);
            mInsertFunction.bindString(2, mCurrentObjId);
            mInsertFunction.executeUpdateDelete();
        }
    }


    public AndroidSqliteDriver(SQLiteDatabase database) {
        mDatabase = database;


        mUpdateObject = mDatabase.compileStatement(String.format("INSERT OR REPLACE INTO %s (%s, %s) VALUES(?, ?);",
                TableObjects.NAME,
                TableObjects.COLUMN_ID, TableObjects.COLUMN_DATA));

        mDeleteObject = mDatabase.compileStatement(String.format("DELETE FROM %s WHERE %s = ?;",
                TableObjects.NAME,
                TableObjects.COLUMN_ID));
    }

    @Override
    public void close() {
        mDatabase.close();
        mDatabase = null;
    }

    @Override
    public UBObject getMeta() throws IOException {
        UBValue retval = getObject(KEY_META);
        if(retval != null && retval.isObject()) {
            return retval.asObject();
        } else {
            return UBValueFactory.createObject();
        }
    }

    @Override
    public void saveMeta(UBObject obj) throws IOException {
        saveObject(KEY_META, obj);
    }

    private UBValue getObject(String key) throws IOException {
        UBValue retval = null;
        android.database.Cursor cursor = mDatabase.rawQuery(SELECT_SINGLE_OBJECT_SQL, new String[]{key});
        try {
            if (cursor.moveToFirst()) {
                byte[] buff = cursor.getBlob(0);
                UBReader reader = new UBReader(new ByteArrayInputStream(buff));
                retval = reader.read();
                reader.close();
            }
        } finally {
            cursor.close();
        }

        return retval;
    }

    private int saveObject(String key, UBValue value) throws IOException {
        mUpdateObject.bindString(1, key);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        UBWriter writer = new UBWriter(bout);
        writer.write(value);
        writer.close();

        byte[] buff = bout.toByteArray();
        mUpdateObject.bindBlob(2, buff);

        return mUpdateObject.executeUpdateDelete();
    }

    @Override
    public UBValue get(UUID key) throws IOException {
        return getObject(key.toString());
    }

    @Override
    public void insert(UUID id, UBValue value) throws IOException {

        mDatabase.beginTransaction();
        try {

            for(int i=0;i<mObjectIndicies.size();i++) {
                ObjectIndex index = mObjectIndicies.get(i);
                index.map(id, value);
            }

            saveObject(id.toString(), value);

            mDatabase.setTransactionSuccessful();

        } finally {
            mDatabase.endTransaction();
        }


    }

    @Override
    public UUID genId() {
        return null;
    }

    @Override
    public void update(UUID id, UBValue value) throws IOException {
        mDatabase.beginTransaction();
        try {

            for(int i=0;i<mObjectIndicies.size();i++) {
                ObjectIndex index = mObjectIndicies.get(i);
                index.map(id, value);
            }

            saveObject(id.toString(), value);

        } finally {
            mDatabase.endTransaction();
        }
    }

    @Override
    public void delete(UUID key) throws IOException {
        mDeleteObject.bindString(1, key.toString());
        mDeleteObject.executeUpdateDelete();
    }

    @Override
    public long incrementLongField(String fieldName) {
        return 0;
    }

    @Override
    public <T extends Comparable<T>> Cursor queryIndex(String indexName, T min, boolean minInclusive, T max, boolean maxInclusive) throws IOException {
        return null;
    }

    private boolean isIntType(Class<?> classType) {
        return classType == long.class || classType == int.class || classType == short.class || classType == byte.class
                || classType == Long.class || classType == Integer.class || classType == Short.class || classType == Byte.class;
    }

    private boolean isFloatType(Class<?> classType) {
        return classType == double.class || classType == float.class
                || classType == Double.class || classType == Float.class;
    }

    @Override
    public <T extends Comparable<T>> void addIndex(final String indexName, final MapFunction<T> mapFunction) throws IOException {

        Class<?> keyType = null;
        for(Type type : mapFunction.getClass().getGenericInterfaces()) {
            if(type instanceof ParameterizedType) {
                keyType = (Class<?>) ((ParameterizedType)type).getActualTypeArguments()[0];
                break;
            }
        }

        String sqlType = null;
        Callable<ObjectIndex> indexConstructor = null;

        if(String.class.isAssignableFrom(keyType)) {
            sqlType = "TEXT";
            indexConstructor = new Callable<ObjectIndex>(){
                @Override
                public ObjectIndex call() throws Exception {
                    return new ObjectIndexString(indexName, (MapFunction<String>) mapFunction);
                }
            };
        } else if(isIntType(keyType)) {
            sqlType = "INTEGER";
            indexConstructor = new Callable<ObjectIndex>() {
                @Override
                public ObjectIndex call() throws Exception {
                    return new ObjectIndexInt(indexName, (MapFunction<Long>) mapFunction);
                }
            };
        } else if(isFloatType(keyType)) {
            sqlType = "REAL";
            indexConstructor = new Callable<ObjectIndex>() {
                @Override
                public ObjectIndex call() throws Exception {
                    return new ObjectIndexFloat(indexName, (MapFunction<Double>) mapFunction);
                }
            };
        }  else {
            throw new RuntimeException("cannot serialize type: " + keyType);
        }


        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (value %s, objid TEXT, PRIMARY KEY(value, objid));",
                indexName, sqlType);

        mDatabase.execSQL(sql);
        try {
            mObjectIndicies.add(indexConstructor.call());
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    @Override
    public void deleteIndex(String indexName) {
        mDatabase.execSQL("DROP TABLE IF EXISTS ?", new String[]{indexName});
    }

    @Override
    public void beginTransaction() throws IOException {
        mDatabase.execSQL("BEGIN;");
    }

    @Override
    public void commitTransaction() throws IOException {
        mDatabase.execSQL("COMMIT;");
    }

    @Override
    public void rollbackTransaction() throws IOException {
        mDatabase.execSQL("ROLLBACK;");
    }
}
