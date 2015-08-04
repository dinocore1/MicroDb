
#include <string.h>

#include "uuid.h"


namespace microdb {

    UUID::UUID() {

    }

    UUID::UUID(const char* str) {
        parse(str);
    }

    UUID::UUID(const std::string& str) {
        parse(str.c_str());
    }

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
