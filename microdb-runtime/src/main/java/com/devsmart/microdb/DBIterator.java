package com.devsmart.microdb;


import com.devsmart.ubjson.UBValue;

import java.io.Closeable;

public interface DBIterator extends Closeable {

    void seekTo(UBValue key);

    boolean valid();
    void next();
    void prev();

    UBValue getKey();
    UBValue getPrimaryKey();
    UBValue getValue();

}
