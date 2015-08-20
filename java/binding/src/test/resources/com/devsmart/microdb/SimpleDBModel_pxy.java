package com.devsmart.microdb;

import com.devsmart.microdb.ubjson.UBObject;
import com.devsmart.microdb.ubjson.UBValue;
import pkg.project.SimpleDBModel;
import com.devsmart.microdb.ubjson.UBValueFactory;

import java.util.TreeMap;

public final class SimpleDBModel_pxy extends SimpleDBModel {

    public static UBObject to(SimpleDBModel value) {
        TreeMap<String, UBValue> retval = new TreeMap<String, UBValue>();
        retval.put("myString", UBValueFactory.createString(value.getMyString()));
        retval.put("myInt", UBValueFactory.createInt(value.getMyInt()));
        retval.put("internal", value.getInternal() == null ? UBValueFactory.createNull() : SimpleDBModel_pxy.to(value.getInternal()));
        retval.put("link", value.link.getId());
        return UBValueFactory.createObject(retval);
    }

    public void init(UBObject obj, MicroDB db) {
        super.init(obj, db);
        setMyString(obj.get("myString").asString());
        setMyInt(obj.get("myInt").asInt());
        {
            SimpleDBModel_pxy tmp = new SimpleDBModel_pxy();
            tmp.init(obj.get("internal").asObject(), db);
            setInternal(tmp);
        }
        link = new Link<SimpleDBModel>(obj.get("link"), db, SimpleDBModel_pxy.class);
    }


    @Override
    public void setMyString(String myString) {
        super.setMyString(myString);
        mDirty = true;
    }

    @Override
    public void setMyInt(int value) {
        super.setMyInt(value);
        mDirty = true;
    }

    @Override
    public void setInternal(SimpleDBModel value) {
        super.setInternal(value);
        mDirty = true;
    }

    public SimpleDBModel getLink() {
        return link.get();
    }

    public void setLink(SimpleDBModel value) {
        link.set(value);
    }
}