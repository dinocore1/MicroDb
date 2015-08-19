package com.devsmart.microdb;

import com.devsmart.microdb.ubjson.UBObject;
import com.devsmart.microdb.ubjson.UBValue;
import pkg.project.SimpleDBModel;
import com.devsmart.microdb.ubjson.UBValueFactory;

import java.util.TreeMap;

public final class SimpleDBModel_pxy extends SimpleDBModel {

    public static UBObject to(SimpleDBModel value) {
        TreeMap<String, UBValue> retval = new TreeMap<String, UBValue>();
        retval.put("myString", UBValueFactory.createString(value.myString));
        retval.put("myInt", UBValueFactory.createInt(value.getMyInt()));
        retval.put("internal", value.internal == null ? UBValueFactory.createNull() : SimpleDBModel_pxy.to(value.internal));
        retval.put("link", value.link.getId());
        return UBValueFactory.createObject(retval);
    }

    public void init(UBObject obj, MicroDB db) {
        super.init(obj, db);
        myString = obj.get("myString").asString();
        setMyInt(obj.get("myInt").asInt());
        internal = new SimpleDBModel_pxy();
        ((SimpleDBModel_pxy)internal).init(obj.get("internal").asObject(), db);
        link = new Link<SimpleDBModel>(obj.get("link"), db, SimpleDBModel_pxy.class);
    }


    public SimpleDBModel getLink() {
        return link.get();
    }

    public void setLink(SimpleDBModel value) {
        link.set(value);
    }
}