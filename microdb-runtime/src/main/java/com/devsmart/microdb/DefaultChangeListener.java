package com.devsmart.microdb;


import com.devsmart.ubjson.UBValue;

import java.util.UUID;

public class DefaultChangeListener implements ChangeListener {

    @Override
    public void onAfterInsert(Driver driver, UUID key, UBValue value) {

    }

    @Override
    public void onBeforeInsert(Driver driver, UBValue value) {

    }

    @Override
    public void onBeforeDelete(Driver driver, UUID key) {

    }

    @Override
    public void onBeforeUpdate(Driver driver, UUID key, UBValue newValue) {

    }
}
