package com.devsmart.microdb;


import com.devsmart.ubjson.*;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import java.util.Arrays;
import java.util.UUID;

public class Utils {

    public static UBValue writeDBObj(MicroDB db, DBObject obj) {
        if(obj == null) {
            return UBValueFactory.createNull();
        } else if(db != null && obj.getDB() == db && obj.getId() != null){
            return UBValueFactory.createString(obj.getId().toString());
        } else {
            UBObject data = UBValueFactory.createObject();
            obj.writeToUBObject(data);
            return data;
        }
    }

    public static <T extends DBObject> T readDBObj(MicroDB db, UBValue value, T shell) {
        if (value == null || value.isNull() || !value.isObject() || shell == null) {
            return null;
        } else if(db != null && value.isString()){
            final UUID id = UUID.fromString(value.asString());
            return db.get(id, shell);
        } else {
            shell.readFromUBObject(value.asObject());
            return shell;
        }
    }

    public static UBValue createArrayOrNull(MicroDB db, DBObject[] input) {
        if(input == null) {
            return UBValueFactory.createNull();
        }
        UBValue[] output = new UBValue[input.length];
        for(int i=0;i<input.length;i++) {
            output[i] = writeDBObj(db, input[i]);
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
