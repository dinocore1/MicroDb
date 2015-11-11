package com.devsmart.microdb;


import com.devsmart.microdb.version.Change;
import com.devsmart.microdb.version.Commit;
import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;
import org.mapdb.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

public class VersionManager {

    private final MicroDB mMicroDB;
    private final MapDBDriver mMapDBDriver;
    private final Atomic.Var<UBObject> mMetadata;
    private final BTreeMap<DiffKey, Change> mDiffs;
    private final BTreeMap<UUID, Commit> mCommits;
    private Commit mCurrentVersion;


    public VersionManager(MicroDB microDB, MapDBDriver mapdbdriver) {
        mMicroDB = microDB;
        mMapDBDriver = mapdbdriver;
        mMetadata = mMapDBDriver.mMetadata;

        mDiffs = mMapDBDriver.mMapDB.createTreeMap("diffs")
                .keySerializerWrap(DiffKey.SERIALIZER)
                .valueSerializer(Change.SERIALIZER)
                .makeOrGet();

        mCommits = mMapDBDriver.mMapDB.createTreeMap("commits")
                .keySerializerWrap(Serializer.UUID)
                .valueSerializer(Commit.SERIALIZER)
                .makeOrGet();

        UBObject metadata = mMetadata.get();
        UBValue currentVersionStr = metadata.get("currentVersion");
        if(currentVersionStr == null || !currentVersionStr.isString()) {
            mCurrentVersion = Commit.newRoot();
            mCommits.put(mCurrentVersion.getId(), mCurrentVersion);
            metadata.put("currentVersion",
                    UBValueFactory.createString(mCurrentVersion.toString()));
            mMetadata.set(metadata);
        } else {
            UUID commitId = UUID.fromString(currentVersionStr.asString());
            mCurrentVersion = mCommits.get(commitId);
        }

        if(mCurrentVersion == null) {
            throw new RuntimeException("currentVersion is null");
        }

        mMapDBDriver.addChangeListener(100, mChangeListener);

    }

    private ChangeListener mChangeListener = new ChangeListener() {
        @Override
        public void onAfterInsert(Driver driver, UUID key, UBValue value) {
            addInsertChange(mCurrentVersion.getId(), key, value);
        }

        @Override
        public void onBeforeInsert(Driver driver, UBValue value) {

        }

        @Override
        public void onBeforeDelete(Driver driver, UUID key) {
            addDeleteChange(mCurrentVersion.getId(), key);

        }

        @Override
        public void onBeforeUpdate(Driver driver, UUID key, UBValue newValue) {
            addInsertChange(mCurrentVersion.getId(), key, newValue);

        }
    };

    public void addInsertChange(UUID patch, UUID objId, UBValue newValue) {
        mDiffs.put(new DiffKey(patch, objId), Change.createInsertChange(objId, newValue));
    }

    public void addDeleteChange(UUID patch, UUID objId) {
        final DiffKey key = new DiffKey(patch, objId);
        if(mDiffs.remove(key) == null) {
            mDiffs.put(key, Change.createDeleteChange(objId));
        }
    }

    public void commit() {
        mMicroDB.enqueWriteCommand(new MicroDB.WriteCommand() {
            @Override
            public void write() throws IOException {
                mCurrentVersion = Commit.withParent(mCurrentVersion);
                mCommits.put(mCurrentVersion.getId(), mCurrentVersion);

                UBObject metadata = mMetadata.get();
                metadata.put("currentVersion",
                        UBValueFactory.createString(mCurrentVersion.getId().toString()));
                mMetadata.set(metadata);
            }
        });
    }

    public boolean isDirty() {
        return mDiffs.ceilingKey(new DiffKey(mCurrentVersion.getId(), null)) != null;
    }


    private static class DiffKey implements Comparable<DiffKey> {

        public static final Serializer<DiffKey> SERIALIZER = new Serializer<DiffKey>() {
            @Override
            public void serialize(DataOutput out, DiffKey value) throws IOException {
                Serializer.UUID.serialize(out, value.patchId);
                Serializer.UUID.serialize(out, value.objKey);
            }

            @Override
            public DiffKey deserialize(DataInput in, int available) throws IOException {
                UUID patchId = Serializer.UUID.deserialize(in, available);
                UUID objKey = Serializer.UUID.deserialize(in, available);

                return new DiffKey(patchId, objKey);
            }

            @Override
            public int fixedSize() {
                return 2*16;
            }
        };

        public final UUID patchId;
        public final UUID objKey;

        public DiffKey(UUID patchId, UUID objId) {
            this.patchId = patchId;
            this.objKey = objId;
        }

        @Override
        public int compareTo(DiffKey o) {
            int retval = patchId.compareTo(o.patchId);
            if(retval != 0) {
                if(objKey != null && o.objKey != null) {
                    retval = objKey.compareTo(o.objKey);
                } else if(objKey == null && o.objKey == null){
                    retval = 0;
                } else if(objKey == null) {
                    retval = -1;
                } else {
                    return 1;
                }
            }
            return retval;
        }
    }


}
