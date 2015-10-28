package com.devsmart.examples.intro.model;


import com.devsmart.microdb.DBObject;
import com.devsmart.microdb.annotations.DBObj;

@DBObj
public class Address extends DBObject {

    private String addressLine;

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

}
