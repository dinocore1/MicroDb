package com.devsmart.microdb;


public interface Cursor {

    void seekToBegining();
    void seekToEnd();
    boolean next();
    boolean prev();
    Row get();
}
