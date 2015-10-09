package com.devsmart.microdb;


import com.devsmart.microdb.ubjson.UBArray;
import com.devsmart.microdb.ubjson.UBObject;
import com.devsmart.microdb.ubjson.UBValueFactory;

public class Utils {


    public static UBArray toUBArray(DBObject[] input) {
        UBObject[] output = new UBObject[input.length];
        for(int i=0;i<input.length;i++) {
            output[i] = new UBObject();
            input[i].writeUBObject(output[i]);
        }
        return UBValueFactory.createArray(output);
    }

}
