package com.devsmart.microdb;


import com.devsmart.microdb.ubjson.UBValue;

import java.io.IOException;

public interface Driver {

    UBValue get(UBValue key) throws IOException;
    void put(UBValue key, UBValue value) throws IOException;
}
