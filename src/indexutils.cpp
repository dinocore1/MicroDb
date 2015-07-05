

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
    
}

