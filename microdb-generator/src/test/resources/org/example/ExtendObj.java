package org.example;

import com.devsmart.ubjson.UBString;
import com.devsmart.ubjson.UBValueFactory;

public class ExtendObj extends MyDBObj {
    public static final UBString TYPE = UBValueFactory.createString("ExtendObj");

    private int myExtendInt;

    public int getMyExtendInt() {
        return myExtendInt;
    }

    public void setMyExtendInt(int value) {
        this.myExtendInt = value;
        setDirty();
    }

}