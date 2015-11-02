package com.devsmart.microdb;

import com.devsmart.ubjson.UBValue;

import java.util.UUID;

public interface ChangeListener {

    void onAfterInsert(Driver driver, UUID key, UBValue value);
    void onBeforeInsert(Driver driver, UBValue value);
    void onBeforeDelete(Driver driver, UUID key);
    void onBeforeUpdate(Driver driver, UUID key, UBValue newValue);
}
