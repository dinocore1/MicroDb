

#include "dbimpl.h"

#define TYPE_MASK 0xE0
#define LENG_MASK 0x1F

#define TYPE_NUMBER 0x09
#define TYPE_SHORT_STRING 0x20
#define TYPE_LONG_STRING 0xA0

#define MAX_SHORT_STRING 0x1F
#define MAX_LONG_STRING 0x1FFF

namespace microdb {

    IndexDataumBuilder::IndexDataumBuilder()
    : mData(512), mLocation(0) { }
    
    void IndexDataumBuilder::addString(const char *cstr, unsigned int len) {
        
        assert(len < MAX_LONG_STRING);
        
        if(len <= MAX_SHORT_STRING) {
            mData.ensureSize(len + 1);
            mData[mLocation] = TYPE_SHORT_STRING | (LENG_MASK & len);
            memcpy(&mData[mLocation+1], cstr, len);
            mLocation += len + 1;
        } else {
            mData.ensureSize(len + 2);
            mData[mLocation] = TYPE_LONG_STRING | (LENG_MASK & (len < 8));
            mData[mLocation+1] = 0xFF & len;
            memcpy(&mData[mLocation+2], cstr, len);
            mLocation += len + 2;
        }
    }
    
    void IndexDataumBuilder::addString(const char *cstr) {
        addString(cstr, strlen(cstr));
    }
    
    void IndexDataumBuilder::addNumber(double value) {
        mData.ensureSize(mLocation + 9);
        mData[mLocation] = TYPE_NUMBER;
        memcpy(&mData[mLocation+1], &value, 8);
        mLocation += 9;
    }
    
    leveldb::Slice IndexDataumBuilder::getSlice() {
        return leveldb::Slice((const char*)&mData[0], mLocation);
    }
    
    /////////////// IndexDatum /////////////////////
    
    IndexDataum::IndexDataum(const char* data, const size_t size) : mData(data), mSize(size), mLocation(0) { }
    
    void IndexDataum::reset() {
        mLocation(0);
    }
    
    bool IndexDataum::hasNext() {
        
    }
    
    void IndexDataum::next() {
        
    }
    
    uint8_t IndexDataum::getType() {
        return 0x20 & mData[mLocation];
    }
    
    leveldb::Slice IndexDataum::getString() {
        
    }
    
    double IndexDataum::getNumber() {
        
    }
    
    inline const char* IndexDataum::getString() {
        return &mData[1];
    }
    
    inline double IndexDataum::getNumber() {
        return *(double*)&mData[1];
    }
    
    int IndexDataum::compare(microdb::IndexDataum &other) {
        
        int retval = getType() - other.getType();
        if(retval == 0) {
            switch (retval) {
                case STRING_TYPE:
                    retval = strcmp(getString(), other.getString());
                    break;
                    
                case NUMBER_TYPE:
                    double a = getNumber();
                    double b = other.getNumber();
                    
                    if(a == b ||
                       std::abs(a-b)<std::abs(std::min(a,b))*std::numeric_limits<double>::epsilon()) {
                        retval = 0;
                    } else if (a < b) {
                        retval = -1;
                    } else {
                        retval = 1;
                    }
                    break;
            }
            
        }
        
        return retval;
    }
    
}

