package com.devsmart.microdb;

import com.devsmart.microdb.ubjson.UBObject;
import com.devsmart.microdb.ubjson.UBValue;
import com.devsmart.microdb.ubjson.UBValueFactory;
import java.util.TreeMap;
import pkg.project.SimpleDBModel;



public final class SimpleDBModel_pxy extends SimpleDBModel {

    public static UBValue to(SimpleDBModel value) {
        if(value == null) {
            return UBValueFactory.createNull();
        }
        TreeMap<String, UBValue> retval = new TreeMap<String, UBValue>();
        retval.put("myString", UBValueFactory.createString(value.getMyString()));
        retval.put("myBool", UBValueFactory.createBool(value.getMyBool()));
        retval.put("myByte", UBValueFactory.createInt(value.getMyByte()));
        retval.put("myShort", UBValueFactory.createInt(value.getMyShort()));
        retval.put("myInt", UBValueFactory.createInt(value.getMyInt()));
        retval.put("myLong", UBValueFactory.createInt(value.getMyLong()));
        retval.put("internal", SimpleDBModel_pxy.to(value.getInternal()));
        retval.put("link", value.link.getId());
        return UBValueFactory.createObject(retval);
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
}