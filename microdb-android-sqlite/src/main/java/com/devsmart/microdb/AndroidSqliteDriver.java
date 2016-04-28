package com.devsmart.microdb;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBReader;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;
import com.devsmart.ubjson.UBWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

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


    public AndroidSqliteDriver(SQLiteDatabase database) {
        mDatabase = database;


        mUpdateObject = mDatabase.compileStatement(String.format("INSERT OR REPLACE INTO %s (%s, %s) VALUES(?, ?)",
                TableObjects.NAME,
                TableObjects.COLUMN_ID, TableObjects.COLUMN_DATA));

        mDeleteObject = mDatabase.compileStatement(String.format("DELETE FROM %s WHERE %s = ?",
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
        saveObject(id.toString(), value);
    }

    @Override
    public UUID genId() {
        return null;
    }

    @Override
    public void update(UUID id, UBValue value) throws IOException {
        saveObject(id.toString(), value);
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

    @Override
    public <T extends Comparable<T>> void addIndex(String indexName, MapFunction<T> mapFunction) throws IOException {

    }

    @Override
    public void deleteIndex(String indexName) {

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
