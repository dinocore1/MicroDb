package com.devsmart.examples.intro.model;

import com.devsmart.microdb.DBObject;
import com.devsmart.microdb.annotations.DBObj;

@DBObj
public class Person extends DBObject {

    private String firstName;
    private String lastName;


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
