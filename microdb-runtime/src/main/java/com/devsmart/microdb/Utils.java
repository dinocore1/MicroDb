package com.devsmart.microdb;


import com.devsmart.ubjson.*;

public class Utils {


    public static UBArray toUBArray(DBObject[] input) {
        UBObject[] output = new UBObject[input.length];
        for(int i=0;i<input.length;i++) {
            output[i] = UBValueFactory.createObject();
            input[i].writeToUBObject(output[i]);
        }
        return UBValueFactory.createArray(output);
    }

    public static boolean isValidObject(UBValue value, UBString type) {
        if(value != null && value.isObject()) {
            UBValue typeStr = value.asObject().get("type");
            return typeStr != null && typeStr.isString() && type.equals(typeStr);
        }
        return false;
    }

}
