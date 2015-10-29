package com.devsmart.microdb;


public class ObjRangeIterator<T extends DBObject> extends ObjectIterator<String, T> {

    private T mNextValue;

    public ObjRangeIterator(KeyIterator<String> keyIterator, MicroDB db, Class<T> classType) {
        super(keyIterator, db, classType);
        mKeyIterator.seekTo(classType.getSimpleName());
    }

    @Override
    public void seekTo(String key) {
    }

    @Override
    public boolean hasNext() {
        if(mNextValue == null && super.hasNext()) {
            DBObject nextObj = super.next();
            if(nextObj.getClass().isAssignableFrom(mClassType.getClass())) {
                mNextValue = (T) nextObj;
                return true;
            }
        }
        if(mNextValue != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public T next() {
        T retval = null;
        if(mNextValue != null) {
            retval = mNextValue;
            mNextValue = null;
        } else {
            retval = (T) super.next();
        }
        return retval;
    }
}
