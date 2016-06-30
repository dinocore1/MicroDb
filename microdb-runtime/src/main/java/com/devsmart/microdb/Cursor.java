package com.devsmart.microdb;


public interface Cursor {

    boolean moveToFirst();
    boolean moveToLast();
    boolean moveToNext();
    boolean moveToPrevious();
    boolean move(int pos);

    boolean isFirst();
    boolean isLast();

    boolean isBeforeFirst();
    boolean isAfterLast();

    int getPosition();
    int getCount();

    Row getRow();

}
