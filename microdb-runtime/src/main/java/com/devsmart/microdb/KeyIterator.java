package com.devsmart.microdb;


import java.util.Iterator;
import java.util.UUID;

public interface KeyIterator<T extends Comparable<?>> extends Iterator<T> {

    UUID getPrimaryKey();
    void seekTo(T key);
}
