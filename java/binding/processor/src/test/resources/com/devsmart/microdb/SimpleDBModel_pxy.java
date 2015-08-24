package com.devsmart.microdb;

import com.devsmart.microdb.ubjson.UBObject;
import com.devsmart.microdb.ubjson.UBValueFactory;
import pkg.project.SimpleDBModel;


public final class SimpleDBModel_pxy extends SimpleDBModel {

    @Override
    public void writeUBObject(UBObject data) {
        super.writeUBObject(data);
        data.set("myString", UBValueFactory.createString(getMyString()));
        data.set("myBool", UBValueFactory.createBool(getMyBool()));
        data.set("myByte", UBValueFactory.createInt(getMyByte()));
        data.set("myShort", UBValueFactory.createInt(getMyShort()));
        data.set("myInt", UBValueFactory.createInt(getMyInt()));
        data.set("myLong", UBValueFactory.createInt(getMyLong()));

        {
            SimpleDBModel inst = getInternal();
            if(inst == null) {
                data.set("internal", UBValueFactory.createNull());
            } else {
                UBObject obj = new UBObject();
                inst.writeUBObject(obj);
                data.set("internal", obj);
            }
        }
        data.set("link", link.getId());
        data.set("myFloatArray", UBValueFactory.createArray(getMyFloatArray()));
        data.set("myDoubleArray", UBValueFactory.createArray(getMyDoubleArray()));
    }

    @Override
    public void init(UBObject obj, MicroDB db) {
        super.init(obj, db);
        setMyString(obj.get("myString").asString());
        setMyBool(obj.get("myBool").asBool());
        setMyByte(obj.get("myByte").asByte());
        setMyShort(obj.get("myShort").asShort());
        setMyInt(obj.get("myInt").asInt());
        setMyLong(obj.get("myLong").asLong());
        {
            SimpleDBModel_pxy tmp = new SimpleDBModel_pxy();
            tmp.init(obj.get("internal").asObject(), db);
            setInternal(tmp);
        }
        link = new Link<SimpleDBModel>(obj.get("link"), db, SimpleDBModel_pxy.class);
        setMyFloatArray(obj.get("myFloatArray").asFloat32Array());
        setMyDoubleArray(obj.get("myDoubleArray").asFloat64Array());
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