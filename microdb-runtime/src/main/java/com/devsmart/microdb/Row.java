package com.devsmart.microdb;


import com.devsmart.ubjson.UBValue;

import java.util.UUID;

public interface Row {

    UUID getPrimaryKey();

    <T extends Comparable<T>> T getSecondaryKey();

    UBValue getValue();
}
