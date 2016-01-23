package org.example;

import com.devsmart.microdb.DBObject;
import com.devsmart.microdb.MicroDB;
import com.devsmart.microdb.Utils;
import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBString;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;

public class MyDBObj extends DBObject {

    public static final UBString TYPE = UBValueFactory.createString("MyDBObj");

    private boolean myBool;

    private byte myByte;

    private char myChar;

    private short myShort;

    private int myInt;

    private long myLong;

    private float myFloat;

    private double myDouble;

    private String myString;

    private MyDBObj myDBO;

    @Override
    public void writeToUBObject(UBObject obj) {
        super.writeToUBObject(obj);
        final MicroDB db = getDB();
        obj.put("myBool", UBValueFactory.createBool(myBool));
        obj.put("myByte", UBValueFactory.createInt(myByte));
        obj.put("myChar", UBValueFactory.createInt(myChar));
        obj.put("myShort", UBValueFactory.createInt(myShort));
        obj.put("myInt", UBValueFactory.createInt(myInt));
        obj.put("myLong", UBValueFactory.createInt(myLong));
        obj.put("myFloat", UBValueFactory.createFloat32(myFloat));
        obj.put("myDouble", UBValueFactory.createFloat64(myDouble));
        obj.put("myString", UBValueFactory.createString(myString));
        obj.put("myDBO", db != null ? db.writeObject(myDBO) : Utils.writeDBObj(myDBO));
    }

    @Override
    public void readFromUBObject(UBObject obj) {
        super.readFromUBObject(obj);
        final MicroDB db = getDB();
        UBValue value = null;
        value = obj.get("myBool");
        if (value != null) {
            this.myBool = value.asBool();
        }
        value = obj.get("myByte");
        if (value != null) {
            this.myByte = value.asByte();
        }
        value = obj.get("myChar");
        if (value != null) {
            this.myChar = value.asChar();
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
        value = obj.get("myFloat");
        if (value != null) {
            this.myFloat = value.asFloat32();
        }
        value = obj.get("myDouble");
        if (value != null) {
            this.myDouble = value.asFloat64();
        }
        value = obj.get("myString");
        if (value != null) {
            this.myString = value.asString();
        }
        value = obj.get("myDBO");
        if (value != null) {
            this.myDBO = new MyDBObj();
            this.myDBO = db != null ? db.readObject(value, this.myDBO) : Utils.readDBObj(value, this.myDBO));
        } else {
            this.myDBO = null;
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

    public char getMyChar() {
        return myChar;
    }

    public void setMyChar(char value) {
        this.myChar = value;
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

    public float getMyFloat() {
        return myFloat;
    }

    public void setMyFloat(float value) {
        this.myFloat = value;
        setDirty();
    }

    public double getMyDouble() {
        return myDouble;
    }

    public void setMyDouble(double value) {
        this.myDouble = value;
        setDirty();
    }

    public String getMyString() {
        return myString;
    }

    public void setMyString(String value) {
        this.myString = value;
        setDirty();
    }

    public MyDBObj getMyDBO() {
        return myDBO;
    }

    public void setMyDBO(MyDBObj value) {
        this.myDBO = value;
        setDirty();
    }

}