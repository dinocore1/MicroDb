package com.devsmart.microdb;

import com.devsmart.ubjson.UBArray;
import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBString;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;
import pkg.project.MyDatum;
import pkg.project.SimpleDBModel;


public final class SimpleDBModel_pxy extends SimpleDBModel {

    public static final UBString TYPE = UBValueFactory.createString("SimpleDBModel");

    @Override
    public void writeToUBObject(UBObject data) {
        super.writeToUBObject(data);
        data.put("type", TYPE);
        data.put("myString", UBValueFactory.createStringOrNull(getMyString()));
        data.put("myBool", UBValueFactory.createBool(getMyBool()));
        data.put("myByte", UBValueFactory.createInt(getMyByte()));
        data.put("myShort", UBValueFactory.createInt(getMyShort()));
        data.put("myInt", UBValueFactory.createInt(getMyInt()));
        data.put("myLong", UBValueFactory.createInt(getMyLong()));
        data.put("indexLong", UBValueFactory.createInt(getIndexLong()));

        {
            SimpleDBModel inst = getInternal();
            if(inst == null) {
                data.put("internal", UBValueFactory.createNull());
            } else {
                UBObject obj = UBValueFactory.createObject();
                inst.writeToUBObject(obj);
                data.put("internal", obj);
            }
        }
        if(link == null) {
            data.put("link", UBValueFactory.createNull());
        } else {
            data.put("link", link.getId());
        }
        data.put("myFloatArray", UBValueFactory.createArrayOrNull(getMyFloatArray()));
        data.put("myDoubleArray", UBValueFactory.createArrayOrNull(getMyDoubleArray()));
        data.put("addresses", Utils.createArrayOrNull(getAddresses()));
        data.put("genericValue", UBValueFactory.createValueOrNull(getGenericValue()));

        {
            MyDatum datum = getMyDatum();
            if(datum == null) {
                data.put("myDatum", UBValueFactory.createNull());
            } else {
                data.put("myDatum", datum.toUBValue());
            }
        }

    }

    @Override
    public void readFromUBObject(UBObject obj) {
        super.readFromUBObject(obj);
        if (obj.containsKey("myString")) {
            super.setMyString(obj.get("myString").asString());
        }
        if (obj.containsKey("myBool")) {
            super.setMyBool(obj.get("myBool").asBool());
        }
        if (obj.containsKey("myByte")) {
            super.setMyByte(obj.get("myByte").asByte());
        }
        if (obj.containsKey("myShort")) {
            super.setMyShort(obj.get("myShort").asShort());
        }
        if (obj.containsKey("myInt")) {
            super.setMyInt(obj.get("myInt").asInt());
        }
        if (obj.containsKey("myLong")) {
            super.setMyLong(obj.get("myLong").asLong());
        }
        if (obj.containsKey("indexLong")) {
            super.setIndexLong(obj.get("indexLong").asLong());
        }
        if (obj.containsKey("internal")) {
            UBValue value = obj.get("internal");
            if(value.isNull()) {
                super.setInternal(null);
            } else {
                SimpleDBModel_pxy tmp = new SimpleDBModel_pxy();
                tmp.init(null, getDB());
                tmp.readFromUBObject(value.asObject());
                super.setInternal(tmp);
            }
        }
        link = new Link<SimpleDBModel>(obj.get("link"), getDB(), SimpleDBModel_pxy.class);
        if (obj.containsKey("myFloatArray")) {
            UBValue value = obj.get("myFloatArray");
            if(value.isNull()) {
                super.setMyFloatArray(null);
            } else {
                super.setMyFloatArray(value.asFloat32Array());
            }
        }
        if (obj.containsKey("myDoubleArray")) {
            UBValue value = obj.get("myDoubleArray");
            if(value.isNull()) {
                super.setMyDoubleArray(null);
            } else {
                super.setMyDoubleArray(value.asFloat64Array());
            }
        }
        if (obj.containsKey("addresses")) {
            UBValue value = obj.get("addresses");
            if(value.isNull()) {
                super.setAddresses(null);
            } else {
                UBArray input = value.asArray();
                final int size = input.size();
                SimpleDBModel_pxy[] output = new SimpleDBModel_pxy[size];
                for (int i = 0; i < size; i++) {
                    SimpleDBModel_pxy tmp = new SimpleDBModel_pxy();
                    tmp.init(null, getDB());
                    tmp.readFromUBObject(input.get(i).asObject());
                    output[i] = tmp;
                }
                super.setAddresses(output);
            }
        }
        if (obj.containsKey("genericValue")) {
            UBValue value = obj.get("genericValue");
            if(value.isNull()){
                super.setGenericValue(null);
            } else {
                super.setGenericValue((UBValue) value);
            }
        }
        if (obj.containsKey("myDatum")) {
            MyDatum datum;
            UBValue value = obj.get("myDatum");
            if(value.isNull()) {
                datum = null;
            } else {
                datum = new MyDatum();
                datum.fromUBValue(value);
            }
            super.setMyDatum(datum);
        }
    }


    @Override
    public void setMyString(String value) {
        super.setMyString(value);
        mDirty = true;
    }

    @Override
    public void setMyBool(boolean value) {
        super.setMyBool(value);
        mDirty = true;
    }

    @Override
    public void setMyByte(byte value) {
        super.setMyByte(value);
        mDirty = true;
    }

    @Override
    public void setMyShort(short value) {
        super.setMyShort(value);
        mDirty = true;
    }

    @Override
    public void setMyInt(int value) {
        super.setMyInt(value);
        mDirty = true;
    }

    @Override
    public void setMyLong(long value) {
        super.setMyLong(value);
        mDirty = true;
    }

    @Override
    public void setIndexLong(long value) {
        super.setIndexLong(value);
        mDirty = true;
    }

    @Override
    public void setInternal(SimpleDBModel value) {
        super.setInternal(value);
        mDirty = true;
    }

    @Override
    public void setMyFloatArray(float[] value) {
        super.setMyFloatArray(value);
        mDirty = true;
    }

    @Override
    public void setMyDoubleArray(double[] value) {
        super.setMyDoubleArray(value);
        mDirty = true;
    }

    @Override
    public void setAddresses(SimpleDBModel[] value) {
        super.setAddresses(value);
        mDirty = true;
    }

    @Override
    public void setGenericValue(UBValue value) {
        super.setGenericValue(value);
        mDirty = true;
    }

    @Override
    public void setMyDatum(MyDatum value) {
        super.setMyDatum(value);
        mDirty = true;
    }
}