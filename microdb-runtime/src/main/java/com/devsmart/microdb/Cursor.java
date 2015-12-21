package com.devsmart.microdb;


import com.devsmart.ubjson.UBValue;

import java.util.Iterator;
import java.util.UUID;

public interface Cursor<K extends Comparable<K>> {

    boolean isBeforeFirst();
    boolean isAfterLast();
    boolean isFirst();
    boolean isLast();

    boolean moveToFirst();
    boolean moveToLast();
    boolean moveToNext();
    boolean moveToPrevious();


    UUID getPrimaryKey();
    K getSecondaryKey();
    UBValue getValue();
}
