package com.devsmart.microdb;


import com.devsmart.microdb.ubjson.UBObject;
import com.devsmart.microdb.ubjson.UBValue;

import java.io.IOException;

public interface Driver {

    void close();

    /**
     * loads the database value with {@code key}
     * @param key
     * @return the value stored in the database or null if it does not exist
     * @throws IOException
     */
    UBValue get(UBValue key) throws IOException;

    /**
     * inserts a new value into the the database. If the value is an object and contains
     * a string value for "id" that value will be used as the primary key, else a new
     * unique key will be automatically generated and returned.
     * @param value
     * @return the unique key for {@code value}
     * @throws IOException
     */
    UBValue insert(UBValue value) throws IOException;

    /**
     * delete the database object with key {@code key}
     * @param key
     * @throws IOException
     */
    void delete(UBValue key) throws IOException;

    DBIterator queryIndex(String indexName) throws IOException;
    void addIndex(String indexName, String indexQuery) throws IOException;
    void deleteIndex(String indexName);
}
