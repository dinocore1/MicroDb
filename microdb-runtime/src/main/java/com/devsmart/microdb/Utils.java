package com.devsmart.microdb;


import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBString;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    public static UBValue writeDBObj(MicroDB db, DBObject obj) {
        if (obj == null) {
            return UBValueFactory.createNull();
        } else if (db != null && obj.getDB() == db && obj.getId() != null) {
            return UBValueFactory.createString(obj.getId().toString());
        } else {
            obj.beforeWrite();
            UBObject data = UBValueFactory.createObject();
            obj.writeToUBObject(data);
            return data;
        }
    }

    public static <T extends DBObject> T readDBObj(MicroDB db, UBValue value, T shell) {
        if (value == null || value.isNull() || shell == null) {
            return null;
        } else if (db != null && value.isString()) {
            final UUID id = UUID.fromString(value.asString());
            return db.get(id, shell);
        } else if(value.isObject()) {
            shell.readFromUBObject(value.asObject());
            shell.afterRead();
            return shell;
        } else {
            LOGGER.warn("value is not an object or a string id");
            return null;
        }
    }

    public static UBValue createArrayOrNull(MicroDB db, DBObject[] input) {
        if (input == null) {
            return UBValueFactory.createNull();
        }
        UBValue[] output = new UBValue[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = writeDBObj(db, input[i]);
        }
        return UBValueFactory.createArray(output);
    }

    public static boolean isValidObject(UBValue value, UBString type) {
        if (value != null && value.isObject()) {
            UBValue typeStr = value.asObject().get("type");
            return typeStr != null && typeStr.isString() && type.equals(typeStr);
        }
        return false;
    }

}
