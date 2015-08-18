package com.devsmart.microdb.ubjson;


import java.io.IOException;
import java.io.OutputStream;

public abstract class UBValue {

    public static final byte MARKER_NULL = 'Z';
    public static final byte MARKER_TRUE = 'T';
    public static final byte MARKER_FALSE = 'F';
    public static final byte MARKER_CHAR = 'C';
    public static final byte MARKER_INT8 = 'i';
    public static final byte MARKER_UINT8 = 'U';
    public static final byte MARKER_INT16 = 'I';
    public static final byte MARKER_INT32 = 'l';
    public static final byte MARKER_INT64 = 'L';
    public static final byte MARKER_FLOAT32 = 'd';
    public static final byte MARKER_FLOAT64 = 'D';
    public static final byte MARKER_STRING = 'S';

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
    //public abstract void write(OutputStream out) throws IOException;

    public boolean isNull() {
        return getType() == Type.Null;
    }

    public boolean isBool() {
        return getType() == Type.Bool;
    }

    public boolean asBool() {
        return ((UBBool)this).getBool();
    }

    public boolean isChar() {
        return getType() == Type.Char;
    }

    public char asChar() {
        return ((UBChar)this).getChar();
    }

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

    public float asFloat32() {
        float retval;
        switch (getType()) {
            case Float32:
                retval = ((UBFloat32)this).getFloat();
                break;

            case Float64:
                retval = (float)((UBFloat64)this).getDouble();
                break;

            default:
                throw new RuntimeException("not a float type");
        }
        return retval;
    }

    public double asFloat64() {
        double retval;
        switch (getType()) {
            case Float32:
                retval = ((UBFloat32)this).getFloat();
                break;

            case Float64:
                retval = ((UBFloat64)this).getDouble();
                break;

            default:
                throw new RuntimeException("not a float type");
        }
        return retval;
    }
}
