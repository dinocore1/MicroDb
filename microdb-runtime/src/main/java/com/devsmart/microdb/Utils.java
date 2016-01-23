package com.devsmart.microdb;


import com.devsmart.ubjson.*;

public class Utils {

    public static UBValue writeDBObj(DBObject obj) {
        if(obj == null) {
            return UBValueFactory.createNull();
        } else {
            UBObject data = UBValueFactory.createObject();
            obj.writeToUBObject(data);
            return data;
        }
    }

    public static <T extends DBObject> T readDBObj(UBValue value, T shell) {
        if (value.isNull() || !value.isObject()) {
            return null;
        } else {
            shell.readFromUBObject(value.asObject());
            return shell;
        }
    }


    public static UBValue createArrayOrNull(DBObject[] input) {
        if(input == null) {
            return UBValueFactory.createNull();
        }
        UBValue[] output = new UBValue[input.length];
        for(int i=0;i<input.length;i++) {
            output[i] = writeDBObj(input[i]);
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
