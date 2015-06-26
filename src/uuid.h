
#ifndef MicroDB_uuid_h
#define MicroDB_uuid_h

#include <string>

namespace microdb {
    class UUID {
        
    private:
        uint8_t mData[16];
        
    public:
        static UUID createRandom();
        
        UUID(const char* str);
        
        std::string getString();
    };
}

#endif
