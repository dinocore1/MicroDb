package com.devsmart.microdb;

import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

public class DBObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBObject.class);

    private UUID mId;
    private MicroDB mDB;
    protected boolean mDirty;

    protected void init(MicroDB microDB) {
        mDB = microDB;
    }

    protected void setId(UUID id) {
        mId = id;
    }

    public void writeToUBObject(UBObject data) {
        if (mId != null) {
            data.put("id", UBValueFactory.createString(mId.toString()));
        }
    }

    public void readFromUBObject(UBObject data) {
        UBValue value = data.get("id");
        if (value != null && value.isString()) {
            mId = UUID.fromString(value.asString());
        }
    }

    protected void beforeWrite() {

    }

    protected void afterRead() {

    }

    public UUID getId() {
        return mId;
    }

    public void save() throws IOException {
        if (mDB != null && mId != null){
            mDB.save(this);
        } else {
            LOGGER.warn("save object but DBObject does not reference a database: {}", this);
        }
    }

    public MicroDB getDB() {
        return mDB;
    }

    public void delete() throws IOException {
        if (mDB != null && mId != null) {
            mDB.delete(this);
        } else {
            LOGGER.warn("delete object but DBObject does not reference a database: {}", this);
        }
    }

    public void setDirty() {
        mDirty = true;
    }
}