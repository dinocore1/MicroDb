
#include "uuid.h"


namespace microdb {
    
    
    UUID UUID::createRandom() {
        
    }
    
    std::string UUID::getString() {
        char buf[36];
        sprintf(buf, "%02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x%02x%02x",
                mData[0], mData[1], mData[2], mData[3],
                mData[4], mData[5],
                mData[6], mData[7],
                mData[8], mData[9],
                mData[10], mData[11], mData[12], mData[13], mData[14], mData[15]
                );
        return std::string(buf);
    }
    
}