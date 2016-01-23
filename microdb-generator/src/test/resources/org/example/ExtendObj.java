package org.example;

import com.devsmart.microdb.MicroDB;
import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBString;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;

public class ExtendObj extends MyDBObj {
    public static final UBString TYPE = UBValueFactory.createString("ExtendObj");

    private int myExtendInt;

    @Override
    public void writeToUBObject(UBObject obj) {
        super.writeToUBObject(obj);
        final MicroDB db = getDB();
        obj.put("myExtendInt", UBValueFactory.createInt(myExtendInt));
    }

    @Override
    public void readFromUBObject(UBObject obj) {
        super.readFromUBObject(obj);
        final MicroDB db = getDB();
        UBValue value = null;
        value = obj.get("myExtendInt");
        if (value != null) {
            this.myExtendInt = value.asInt();
        }
    }

    public int getMyExtendInt() {
        return myExtendInt;
    }

    public void setMyExtendInt(int value) {
        this.myExtendInt = value;
        setDirty();
    }
}