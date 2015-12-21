package com.devsmart.microdb;


import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBValue;

import java.io.IOException;
import java.util.UUID;

public interface Driver {

    void close();

    UBObject getMeta() throws IOException;
    void saveMeta(UBObject obj) throws IOException;

    void addChangeListener(ChangeListener changeListener);

    /**
     * loads the database value with {@code key}
     * @param key
     * @return the value stored in the database or null if it does not exist
     * @throws IOException
     */
    UBValue get(UUID key) throws IOException;

    /**
     * inserts a new value into the the database. A new
     * unique key will be automatically generated and returned.
     * @param value
     * @return the unique key for {@code value}
     * @throws IOException
     */
    UUID insert(UBValue value) throws IOException;

    /**
     * replace value with key {@code id} with new value {@code value}.
     * If a value with with {@code id} does not exist, insert the key-value
     * pair.
     * @param id
     * @param value
     * @throws IOException
     */
    void update(UUID id, UBValue value) throws IOException;

    /**
     * delete the database object with key {@code key}
     * @param key
     * @throws IOException
     */
    void delete(UUID key) throws IOException;

    long incrementLongField(String fieldName);

    <T> Iterable<Row> queryIndex(String indexName, Comparable<T> min, boolean minInclusive, Comparable<T> max, boolean maxInclusive) throws IOException;
    <T extends Comparable<?>> void addIndex(String indexName, MapFunction<T> mapFunction) throws IOException;
    void deleteIndex(String indexName);

    void beginTransaction() throws IOException;
    void commitTransaction() throws IOException;
    void rollbackTransaction() throws IOException;
}
