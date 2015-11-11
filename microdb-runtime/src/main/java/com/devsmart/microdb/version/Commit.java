package com.devsmart.microdb.version;


import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class Commit {

    private UUID mId;
    private UUID mParent;
    private Date mDate;
    private String mMessage;

    private Commit() {}

    public static Commit newRoot() {
        Commit retval = new Commit();
        retval.mId = UUID.randomUUID();
        retval.mDate = new Date();

        return retval;
    }

    public static Commit withParent(Commit parent) {
        return withParent(parent.getId());
    }

    public static Commit withParent(UUID parent) {
        Commit retval = new Commit();
        retval.mId = UUID.randomUUID();
        retval.mParent = parent;
        retval.mDate = new Date();
        return retval;
    }

    public static final Serializer<Commit> SERIALIZER = new Serializer<Commit>() {
        @Override
        public void serialize(DataOutput out, Commit value) throws IOException {
            Serializer.UUID.serialize(out, value.mId);
            Serializer.UUID.serialize(out, value.mParent);

        }

        @Override
        public Commit deserialize(DataInput in, int available) throws IOException {
            Commit retval = new Commit();
            retval.mId = Serializer.UUID.deserialize(in, available);
            retval.mParent = Serializer.UUID.deserialize(in, available);
            return retval;
        }

        @Override
        public int fixedSize() {
            return -1;
        }
    };

    public UUID getId() {
        return mId;
    }
}
