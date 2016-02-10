package com.devsmart.microdb;


public interface Cursor {

    void seekToBegining();
    void seekToEnd();
    int getPosition();
    boolean moveToPosition(int pos);
    boolean next();
    boolean prev();
    Row get();
    int getCount();
}
