package com.devsmart.microdb;

import com.devsmart.microdb.ubjson.UBObject;
import com.devsmart.microdb.ubjson.UBString;
import com.devsmart.microdb.ubjson.UBValueFactory;
import pkg.project.SimpleDBModel;


public final class SimpleDBModel_pxy extends SimpleDBModel {

    private static final UBString TYPE = UBValueFactory.createString("SimpleDBModel");

    @Override
    public void writeUBObject(UBObject data) {
        super.writeUBObject(data);
        data.put("myString", UBValueFactory.createString(getMyString()));
        data.put("myBool", UBValueFactory.createBool(getMyBool()));
        data.put("myByte", UBValueFactory.createInt(getMyByte()));
        data.put("myShort", UBValueFactory.createInt(getMyShort()));
        data.put("myInt", UBValueFactory.createInt(getMyInt()));
        data.put("myLong", UBValueFactory.createInt(getMyLong()));

        {
            SimpleDBModel inst = getInternal();
            if(inst == null) {
                data.put("internal", UBValueFactory.createNull());
            } else {
                UBObject obj = new UBObject();
                inst.writeUBObject(obj);
                data.put("internal", obj);
            }
        }
        data.put("link", link.getId());
        data.put("myFloatArray", UBValueFactory.createArray(getMyFloatArray()));
        data.put("myDoubleArray", UBValueFactory.createArray(getMyDoubleArray()));
    }

    @Override
    public void init(UBObject obj, MicroDB db) {
        super.init(obj, db);
        obj.put("type", TYPE);
        if(obj.containsKey("myString")) {
            setMyString(obj.get("myString").asString());
        }
        if(obj.containsKey("myBool")) {
            setMyBool(obj.get("myBool").asBool());
        }
        if(obj.containsKey("myByte")) {
            setMyByte(obj.get("myByte").asByte());
        }
        if(obj.containsKey("myShort")) {
            setMyShort(obj.get("myShort").asShort());
        }
        if(obj.containsKey("myInt")) {
            setMyInt(obj.get("myInt").asInt());
        }
        if(obj.containsKey("myLong")) {
            setMyLong(obj.get("myLong").asLong());
        }
        if(obj.containsKey("internal")) {
            SimpleDBModel_pxy tmp = new SimpleDBModel_pxy();
            tmp.init(obj.get("internal").asObject(), db);
            setInternal(tmp);
        }
        link = new Link<SimpleDBModel>(obj.get("link"), db, SimpleDBModel_pxy.class);
        if(obj.containsKey("myFloatArray")) {
            setMyFloatArray(obj.get("myFloatArray").asFloat32Array());
        }
        if(obj.containsKey("myDoubleArray")) {
            setMyDoubleArray(obj.get("myDoubleArray").asFloat64Array());
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
}