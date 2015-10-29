package com.devsmart.microdb;


import com.devsmart.ubjson.UBValue;

import java.io.Closeable;
import java.util.UUID;

public interface DBIterator<T extends Comparable<?>> {

    void seekTo(T key);


    T getKey();
    UUID getPrimaryKey();
    UBValue getValue();

}
