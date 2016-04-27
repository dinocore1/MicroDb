package com.devsmart.microdb;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.ParcelFileDescriptor;

import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBReader;
import com.devsmart.ubjson.UBValue;

import java.io.IOException;
import java.util.UUID;

public class AndroidSqliteDriver implements Driver {

    private final SQLiteStatement mGetObjectByIdQuery;

    static class TableObjects {
        final static String NAME = "Objects";
        final static String COLUMN_ID = "id";
        final static String COLUMN_DATA = "data";
    }

    private SQLiteDatabase mDatabase;

    public AndroidSqliteDriver(SQLiteDatabase database) {
        mDatabase = database;

        mGetObjectByIdQuery = mDatabase.compileStatement(String.format("SELECT %s FROM %s WHERE %s = ?",
                TableObjects.COLUMN_DATA,
                TableObjects.NAME,
                TableObjects.COLUMN_ID));

        mInsertObject = mDatabase.compileStatement(String.format("INSERT "))
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
        mGetObjectByIdQuery.bindString(1, key.toString());
        ParcelFileDescriptor descriptor = mGetObjectByIdQuery.simpleQueryForBlobFileDescriptor();
        ParcelFileDescriptor.AutoCloseInputStream input = new ParcelFileDescriptor.AutoCloseInputStream(descriptor);
        UBReader reader = new UBReader(input);
        UBValue retval = reader.read();
        input.close();

        return retval;
    }

    @Override
    public void insert(UUID id, UBValue value) throws IOException {

    }

    @Override
    public UUID genId() {
        return null;
    }

    @Override
    public void update(UUID id, UBValue value) throws IOException {

    }

    @Override
    public void delete(UUID key) throws IOException {

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
