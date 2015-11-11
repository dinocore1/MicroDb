package com.devsmart.microdb.version;


import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class DatabaseVersion {

    private UUID mId;
    private UUID mParent;
    private Date mDate;
    private String mMessage;

    private DatabaseVersion() {}

    public static DatabaseVersion newRoot() {
        DatabaseVersion retval = new DatabaseVersion();
        retval.mId = UUID.randomUUID();
        retval.mDate = new Date();

        return retval;
    }

    public static DatabaseVersion withParent(DatabaseVersion parent) {
        return withParent(parent.getId());
    }

    public static DatabaseVersion withParent(UUID parent) {
        DatabaseVersion retval = new DatabaseVersion();
        retval.mId = UUID.randomUUID();
        retval.mParent = parent;
        retval.mDate = new Date();
        return retval;
    }

    public static final Serializer<DatabaseVersion> SERIALIZER = new Serializer<DatabaseVersion>() {
        @Override
        public void serialize(DataOutput out, DatabaseVersion value) throws IOException {
            Serializer.UUID.serialize(out, value.mId);
            Serializer.UUID.serialize(out, value.mParent);

        }

        @Override
        public DatabaseVersion deserialize(DataInput in, int available) throws IOException {
            DatabaseVersion retval = new DatabaseVersion();
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
