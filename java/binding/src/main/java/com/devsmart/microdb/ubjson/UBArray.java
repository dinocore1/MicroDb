package com.devsmart.microdb.ubjson;


import java.util.ArrayList;

public class UBArray extends UBValue {

    private ArrayList<UBValue> mValue = new ArrayList<UBValue>();

    @Override
    public Type getType() {
        return Type.Array;
    }

    public void add(UBValue value) {
        mValue.add(value);
    }
}
