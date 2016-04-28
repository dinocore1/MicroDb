package com.devsmart.microdb;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBReader;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class AndroidSqliteDriver implements Driver {


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
    private final SQLiteStatement mInsertObject;
    private final SQLiteStatement mUpdateObject;
    private final SQLiteStatement mDeleteObject;


    public AndroidSqliteDriver(SQLiteDatabase database) {
        mDatabase = database;

        mInsertObject = mDatabase.compileStatement(String.format("INSERT INTO %s (%s, %s) VALUES(?, ?)",
                TableObjects.NAME,
                TableObjects.COLUMN_ID, TableObjects.COLUMN_DATA));

        mUpdateObject = mDatabase.compileStatement(String.format("UPDATE %s SET %s = ? WHERE %s = ?",
                TableObjects.NAME,
                TableObjects.COLUMN_DATA,
                TableObjects.COLUMN_ID));

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
        return null;
    }

    @Override
    public void saveMeta(UBObject obj) throws IOException {

    }

    @Override
    public UBValue get(UUID key) throws IOException {

        UBValue retval = null;
        android.database.Cursor cursor = mDatabase.rawQuery(SELECT_SINGLE_OBJECT_SQL, new String[]{key.toString()});
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

    @Override
    public void insert(UUID id, UBValue value) throws IOException {
        mInsertObject.bindString(1, id.toString());

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        UBWriter writer = new UBWriter(bout);
        writer.write(value);
        writer.close();

        byte[] buff = bout.toByteArray();
        mInsertObject.bindBlob(2, buff);
        if(mInsertObject.executeInsert() == -1) {
            throw new IOException("error inserting" + id);
        }
    }

    @Override
    public UUID genId() {
        return null;
    }

    @Override
    public void update(UUID id, UBValue value) throws IOException {

        mUpdateObject.bindString(2, id.toString());

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        UBWriter writer = new UBWriter(bout);
        writer.write(value);
        writer.close();

        byte[] buff = bout.toByteArray();
        mUpdateObject.bindBlob(1, buff);

        if(mUpdateObject.executeUpdateDelete() != 1) {
            throw new IOException("update " + id);
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

    @Override
    public <T extends Comparable<T>> void addIndex(String indexName, MapFunction<T> mapFunction) throws IOException {

    }

    @Override
    public void deleteIndex(String indexName) {

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
