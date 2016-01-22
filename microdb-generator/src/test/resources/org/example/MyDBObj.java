package org.example;

import com.devsmart.microdb.DBObject;
import com.devsmart.ubjson.UBArray;
import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBString;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;

class MyDBObj extends DBObject {

    public static final UBString TYPE = UBValueFactory.createString("SimpleDBModel");

}