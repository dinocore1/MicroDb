package org.example;

import com.devsmart.microdb.Cursor;
import com.devsmart.microdb.DBObject;
import com.devsmart.microdb.DefaultChangeListener;
import com.devsmart.microdb.Driver;
import com.devsmart.microdb.Emitter;
import com.devsmart.microdb.MapFunction;
import com.devsmart.microdb.MicroDB;
import com.devsmart.microdb.Utils;
import com.devsmart.ubjson.UBArray;
import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBString;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;
import java.io.IOException;

public class MyDBObj extends DBObject {

    public static final UBString TYPE = UBValueFactory.createString("MyDBObj");

    private static final UBString[] SUBTYPES = new UBString[]{ UBValueFactory.createString("ExtendObj"), MyDBObj.TYPE };

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

    private String[] myStringArray;

    private ExtendObj[] myExtendoArray;

    private UBObject myUBObject;

    private UBArray myUBArray;

    private String myNoSerialize;

    private long myAutoIncrement;

    private String myStrIndex;

    @Override
    public synchronized void writeToUBObject(UBObject obj) {
        super.writeToUBObject(obj);
        final MicroDB db = getDB();
        obj.put("type", TYPE);
        obj.put("myBool", UBValueFactory.createBool(myBool));
        obj.put("myByte", UBValueFactory.createInt(myByte));
        obj.put("myChar", UBValueFactory.createInt(myChar));
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
        obj.put("myStringArray", UBValueFactory.createArrayOrNull(myStringArray));
        obj.put("myExtendoArray", Utils.createArrayOrNull(db, myExtendoArray));
        obj.put("myUBObject", myUBObject != null ? myUBObject : UBValueFactory.createNull());
        obj.put("myUBArray", myUBArray != null ? myUBArray : UBValueFactory.createNull());
        obj.put("myAutoIncrement", UBValueFactory.createInt(myAutoIncrement));
        obj.put("myStrIndex", UBValueFactory.createStringOrNull(myStrIndex));
    }

    @Override
    public void readFromUBObject(UBObject obj) {
        super.readFromUBObject(obj);
        final MicroDB db = getDB();
        UBValue value = null;
        value = obj.get("myBool");
        if (value != null && value.isBool()) {
            this.myBool = value.asBool();
        }
        value = obj.get("myByte");
        if (value != null && value.isNumber()) {
            this.myByte = value.asByte();
        }
        value = obj.get("myChar");
        if (value != null && value.isChar()) {
            this.myChar = value.asChar();
        }
        value = obj.get("myShort");
        if (value != null && value.isNumber()) {
            this.myShort = value.asShort();
        }
        value = obj.get("myInt");
        if (value != null && value.isNumber()) {
            this.myInt = value.asInt();
        }
        value = obj.get("myLong");
        if (value != null && value.isNumber()) {
            this.myLong = value.asLong();
        }
        value = obj.get("myFloat");
        if (value != null && value.isNumber()) {
            this.myFloat = value.asFloat32();
        }
        value = obj.get("myDouble");
        if (value != null && value.isNumber()) {
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
        value = obj.get("myStringArray");
        if (value != null && value.isArray()) {
            this.myStringArray = value.asStringArray();
        }
        value = obj.get("myExtendoArray");
        if (value != null && value.isArray()) {
            UBArray array = value.asArray();
            final int size = array.size();
            this.myExtendoArray = new ExtendObj[size];
            for(int i=0;i<size;i++) {
                this.myExtendoArray[i] = Utils.readDBObj(db, array.get(i), new ExtendObj());
            }
        } else {
            this.myExtendoArray = null;
        }
        value = obj.get("myUBObject");
        if (value != null && value.isObject()) {
            this.myUBObject = value.asObject();
        }
        value = obj.get("myUBArray");
        if (value != null && value.isArray()) {
            this.myUBArray = value.asArray();
        }
        value = obj.get("myAutoIncrement");
        if (value != null && value.isNumber()) {
            this.myAutoIncrement = value.asLong();
        }
        value = obj.get("myStrIndex");
        if (value != null) {
            if (value.isString()) {
                this.myStrIndex = value.asString();
            } else {
                this.myStrIndex = null;
            }
        }
    }

    public boolean getMyBool() {
        return myBool;
    }

    public synchronized void setMyBool(boolean value) {
        this.myBool = value;
        setDirty();
    }

    public byte getMyByte() {
        return myByte;
    }

    public synchronized void setMyByte(byte value) {
        this.myByte = value;
        setDirty();
    }

    public char getMyChar() {
        return myChar;
    }

    public synchronized void setMyChar(char value) {
        this.myChar = value;
        setDirty();
    }

    public short getMyShort() {
        return myShort;
    }

    public synchronized void setMyShort(short value) {
        this.myShort = value;
        setDirty();
    }

    public int getMyInt() {
        return myInt;
    }

    public synchronized void setMyInt(int value) {
        this.myInt = value;
        setDirty();
    }

    public long getMyLong() {
        return myLong;
    }

    public synchronized void setMyLong(long value) {
        this.myLong = value;
        setDirty();
    }

    public float getMyFloat() {
        return myFloat;
    }

    public synchronized void setMyFloat(float value) {
        this.myFloat = value;
        setDirty();
    }

    public double getMyDouble() {
        return myDouble;
    }

    public synchronized void setMyDouble(double value) {
        this.myDouble = value;
        setDirty();
    }

    public String getMyString() {
        return myString;
    }

    public synchronized void setMyString(String value) {
        this.myString = value;
        setDirty();
    }

    public MyDBObj getMyDBO() {
        return myDBO;
    }

    public synchronized void setMyDBO(MyDBObj value) {
        this.myDBO = value;
        setDirty();
    }

    public ExtendObj getMyExtendo() {
        return myExtendo;
    }

    public synchronized void setMyExtendo(ExtendObj value) {
        this.myExtendo = value;
        setDirty();
    }

    public boolean[] getMyBoolArray() {
        return myBoolArray;
    }

    public synchronized void setMyBoolArray(boolean[] value) {
        this.myBoolArray = value;
        setDirty();
    }

    public byte[] getMyByteArray() {
        return myByteArray;
    }

    public synchronized void setMyByteArray(byte[] value) {
        this.myByteArray = value;
        setDirty();
    }

    public short[] getMyShortArray() {
        return myShortArray;
    }

    public synchronized void setMyShortArray(short[] value) {
        this.myShortArray = value;
        setDirty();
    }

    public int[] getMyIntArray() {
        return myIntArray;
    }

    public synchronized void setMyIntArray(int[] value) {
        this.myIntArray = value;
        setDirty();
    }

    public long[] getMyLongArray() {
        return myLongArray;
    }

    public synchronized void setMyLongArray(long[] value) {
        this.myLongArray = value;
        setDirty();
    }

    public float[] getMyFloatArray() {
        return myFloatArray;
    }

    public synchronized void setMyFloatArray(float[] value) {
        this.myFloatArray = value;
        setDirty();
    }

    public double[] getMyDoubleArray() {
        return myDoubleArray;
    }

    public synchronized void setMyDoubleArray(double[] value) {
        this.myDoubleArray = value;
        setDirty();
    }

    public String[] getMyStringArray() {
        return myStringArray;
    }

    public synchronized void setMyStringArray(String[] value) {
        this.myStringArray = value;
        setDirty();
    }

    public ExtendObj[] getMyExtendoArray() {
        return myExtendoArray;
    }

    public synchronized void setMyExtendoArray(ExtendObj[] value) {
        this.myExtendoArray = value;
        setDirty();
    }

    public UBObject getMyUBObject() {
        return myUBObject;
    }

    public synchronized void setMyUBObject(UBObject value) {
        this.myUBObject = value;
        setDirty();
    }

    public UBArray getMyUBArray() {
        return myUBArray;
    }

    public synchronized void setMyUBArray(UBArray value) {
        this.myUBArray = value;
        setDirty();
    }

    public String getMyNoSerialize() {
        return myNoSerialize;
    }

    public synchronized void setMyNoSerialize(String value) {
        this.myNoSerialize = value;
        setDirty();
    }

    public long getMyAutoIncrement() {
        return myAutoIncrement;
    }

    public String getMyStrIndex() {
        return myStrIndex;
    }

    public synchronized void setMyStrIndex(String value) {
        this.myStrIndex = value;
        setDirty();
    }

    public static void install(MicroDB db) throws IOException {
        db.addChangeListener(new DefaultChangeListener() {
            @Override
            public void onBeforeInsert(Driver driver, UBValue value) {
                if(Utils.isValidObject(value, MyDBObj.SUBTYPES)) {
                    final long longValue = driver.incrementLongField("MyDBObj.myAutoIncrement_var");
                    value.asObject().put("myAutoIncrement", UBValueFactory.createInt(longValue));
                }
            }
        });
        db.addIndex("MyDBObj.myStrIndex_idx", new MapFunction<String>() {
            @Override
            public void map(UBValue value, Emitter<String> emitter) {
                if (Utils.isValidObject(value, MyDBObj.SUBTYPES)) {
                    UBValue v = value.asObject().get("myStrIndex");
                    if(v != null && v.isString()) {
                        emitter.emit(v.asString());
                    }
                }
            }
        });
    }

    public static Cursor queryByMyStrIndexIndex(MicroDB db, String min, boolean includeMin, String max, boolean includeMax) throws IOException {
        return db.queryIndex("MyDBObj.myStrIndex_idx", min, includeMin, max, includeMax);
    }

    @Override
    protected void beforeWrite() {
        System.out.println("about to write");
    }

}