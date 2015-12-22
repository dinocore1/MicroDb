package com.devsmart.microdb;


import java.io.IOException;

public interface Dataset {

    void install(MicroDB db) throws IOException;
}
