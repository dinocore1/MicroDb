package com.devsmart.microdb.ubjson;


public final class UBFloat32Array extends UBValue {

    private final float[] mValues;

    UBFloat32Array(float[] values) {
        mValues = values;
    }

    @Override
    public Type getType() {
        return Type.Array;
    }

    public float[] getValues() {
        return mValues;
    }
}
