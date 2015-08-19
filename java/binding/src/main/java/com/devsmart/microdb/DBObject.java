package com.devsmart.microdb;

import com.devsmart.microdb.ubjson.UBObject;

public class DBObject {

    protected UBObject mData;

    protected void init(UBObject data) {
        mData = data;
    }
	
}