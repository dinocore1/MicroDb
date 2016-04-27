package example;

import com.devsmart.microdb.DBObject;
import com.devsmart.microdb.MicroDB;
import com.devsmart.microdb.Utils;
import com.devsmart.ubjson.UBArray;
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

    private ExtendObj myExtendo;

    private boolean[] myBoolArray;

    private byte[] myByteArray;

    private short[] myShortArray;

    private int[] myIntArray;

    private long[] myLongArray;

    private float[] myFloatArray;

    private double[] myDoubleArray;

    private ExtendObj[] myExtendoArray;

    @Override
    public void writeToUBObject(UBObject obj) {
        super.writeToUBObject(obj);
        final MicroDB db = getDB();
        obj.put("myBool", UBValueFactory.createBool(myBool));
        obj.put("myByte", UBValueFactory.createInt(myByte));
        obj.put("myChar", UBValueFactory.createChar(myChar));
        obj.put("myShort", UBValueFactory.createInt(myShort));
        obj.put("myInt", UBValueFactory.createInt(myInt));
        obj.put("myLong", UBValueFactory.createInt(myLong));
        obj.put("myFloat", UBValueFactory.createFloat32(myFloat));
        obj.put("myDouble", UBValueFactory.createFloat64(myDouble));
        obj.put("myString", UBValueFactory.createStringOrNull(myString));
        obj.put("myDBO", Utils.writeDBObj(db, myDBO));
        obj.put("myExtendo", Utils.writeDBObj(db, myExtendo));
        obj.put("myBoolArray", UBValueFactory.createArrayOrNull(myBoolArray));
        obj.put("myByteArray", UBValueFactory.createArrayOrNull(myByteArray));
        obj.put("myShortArray", UBValueFactory.createArrayOrNull(myShortArray));
        obj.put("myIntArray", UBValueFactory.createArrayOrNull(myIntArray));
        obj.put("myLongArray", UBValueFactory.createArrayOrNull(myLongArray));
        obj.put("myFloatArray", UBValueFactory.createArrayOrNull(myFloatArray));
        obj.put("myDoubleArray", UBValueFactory.createArrayOrNull(myDoubleArray));
        obj.put("myExtendoArray", Utils.createArrayOrNull(db, myExtendoArray));
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
            if (value.isString()) {
                this.myString = value.asString();
            } else {
                this.myString = null;
            }
        }
        value = obj.get("myDBO");
        if (value != null) {
            this.myDBO = Utils.readDBObj(db, value, new MyDBObj());
        } else {
            this.myDBO = null;
        }
        value = obj.get("myExtendo");
        if (value != null) {
            this.myExtendo = Utils.readDBObj(db, value, new ExtendObj());
        } else {
            this.myExtendo = null;
        }
        value = obj.get("myBoolArray");
        if (value != null && value.isArray()) {
            this.myBoolArray = value.asBoolArray();
        }
        value = obj.get("myByteArray");
        if (value != null && value.isArray()) {
            this.myByteArray = value.asByteArray();
        }
        value = obj.get("myShortArray");
        if (value != null && value.isArray()) {
            this.myShortArray = value.asShortArray();
        }
        value = obj.get("myIntArray");
        if (value != null && value.isArray()) {
            this.myIntArray = value.asInt32Array();
        }
        value = obj.get("myLongArray");
        if (value != null && value.isArray()) {
            this.myLongArray = value.asInt64Array();
        }
        value = obj.get("myFloatArray");
        if (value != null && value.isArray()) {
            this.myFloatArray = value.asFloat32Array();
        }
        value = obj.get("myDoubleArray");
        if (value != null && value.isArray()) {
            this.myDoubleArray = value.asFloat64Array();
        }
        value = obj.get("myExtendoArray");
        if (value != null && value.isArray()) {
            UBArray array = value.asArray();
            final int size = array.size();
            this.myExtendoArray = new ExtendObj[size];
            for (int i = 0; i < size; i++) {
                this.myExtendoArray[i] = Utils.readDBObj(db, array.get(i), new ExtendObj());
            }
        } else {
            this.myExtendoArray = null;
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

    public ExtendObj getMyExtendo() {
        return myExtendo;
    }

    public void setMyExtendo(ExtendObj value) {
        this.myExtendo = value;
        setDirty();
    }

    public boolean[] getMyBoolArray() {
        return myBoolArray;
    }

    public void setMyBoolArray(boolean[] value) {
        this.myBoolArray = value;
        setDirty();
    }

    public byte[] getMyByteArray() {
        return myByteArray;
    }

    public void setMyByteArray(byte[] value) {
        this.myByteArray = value;
        setDirty();
    }

    public short[] getMyShortArray() {
        return myShortArray;
    }

    public void setMyShortArray(short[] value) {
        this.myShortArray = value;
        setDirty();
    }

    public int[] getMyIntArray() {
        return myIntArray;
    }

    public void setMyIntArray(int[] value) {
        this.myIntArray = value;
        setDirty();
    }

    public long[] getMyLongArray() {
        return myLongArray;
    }

    public void setMyLongArray(long[] value) {
        this.myLongArray = value;
        setDirty();
    }

    public float[] getMyFloatArray() {
        return myFloatArray;
    }

    public void setMyFloatArray(float[] value) {
        this.myFloatArray = value;
        setDirty();
    }

    public double[] getMyDoubleArray() {
        return myDoubleArray;
    }

    public void setMyDoubleArray(double[] value) {
        this.myDoubleArray = value;
        setDirty();
    }

    public ExtendObj[] getMyExtendoArray() {
        return myExtendoArray;
    }

    public void setMyExtendoArray(ExtendObj[] value) {
        this.myExtendoArray = value;
        setDirty();
    }

}