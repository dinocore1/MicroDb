package com.devsmart.microdb.version;


import com.devsmart.microdb.Driver;
import com.devsmart.microdb.MapDBDriver;
import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

public class Change {

    public static final byte TYPE_INSERT = 0;
    public static final byte TYPE_DELETE = 1;

    public static final class ChangeSerializer implements Serializer<Change>, Serializable {
        @Override
        public void serialize(DataOutput out, Change value) throws IOException {
            out.writeByte(value.mType);
            Serializer.UUID.serialize(out, value.mObjId);
            MapDBDriver.SERIALIZER_UBVALUE.serialize(out, value.mValue);
        }

        @Override
        public Change deserialize(DataInput in, int available) throws IOException {
            Change retval = new Change();
            retval.mType = in.readByte();
            retval.mObjId = Serializer.UUID.deserialize(in, available);
            retval.mValue = MapDBDriver.SERIALIZER_UBVALUE.deserialize(in, available);

            return retval;
        }

        @Override
        public int fixedSize() {
            return -1;
        }
    }

    public static final Serializer<Change> SERIALIZER = new ChangeSerializer();

    public static Change createDeleteChange(UUID key) {
        Change retval = new Change();
        retval.mType = TYPE_DELETE;
        retval.mObjId = key;
        retval.mValue = UBValueFactory.createNull();

        return retval;
    }

    public static Change createInsertChange(UUID key, UBValue newValue) {
        Change retval = new Change();
        retval.mType = TYPE_INSERT;
        retval.mObjId = key;
        retval.mValue = newValue;

        return retval;
    }

    private byte mType;
    private UUID mObjId;
    private UBValue mValue;

    public static Change parse(UBObject obj) {
        Change retval = new Change();
        retval.mType = obj.get("t").asByte();
        retval.mObjId = UUID.fromString(obj.get("k").asString());
        retval.mValue = obj.get("v");

        return retval;
    }

    public byte getType() {
        return mType;
    }

    public UUID getObjId() {
        return mObjId;
    }

    public UBValue getObjValue() {
        return mValue;
    }

    public void apply(Driver driver) throws IOException {
        switch (mType) {
            case TYPE_DELETE:
                driver.delete(mObjId);
                break;

            case TYPE_INSERT:
                driver.update(mObjId, mValue);
                break;

            default:
                throw new RuntimeException("unknown change type: " + mType);
        }
    }

    @Override
    public String toString() {
        String retval = (mType == TYPE_INSERT ? "+" : "-") + mObjId.toString().substring(0, 5);
        return retval;
    }
}
