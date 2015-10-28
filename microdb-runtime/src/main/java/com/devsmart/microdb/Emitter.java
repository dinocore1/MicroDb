package com.devsmart.microdb;


public interface Emitter<T extends Comparable<?>> {

    public void emit(T key);
}
