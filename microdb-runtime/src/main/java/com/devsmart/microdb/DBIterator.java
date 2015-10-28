package com.devsmart.microdb;


import com.devsmart.ubjson.UBValue;

import java.io.Closeable;
import java.util.UUID;

public interface DBIterator<T extends Comparable<?>> extends Closeable {

    void seekTo(T key);

    boolean valid();
    void next();
    void prev();

    T getKey();
    UUID getPrimaryKey();
    UBValue getValue();

}
