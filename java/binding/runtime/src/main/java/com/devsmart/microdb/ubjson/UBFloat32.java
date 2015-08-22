package com.devsmart.microdb.ubjson;


public final class UBFloat32 extends UBValue {

    private float mValue;

    UBFloat32(float value) {
        mValue = value;
    }

    @Override
    public Type getType() {
        return Type.Float32;
    }

    public float getFloat() {
        return mValue;
    }

    public double getDouble() {
        return mValue;
    }
}
