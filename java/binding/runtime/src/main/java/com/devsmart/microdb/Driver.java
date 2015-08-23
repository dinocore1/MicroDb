package com.devsmart.microdb;


import com.devsmart.microdb.ubjson.UBValue;

public interface Driver {

    UBValue get(UBValue key);
    void put(UBValue key, UBValue value);
}
