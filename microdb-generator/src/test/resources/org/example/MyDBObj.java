package org.example;

import com.devsmart.microdb.DBObject;
import com.devsmart.ubjson.UBArray;
import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBString;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;

class MyDBObj extends DBObject {

    public static final UBString TYPE = UBValueFactory.createString("SimpleDBModel");

    private int myInt;

    public int getMyInt() {
        return myInt;
    }

    public void setMyInt(int value) {
        this.myInt = value;
        setDirty();
    }

}