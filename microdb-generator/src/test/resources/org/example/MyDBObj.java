package org.example;

import com.devsmart.microdb.DBObject;
import com.devsmart.ubjson.UBString;
import com.devsmart.ubjson.UBValueFactory;

public class MyDBObj extends DBObject {

    public static final UBString TYPE = UBValueFactory.createString("MyDBObj");

    private boolean myBool;

    private byte myByte;

    private short myShort;

    private int myInt;

    private long myLong;

    private String myString;

    public boolean getMyBool() {
        return myBool;
    }

    public void setMyBool(boolean value) {
        this.myBool = value;
        setDirty();
    }

    public byte getMyByte() {
        return myByte;
    }

    public void setMyByte(byte value) {
        this.myByte = value;
        setDirty();
    }

    public short getMyShort() {
        return myShort;
    }

    public void setMyShort(short value) {
        this.myShort = value;
        setDirty();
    }

    public int getMyInt() {
        return myInt;
    }

    public void setMyInt(int value) {
        this.myInt = value;
        setDirty();
    }

    public long getMyLong() {
        return myLong;
    }

    public void setMyLong(long value) {
        this.myLong = value;
        setDirty();
    }

    public String getMyString() {
        return myString;
    }

    public void setMyString(String value) {
        this.myString = value;
        setDirty();
    }

}