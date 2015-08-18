package com.devsmart.microdb.ubjson;


import java.io.IOException;
import java.io.OutputStream;

public abstract class UBValue {

    public static final char MARKER_STRING = 'S';

    public enum Type {
        Null,
        Char,
        Bool,
        Int8,
        Uint8,
        Int16,
        Int32,
        Int64,
        Float32,
        Float64,
        String,
        Array,
        Object
    }

    public abstract Type getType();
    public abstract void write(OutputStream out) throws IOException;

    public boolean isNumber() {
        switch (getType()){
            case Int8:
            case Uint8:
            case Int16:
            case Int32:
            case Int64:
            case Float32:
            case Float64:
                return true;

            default:
                return false;
        }
    }

    public boolean isInteger() {
        switch (getType()){
            case Int8:
            case Uint8:
            case Int16:
            case Int32:
            case Int64:
                return true;

            default:
                return false;
        }
    }

    public String asString() {
        UBString thiz = (UBString)this;
        return thiz.getString();
    }

    public long asInt() {
        switch (getType()){
            case Int8:
                return ((UBInt8)this).getInt();
            case Uint8:
                return ((UBUInt8)this).getInt();
            case Int16:
                return ((UBInt16)this).getInt();
            case Int32:
                return ((UBInt32)this).getInt();
            case Int64:
                return ((UBInt64)this).getInt();
            case Float32:
                return (long)((UBFloat32)this).getFloat();
            case Float64:
                return (long)((UBFloat64)this).getDouble();

            default:
                throw new RuntimeException("not a number type");

        }
    }
}
