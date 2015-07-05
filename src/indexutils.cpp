

#include "dbimpl.h"

#include <cmath>

#include <leveldb/comparator.h>



namespace microdb {

    IndexDataumBuilder::IndexDataumBuilder()
    : mData(512), mLocation(0) { }
    
    IndexDataumBuilder& IndexDataumBuilder::move() {
        return *this;
    }
    
    IndexDataumBuilder& IndexDataumBuilder::addString(const char *cstr, unsigned int len) {
        
        assert(len < MAX_LONG_STRING);
        
        if(len <= MAX_SHORT_STRING) {
            mData.ensureSize(len + 1);
            mData[mLocation] = TYPE_SHORT_STRING | (LENG_MASK & len);
            memcpy(&mData[mLocation+1], cstr, len);
            mLocation += len + 1;
        } else {
            mData.ensureSize(len + 2);
            mData[mLocation] = TYPE_LONG_STRING | (LENG_MASK & (len >> 8));
            mData[mLocation+1] = 0xFF & len;
            memcpy(&mData[mLocation+2], cstr, len);
            mLocation += len + 2;
        }
        return *this;
    }
    
    IndexDataumBuilder& IndexDataumBuilder::addString(const char *cstr) {
        addString(cstr, strlen(cstr));
        return *this;
    }
    
    IndexDataumBuilder& IndexDataumBuilder::addNumber(double value) {
        mData.ensureSize(mLocation + 9);
        mData[mLocation] = TYPE_NUMBER;
        memcpy(&mData[mLocation+1], &value, 8);
        mLocation += 9;
        return *this;
    }
    
    leveldb::Slice IndexDataumBuilder::getSlice() {
        return leveldb::Slice((const char*)&mData[0], mLocation);
    }
    
    /////////////// IndexDatum /////////////////////
    
    IndexDataum::IndexDataum(const void* data, const size_t size) : mData((uint8_t*)data), mSize(size), mLocation(0) { }
    IndexDataum::IndexDataum(const void* data, const size_t size, size_t location) : mData((uint8_t*)data), mSize(size), mLocation(location) {}
    IndexDataum::IndexDataum(const leveldb::Slice& slice)
    : mData((uint8_t*)slice.data()), mSize(slice.size()), mLocation(0) { }
    
    void IndexDataum::reset() {
        mLocation = 0;
    }
    
    bool IndexDataum::hasNext() {
        return mLocation < mSize;
    }
    
    uint8_t IndexDataum::getType() {
        return 0x20 & mData[mLocation];
    }
    
    bool IndexDataum::starts_with(const IndexDataum &value) {
        return memcmp(&mData[mLocation], &value.mData[value.mLocation], value.mSize) == 0;
    }
    
    IndexDataum IndexDataum::getString(leveldb::Slice& retval) {
        assert(TYPE_STRING & mData[mLocation]);
        
        size_t offset;
        const char* buf;
        uint16_t size = 0;
        if(TYPE_LONG & mData[mLocation]) {
            size = ((LENG_MASK & mData[mLocation]) << 8) | mData[mLocation+1];
            buf = (const char*) &mData[mLocation + 2];
            offset = size + 2;
        } else {
            size = (LENG_MASK & mData[mLocation]);
            buf = (const char*) &mData[mLocation + 1];
            offset = size + 1;
        }
        
        retval = leveldb::Slice(buf, size);
        
        return IndexDataum(mData, mSize, mLocation + offset);
    }
    
    IndexDataum IndexDataum::getNumber(double& retval) {
        assert(TYPE_NUMBER & mData[mLocation]);
        
        memcpy(&retval, &mData[mLocation + 1], 8);
        
        return IndexDataum(mData, mSize, mLocation+9);
    }
    
    inline int compareString(IndexDataum& a, IndexDataum& b) {
        leveldb::Slice aSlice, bSlice;
        IndexDataum nextA = a.getString(aSlice);
        IndexDataum nextB = b.getString(bSlice);
        
        int retval = leveldb::BytewiseComparator()->Compare(aSlice, bSlice);
        if(retval == 0) {
            retval = nextA.compare(nextB);
        }
        return retval;
    }
    
    inline int compareNumber(IndexDataum& a, IndexDataum& b) {
        double aNum, bNum;
        IndexDataum nextA = a.getNumber(aNum);
        IndexDataum nextB = b.getNumber(bNum);
        
        int retval;
        if(aNum == bNum || std::abs(aNum-bNum)<std::abs(std::min(aNum,bNum))*std::numeric_limits<double>::epsilon()) {
            retval = nextA.compare(nextB);
        } else if(aNum < bNum) {
            retval = -1;
        } else {
            retval = 1;
        }
        return retval;
    }
    
    inline int compareType(IndexDataum& a, IndexDataum& b) {
        int aType = a.getType();
        int bType = b.getType();
        int retval = aType - bType;
        if(retval == 0) {
            if(aType == TYPE_STRING) {
                retval = compareString(a, b);
            } else {
                retval = compareNumber(a, b);
            }
        }
        return retval;
    }
    
    inline int compareHasNext(IndexDataum& a, IndexDataum& b) {
        int ahasNext = a.hasNext() ? 1 : 0;
        int bhasNext = b.hasNext() ? 1 : 0;
        int retval = ahasNext - bhasNext;
        if(retval == 0 && ahasNext == 1) {
            retval = compareType(a, b);
        }
        return retval;
    }
    
    int IndexDataum::compare(microdb::IndexDataum &other) {
        int retval = compareHasNext(*this, other);
        return retval;
    }
    
}

