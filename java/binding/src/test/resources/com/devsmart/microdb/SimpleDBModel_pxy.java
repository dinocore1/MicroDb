package com.devsmart.microdb;

import com.devsmart.microdb.ubjson.UBObject;
import pkg.project.SimpleDBModel;
import com.devsmart.microdb.ubjson.UBValueFactory;

public final class SimpleDBModel_pxy extends SimpleDBModel {

    void init(UBObject data, MicroDB db) {
        super.init(data, db);
    }

    public String getMyString() {
        return mData.get("myString").asString();
    }

    public void setMyString(String value) {
        mData.set("myString", UBValueFactory.createString(value));
    }

    public int getMyInt() {
        return mData.get("myInt").asInt();
    }

    public void setMyInt(int value) {
        mData.set("myInt", UBValueFactory.createInt(value));
    }
}