package com.devsmart.microdb.ubjson;


public class UBArray extends UBValue {

    public enum ArrayType {
        Generic,
        Int8,
        Float32,
        Float64
    }

    private UBValue[] mValue;

    UBArray() {

    }

    UBArray(UBValue[] value) {
        mValue = value;
    }

    @Override
    public Type getType() {
        return Type.Array;
    }

    public boolean isStronglyTyped() {
        return false;
    }

    public ArrayType getStrongType() {
        return ArrayType.Generic;
    }

    public int size() {
        return mValue.length;
    }

    public UBValue get(int index) {
        return mValue[index];
    }

    @Override
    public int compareTo(UBValue o) {
        //TODO: implement
        int retval = 0;
        return retval;

    }
}
