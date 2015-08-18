package com.devsmart.microdb.ubjson;


public class UBString extends UBValue {


    private String mString;

    UBString(String string) {
        mString = string;
    }

    @Override
    public Type getType() {
        return Type.String;
    }

    public String getString() {
        return mString;
    }
}
