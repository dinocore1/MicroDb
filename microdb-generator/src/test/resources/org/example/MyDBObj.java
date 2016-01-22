package org.example;

import com.devsmart.microdb.DBObject;
import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBString;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;

public class MyDBObj extends DBObject {

    public static final UBString TYPE = UBValueFactory.createString("MyDBObj");

    private boolean myBool;

    private byte myByte;

    private short myShort;

    private int myInt;

    private long myLong;

    private String myString;

    @Override
    public void writeToUBObject(UBObject obj) {
        super.writeToUBObject(obj);
        obj.put("myBool", UBValueFactory.createBool(myBool));
        obj.put("myByte", UBValueFactory.createInt(myByte));
        obj.put("myShort", UBValueFactory.createInt(myShort));
        obj.put("myInt", UBValueFactory.createInt(myInt));
        obj.put("myLong", UBValueFactory.createInt(myLong));
        obj.put("myString", UBValueFactory.createString(myString));
    }

    @Override
    public void readFromUBObject(UBObject obj) {
        super.readFromUBObject(obj);
        UBValue value = null;
        value = obj.get("myBool");
        if (value != null) {
            this.myBool = value.asBool();
        }
        value = obj.get("myByte");
        if (value != null) {
            this.myByte = value.asByte();
        }
        value = obj.get("myShort");
        if (value != null) {
            this.myShort = value.asShort();
        }
        value = obj.get("myInt");
        if (value != null) {
            this.myInt = value.asInt();
        }
        value = obj.get("myLong");
        if (value != null) {
            this.myLong = value.asLong();
        }
        value = obj.get("myString");
        if (value != null) {
            this.myString = value.asString();
        }
    }

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