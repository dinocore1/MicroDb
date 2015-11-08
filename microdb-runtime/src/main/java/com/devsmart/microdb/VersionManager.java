package com.devsmart.microdb;


import com.devsmart.microdb.version.Change;
import com.devsmart.microdb.version.DatabaseVersion;
import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;
import org.mapdb.Atomic;
import org.mapdb.BTreeMap;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

public class VersionManager {

    private final MapDBDriver mMapDBDriver;
    private final Atomic.Var<UBObject> mMetadata;
    private final BTreeMap<Object, Object> mDiffs;
    private final DatabaseVersion mCurrentVersion;

    public VersionManager(MapDBDriver mapdbdriver) {
        mMapDBDriver = mapdbdriver;
        mMetadata = mMapDBDriver.mMetadata;

        UBObject metadata = mMetadata.get();
        UBValue currentVersion = metadata.get("currentVersion");
        if(currentVersion == null || !currentVersion.isObject()) {
            mCurrentVersion = new DatabaseVersion();
            metadata.put("currentVersion", mCurrentVersion.serialize());
            mMetadata.set(metadata);
        } else {
            mCurrentVersion = DatabaseVersion.deserialize(currentVersion.asObject());
        }

        mDiffs = mMapDBDriver.mMapDB.createTreeMap("diffs")
                .keySerializerWrap(DiffKey.SERIALIZER)
                .valueSerializer(Change.SERIALIZER)
                .makeOrGet();

        mMapDBDriver.addChangeListener(100, mChangeListener);

    }

    private ChangeListener mChangeListener = new ChangeListener() {
        @Override
        public void onAfterInsert(Driver driver, UUID key, UBValue value) {
            addInsertChange(mCurrentVersion.getBase(), key, value);
        }

        @Override
        public void onBeforeInsert(Driver driver, UBValue value) {

        }

        @Override
        public void onBeforeDelete(Driver driver, UUID key) {
            addDeleteChange(mCurrentVersion.getBase(), key);

        }

        @Override
        public void onBeforeUpdate(Driver driver, UUID key, UBValue newValue) {
            addInsertChange(mCurrentVersion.getBase(), key, newValue);

        }
    };

    public void addInsertChange(UUID patch, UUID objId, UBValue newValue) {
        mDiffs.put(new DiffKey(patch, objId), Change.createInsertChange(objId, newValue));
    }

    public void addDeleteChange(UUID patch, UUID objId) {
        mDiffs.put(new DiffKey(patch, objId), Change.createDeleteChange(objId));
    }

    public void commit() {

    }


    private static class DiffKey {

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

    }


}
