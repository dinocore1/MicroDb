
#ifndef MicroDB_uuid_h
#define MicroDB_uuid_h

#include <string>

namespace microdb {
    class UUID {
        
    private:
        uint8_t mData[16];
        
        
    public:
        static UUID createRandom();
        
        UUID();
        UUID(const char* str);
        UUID(const std::string& str);
        
        bool parse(const char* str);
        
        std::string getString() const;
        

        bool operator==(const UUID &other) const;
        bool operator!=(const UUID &other) const;
    };
}

#endif
