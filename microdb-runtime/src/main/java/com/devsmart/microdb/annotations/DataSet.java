package com.devsmart.microdb.annotations;

import com.devsmart.microdb.DBObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface DataSet {

    Class<? extends DBObject>[] objects();
}
