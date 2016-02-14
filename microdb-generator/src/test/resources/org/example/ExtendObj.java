package org.example;

import com.devsmart.microdb.Cursor;
import com.devsmart.microdb.DefaultChangeListener;
import com.devsmart.microdb.Driver;
import com.devsmart.microdb.Emitter;
import com.devsmart.microdb.MapFunction;
import com.devsmart.microdb.MicroDB;
import com.devsmart.microdb.Utils;
import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBString;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;
import java.io.IOException;

public class ExtendObj extends MyDBObj {
    public static final UBString TYPE = UBValueFactory.createString("ExtendObj");

    private int myExtendInt;

    private long myTestId;

    @Override
    public void writeToUBObject(UBObject obj) {
        super.writeToUBObject(obj);
        final MicroDB db = getDB();
        obj.put("type", TYPE);
        obj.put("myExtendInt", UBValueFactory.createInt(myExtendInt));
        obj.put("myTestId", UBValueFactory.createInt(myTestId));
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
        value = obj.get("myTestId");
        if (value != null) {
            this.myTestId = value.asLong();
        }
    }

    public int getMyExtendInt() {
        return myExtendInt;
    }

    public void setMyExtendInt(int value) {
        this.myExtendInt = value;
        setDirty();
    }

    public long getMyTestId() {
        return myTestId;
    }

    public static void install(MicroDB db) throws IOException {
        db.addChangeListener(new DefaultChangeListener() {
            @Override
            public void onBeforeInsert(Driver driver, UBValue value) {
                if(Utils.isValidObject(value, ExtendObj.TYPE)) {
                    final long longValue = driver.incrementLongField("ExtendObj.myTestId_var");
                    value.asObject().put("myTestId", UBValueFactory.createInt(longValue));
                }
            }
        });
        db.addIndex("ExtendObj.myTestId_idx", new MapFunction<Long>() {
            @Override
            public void map(UBValue value, Emitter<Long> emitter) {
                if (Utils.isValidObject(value, ExtendObj.TYPE)) {
                    UBValue v = value.asObject().get("myTestId");
                    if (v != null && v.isInteger()) {
                        emitter.emit(v.asLong());
                    }
                }
            }
        });
    }

    public static Cursor queryByMyTestIdIndex(MicroDB db, Long min, boolean includeMin, Long max, boolean includeMax) throws IOException {
        return db.queryIndex("ExtendObj.myTestId_idx", min, includeMin, max, includeMax);
    }

}