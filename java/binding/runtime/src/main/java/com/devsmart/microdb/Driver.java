package com.devsmart.microdb;


import com.devsmart.microdb.ubjson.UBValue;

import java.io.IOException;

public interface Driver {

    void close();

    /**
     * loads and returns the object data.
     * @param key the object primary key
     * @return
     * @throws IOException
     */
    UBValue load(UBValue key) throws IOException;


    /**
     * Save the data to database. This method will update the
     * object data if it already exists in the database, or will
     * create a new entry and assign a primary key to the object.
     * @param data object data to be saved or updated
     * @return the primary key of the object
     * @throws IOException
     */
    UBValue save(UBValue data) throws IOException;


    void delete(UBValue key) throws IOException;

    DBIterator queryIndex(String indexName) throws IOException;
    void addIndex(String indexName, String indexQuery) throws IOException;
    void deleteIndex(String indexName);
}
