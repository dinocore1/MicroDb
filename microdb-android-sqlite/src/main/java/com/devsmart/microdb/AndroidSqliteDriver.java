package com.devsmart.microdb;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBReader;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;
import com.devsmart.ubjson.UBWriter;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Callable;

public class AndroidSqliteDriver implements Driver {

    private static final Logger LOGGER = LoggerFactory.getLogger(AndroidSqliteDriver.class);

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

        final String mIndexName;
        MapFunction<T> mMapFunction;
        SQLiteStatement mInsertFunction;
        SQLiteStatement mDeleteFunction;
        String mCurrentObjId;

        public ObjectIndex(String indexName, MapFunction<T> mapFunction) {
            mIndexName = indexName;
            mMapFunction = mapFunction;
            mInsertFunction = mDatabase.compileStatement(
                    String.format("INSERT OR REPLACE INTO %s (value, objid) VALUES(?, ?);",
                            indexName));

            mDeleteFunction = mDatabase.compileStatement(
                    String.format("DELETE FROM %s WHERE objid = ?;", indexName));

        }

        public synchronized void map(UUID id, UBValue value) {
            mCurrentObjId = id.toString();
            mMapFunction.map(value, this);
        }

        public void deleteEntriesFromObject(String objid) {
            synchronized (mDeleteFunction) {
                mDeleteFunction.bindString(1, objid);
                mDeleteFunction.executeUpdateDelete();
            }
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
        final String objid = id.toString();
        mDatabase.beginTransaction();
        try {

            for(int i=0;i<mObjectIndicies.size();i++) {
                ObjectIndex index = mObjectIndicies.get(i);
                index.deleteEntriesFromObject(objid);
                index.map(id, value);
            }

            saveObject(id.toString(), value);

        } finally {
            mDatabase.endTransaction();
        }
    }

    @Override
    public void delete(UUID key) throws IOException {
        final String keyStr = key.toString();
        mDatabase.beginTransaction();
        try {

            for(int i=0;i<mObjectIndicies.size();i++) {
                ObjectIndex index = mObjectIndicies.get(i);
                index.deleteEntriesFromObject(keyStr);
            }

            mDeleteObject.bindString(1, keyStr);
            mDeleteObject.executeUpdateDelete();

            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
    }

    @Override
    public long incrementLongField(String fieldName) {
        return 0;
    }

    private <T extends Comparable<T>> ObjectIndex<T> findIndexByName(String indexName) {
        ObjectIndex retval = null;
        for(int i=0;i<mObjectIndicies.size();i++) {
            ObjectIndex<T> idx = mObjectIndicies.get(i);
            if(idx.mIndexName.equals(indexName)) {
                return idx;
            }
        }

        return retval;
    }


    private static final String[] INDEX_COLUMNS = new String[]{"value", "objid"};

    @Override
    public <T extends Comparable<T>> Cursor queryIndex(String indexName, T min, boolean minInclusive, T max, boolean maxInclusive) throws IOException {

        ObjectIndex<T> objIndex = findIndexByName(indexName);
        if(objIndex == null) {
            throw new IOException("no index with name: " + indexName);
        }

        android.database.Cursor c = null;

        String minSelectionFrag = null;
        String maxSelectionFrag = null;

        if(min != null) {
            minSelectionFrag = String.format("value %s ?", minInclusive ? ">=" : ">");
        }

        if(max != null) {
            maxSelectionFrag = String.format("value %s ?", maxInclusive ? "<=" : "<");
        }

        if(min != null && max != null) {
            String selection = String.format("%s AND %s", minSelectionFrag, maxSelectionFrag);
            c = mDatabase.query(indexName, INDEX_COLUMNS, selection, new String[]{min.toString(), max.toString()}, null, null, null, null);

        } else if(min != null) {
            c = mDatabase.query(indexName, INDEX_COLUMNS, minSelectionFrag, new String[]{min.toString()}, null, null, null, null);

        } else if(max != null) {
            c = mDatabase.query(indexName, INDEX_COLUMNS, maxSelectionFrag, new String[]{max.toString()}, null, null, null, null);

        } else {
            c = mDatabase.query(indexName, INDEX_COLUMNS, null, null, null, null, null, null);
        }

        Class<?> keyType = null;
        for(Type type : objIndex.getClass().getGenericInterfaces()) {
            if(type instanceof ParameterizedType) {
                keyType = (Class<?>) ((ParameterizedType)type).getActualTypeArguments()[0];
                break;
            }
        }

        if(String.class.isAssignableFrom(keyType)) {
            return new SQLiteStringIndexCursor(this, c);

        } else if(isIntType(keyType)) {
            return new SQLiteLongIndexCursor(this, c);

        } else if(isFloatType(keyType)) {
            return new SQLiteDoubleIndexCursor(this, c);

        }  else {
            throw new RuntimeException("cannot serialize type: " + keyType);
        }
    }

    private static abstract class SQLiteIndexCursor<T extends Comparable<T>> implements Cursor {

        final AndroidSqliteDriver mDriver;
        final android.database.Cursor mCursor;

        public SQLiteIndexCursor(AndroidSqliteDriver driver, android.database.Cursor cursor) {
            mDriver = driver;
            mCursor = cursor;
        }

        @Override
        public boolean moveToFirst() {
            return mCursor.moveToFirst();
        }

        @Override
        public boolean moveToLast() {
            return mCursor.moveToLast();
        }

        @Override
        public boolean moveToNext() {
            return mCursor.moveToNext();
        }

        @Override
        public boolean moveToPrevious() {
            return mCursor.moveToPrevious();
        }

        @Override
        public boolean move(int pos) {
            return mCursor.move(pos);
        }

        @Override
        public boolean isFirst() {
            return mCursor.isFirst();
        }

        @Override
        public boolean isLast() {
            return mCursor.isLast();
        }

        @Override
        public boolean isBeforeFirst() {
            return mCursor.isBeforeFirst();
        }

        @Override
        public boolean isAfterLast() {
            return mCursor.isAfterLast();
        }

        @Override
        public int getPosition() {
            return mCursor.getPosition();
        }

        @Override
        public int getCount() {
            return mCursor.getCount();
        }
    }

    private static class SQLiteStringIndexCursor extends SQLiteIndexCursor<String> {

        public SQLiteStringIndexCursor(AndroidSqliteDriver driver, android.database.Cursor cursor) {
            super(driver, cursor);
        }

        @Override
        public Row getRow() {
            return new SqliteStringIndexRow(mDriver, mCursor);
        }
    }

    private static class SQLiteLongIndexCursor extends SQLiteIndexCursor<Long> {

        public SQLiteLongIndexCursor(AndroidSqliteDriver driver, android.database.Cursor cursor) {
            super(driver, cursor);
        }

        @Override
        public Row getRow() {
            return new SqliteLongIndexRow(mDriver, mCursor);
        }
    }

    private static class SQLiteDoubleIndexCursor extends SQLiteIndexCursor<Double> {

        public SQLiteDoubleIndexCursor(AndroidSqliteDriver driver, android.database.Cursor cursor) {
            super(driver, cursor);
        }

        @Override
        public Row getRow() {
            return new SqliteDoubleIndexRow(mDriver, mCursor);
        }
    }

    private static abstract class SqliteIndexRow<T extends Comparable<T>> implements Row {

        private final AndroidSqliteDriver mDriver;
        private final UUID mObjectId;
        UBValue mObjValue;

        public SqliteIndexRow(AndroidSqliteDriver driver, android.database.Cursor cursor) {
            mDriver = driver;
            mObjectId = UUID.fromString(cursor.getString(1));
        }

        @Override
        public UUID getPrimaryKey() {
            return mObjectId;
        }

        @Override
        public UBValue getValue() {
            UUID primaryKey = getPrimaryKey();
            if (mObjValue == null) try {
                mObjValue = mDriver.get(primaryKey);
            } catch (IOException e) {
                LOGGER.error("error getting value for: {}", primaryKey, e);
            }
            return mObjValue;
        }
    }

    private static class SqliteStringIndexRow extends SqliteIndexRow<String> {

        private final String mStringValue;

        public SqliteStringIndexRow(AndroidSqliteDriver driver, android.database.Cursor cursor) {
            super(driver, cursor);
            mStringValue = cursor.getString(0);
        }

        @Override
        public String getSecondaryKey() {
            return mStringValue;
        }
    }

    private static class SqliteDoubleIndexRow extends SqliteIndexRow<Double> {

        private final double mDoubleValue;

        public SqliteDoubleIndexRow(AndroidSqliteDriver driver, android.database.Cursor cursor) {
            super(driver, cursor);
            mDoubleValue = cursor.getDouble(0);
        }


        @Override
        public Double getSecondaryKey() {
            return mDoubleValue;
        }
    }

    private static class SqliteLongIndexRow extends SqliteIndexRow<Long> {

        private final long mLongValue;

        public SqliteLongIndexRow(AndroidSqliteDriver driver, android.database.Cursor cursor) {
            super(driver, cursor);
            mLongValue = cursor.getLong(0);
        }

        @Override
        public Long getSecondaryKey() {
            return mLongValue;
        }
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


        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (value %s, objid TEXT);" +
                        "CREATE INDEX IF NOT EXISTS %s ON %s(value);" +
                        "CREATE INDEX IF NOT EXISTS %s ON %s(objid);",
                indexName, sqlType,
                indexName+"_val", indexName,
                indexName+"_obj", indexName);

        mDatabase.execSQL(sql);
        try {
            mObjectIndicies.add(indexConstructor.call());
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    @Override
    public void recomputeIndex(String indexName) {
        //TODO: implement
    }

    @Override
    public void deleteIndex(String indexName) {
        mDatabase.execSQL("DROP TABLE IF EXISTS ?", new String[]{indexName});
    }

    @Override
    public void beginTransaction() throws IOException {
        mDatabase.beginTransaction();
        //mDatabase.execSQL("BEGIN;");
    }

    @Override
    public void commitTransaction() throws IOException {
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
        //mDatabase.execSQL("COMMIT;");
    }

    @Override
    public void rollbackTransaction() throws IOException {
        mDatabase.endTransaction();
        //mDatabase.execSQL("ROLLBACK;");
    }
}
