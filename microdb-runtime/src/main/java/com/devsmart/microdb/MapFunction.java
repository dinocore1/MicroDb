package com.devsmart.microdb;


import com.devsmart.ubjson.UBValue;

public interface MapFunction<T extends Comparable<? extends T>> {

    //Class<T> getKeyType();

    void map(UBValue value, Emitter<T> emitter);
}
