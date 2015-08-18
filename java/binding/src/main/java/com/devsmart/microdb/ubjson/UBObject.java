package com.devsmart.microdb.ubjson;


import java.util.TreeMap;

public final class UBObject extends UBValue {

    private TreeMap<String, UBValue> mValue = new TreeMap<String, UBValue>();

    public UBObject() {

    }

    @Override
    public Type getType() {
        return Type.Object;
    }

    public UBValue get(String key) {
        return mValue.get(key);
    }

    public void set(String key, UBValue value) {
        mValue.put(key, value);
    }
}
