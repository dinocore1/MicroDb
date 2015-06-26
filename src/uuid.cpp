
#include "uuid.h"

#ifdef GUID_CFUUID
#include <CoreFoundation/CFUUID.h>
#endif

namespace microdb {
    
    UUID::UUID() {
        
    }
    
    UUID::UUID(const char* str) {
        parse(str);
    }
    
    UUID::UUID(const std::string& str) {
        parse(str.c_str());
    }
    
#ifdef GUID_CFUUID
    
    UUID UUID::createRandom() {
        UUID retval;
        auto newId = CFUUIDCreate(NULL);
        auto bytes = CFUUIDGetUUIDBytes(newId);
        retval.mData[0] = bytes.byte0;
        retval.mData[1] = bytes.byte1;
        retval.mData[2] = bytes.byte2;
        retval.mData[3] = bytes.byte3;
        retval.mData[4] = bytes.byte4;
        retval.mData[5] = bytes.byte5;
        retval.mData[6] = bytes.byte6;
        retval.mData[7] = bytes.byte7;
        retval.mData[8] = bytes.byte8;
        retval.mData[9] = bytes.byte9;
        retval.mData[10] = bytes.byte10;
        retval.mData[11] = bytes.byte11;
        retval.mData[12] = bytes.byte12;
        retval.mData[13] = bytes.byte13;
        retval.mData[14] = bytes.byte14;
        retval.mData[15] = bytes.byte15;
        CFRelease(newId);
        
        return retval;
    }
    
#endif
    
    std::string UUID::getString() const {
        char buf[37];
        sprintf(buf, "%02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x%02x%02x",
                mData[0], mData[1], mData[2], mData[3],
                mData[4], mData[5],
                mData[6], mData[7],
                mData[8], mData[9],
                mData[10], mData[11], mData[12], mData[13], mData[14], mData[15]
                );
        return std::string(buf);
    }
    
    
    bool UUID::operator==(const UUID& other) const
    {
        return memcmp(mData, other.mData, 16) == 0;
    }
    
    bool UUID::operator!=(const UUID& other) const
    {
        return memcmp(mData, other.mData, 16) != 0;
    }
    
    
    
}