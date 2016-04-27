package com.devsmart.microdb.version;


import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class Commit {

    private UUID mId;
    private UUID mParent;
    private Date mDate;
    private String mMessage;

    private Commit() {
    }

    public static Commit newRoot() {
        Commit retval = new Commit();
        retval.mId = new UUID(0, 0);
        retval.mParent = new UUID(0, 0);
        retval.mDate = new Date();

        return retval;
    }

    public static Commit withParent(Commit parent) {
        return withParent(parent.getId());
    }

    public static Commit withParent(UUID parent) {
        return withParentAndId(parent, UUID.randomUUID());
    }

    public static Commit withParentAndId(Commit parent, UUID commitId) {
        return withParentAndId(parent.getId(), commitId);
    }

    public static Commit withParentAndId(UUID parent, UUID commitId) {
        Commit retval = new Commit();
        retval.mId = commitId;
        retval.mParent = parent;
        retval.mDate = new Date();
        return retval;
    }

    public UUID getParent() {
        return mParent;
    }

    public static class CommitSerializer implements Serializer<Commit>, Serializable {
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
    }

    public static final Serializer<Commit> SERIALIZER = new CommitSerializer();

    public UUID getId() {
        return mId;
    }
}
