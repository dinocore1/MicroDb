package com.devsmart.microdb.ubjson;


import java.nio.charset.Charset;

public class UBString extends UBValue {

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    private byte[] mData;

    UBString(byte[] string) {
        mData = string;
    }

    @Override
    public Type getType() {
        return Type.String;
    }

    public String getString() {
        return new String(mData, UTF_8);
    }
}
