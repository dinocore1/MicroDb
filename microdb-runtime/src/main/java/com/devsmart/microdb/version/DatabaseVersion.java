package com.devsmart.microdb.version;


import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBValueFactory;

import java.util.UUID;

public class DatabaseVersion {


    private UUID mBase;
    private UUID mParent;
    private UUID mUpdatingTo;

    public DatabaseVersion() {
        mBase = UUID.randomUUID();
    }

    public static DatabaseVersion deserialize(UBObject obj) {
        DatabaseVersion retval = new DatabaseVersion();
        retval.mBase = UUID.fromString(obj.get("base").asString());

        return retval;
    }

    public UBObject serialize() {
        UBObject retval = UBValueFactory.createObject();
        retval.put("base", UBValueFactory.createString(mBase.toString()));

        return retval;
    }

    public UUID getBase() {
        return mBase;
    }
}
