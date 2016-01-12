package com.devsmart.microdb;


import com.devsmart.ubjson.UBArray;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class LinkList<T extends DBObject> implements Iterable<T> {

    private final DBObject mDBObj;
    private final Class<? extends T> mClassType;
    private final ArrayList<UUID> mList = new ArrayList<UUID>();

    LinkList(UBValue idArray, DBObject obj, Class<? extends T> classType) {
        mDBObj = obj;
        mClassType = classType;
        if(idArray != null && idArray.isArray()) {
            UBArray strIdArray = idArray.asArray();
            for(int i=0;i<strIdArray.size();i++){
                mList.add(UUID.fromString(strIdArray.get(i).asString()));
            }
        }
    }

    private boolean isValid(T obj) {
        if(obj == null || obj.getDB() != mDBObj.getDB() || obj.getId() == null) {
           return false;
        } else {
            return true;
        }
    }

    public void add(T obj){
        if(!isValid(obj)) {
            throw new RuntimeException("obj is not valid");
        }

        mList.add(obj.getId());
        mDBObj.setDirty();
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).
     *
     * @param index the index of the element to be removed
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public void remove(int index) {
        mList.remove(index);
    }

    /**
     * Removes the first occurrence of the specified element from this list,
     * if it is present.  If the list does not contain the element, it is
     * unchanged.
     * @param obj
     * @return <tt>true</tt> if this list contained the specified element
     */
    public boolean remove(T obj) {
        if(!isValid(obj)) {
            throw new RuntimeException("obj is not valid");
        }

        return mList.remove(obj.getId());
    }

    public int size() {
        return mList.size();
    }

    public boolean isEmpty() {
        return mList.isEmpty();
    }

    public void clear() {
        mList.clear();
        mDBObj.setDirty();
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param  index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public T get(int index) {
        UUID id = mList.get(index);
        return mDBObj.getDB().get(id, mClassType);
    }

    UBArray getIdArray() {
        return UBValueFactory.createArray(Iterables.transform(mList, UUIDtoUBValue));
    }

    private final Function<UUID, UBValue> UUIDtoUBValue = new Function<UUID, UBValue>() {
        @Override
        public UBValue apply(UUID input) {
            return UBValueFactory.createString(input.toString());
        }
    };

    @Override
    public Iterator<T> iterator() {
        return new LinkIterator(mList.iterator());
    }

    private class LinkIterator implements Iterator<T> {

        private final Iterator<UUID> mIt;

        public LinkIterator(Iterator<UUID> iterator) {
            mIt = iterator;
        }

        @Override
        public boolean hasNext() {
            return mIt.hasNext();
        }

        @Override
        public T next() {
            UUID id = mIt.next();
            return mDBObj.getDB().get(id, mClassType);
        }
    }
}
