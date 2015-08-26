package com.devsmart.microdb.ubjson;


import java.util.Map;
import java.util.TreeMap;

public final class UBObject extends UBValue {

    private TreeMap<String, UBValue> mValue;

    public UBObject() {
        mValue = new TreeMap<String, UBValue>();
    }

    public UBObject(TreeMap<String, UBValue> value) {
        mValue = value;
    }

    @Override
    public Type getType() {
        return Type.Object;
    }

    public boolean has(String key) {
        return mValue.containsKey(key);
    }

    public UBValue get(String key) {
        return mValue.get(key);
    }

    public void set(String key, UBValue value) {
        mValue.put(key, value);
    }

    public Map<String, UBValue> asMap() {
        return mValue;
    }
}
