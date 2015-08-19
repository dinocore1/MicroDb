package com.devsmart.microdb;

import com.devsmart.microdb.ubjson.UBObject;
import pkg.project.SimpleDBModel;
import com.devsmart.microdb.ubjson.UBValueFactory;

public final class SimpleDBModel_pxy extends SimpleDBModel {

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

    private SimpleDBModel_pxy mCache_internal;
    public SimpleDBModel getInternal() {
        if(mCache_internal == null) {
            UBObject data = mData.get("internal").asObject();
            mCache_internal = new SimpleDBModel_pxy();
            mCache_internal.init(data);
        }
        return mCache_internal;
    }

    public void setInternal(SimpleDBModel_pxy value) {
        mData.set("internal", value.mData);
        mCache_internal = value;
    }
}