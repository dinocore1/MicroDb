package com.devsmart.microdb;


import com.devsmart.ubjson.UBValue;

public interface Datum {

    UBValue toUBValue();

    void fromUBValue(UBValue value);
}
