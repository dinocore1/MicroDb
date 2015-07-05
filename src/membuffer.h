
#ifndef MEMBUFFER_H_
#define MEMBUFFER_H_

#include <cstdlib>

namespace microdb {
    
    
    class MemBuffer {
    private:
        uint8_t* mData;
        size_t mSize;
        
    public:
        MemBuffer(size_t initialSize);
        MemBuffer(const char* cstr);
        MemBuffer(const char* buf, size_t size);
        ~MemBuffer();
        
        void ensureSize(const size_t size);
        
        uint8_t& operator[](const size_t n);
        
    };
    
}


#endif /* MEMBUFFER_H_ */