
#include <stdint.h>
#include <stdlib.h>
#include <assert.h>

#include <cstring>
#include <cstdlib>

#include "membuffer.h"

namespace microdb {


    MemBuffer::MemBuffer(size_t initialSize) {
        mData = (uint8_t*) malloc(initialSize);
        mSize = initialSize;
    }

    MemBuffer::MemBuffer(const char* buf, size_t size) {
        mData = (uint8_t*) malloc(size);
        mSize = size;
        memcpy(mData, buf, mSize);
    }

    MemBuffer::MemBuffer(const char* cstr) {
        mSize = strlen(cstr);
        mData = (uint8_t*) malloc(mSize);
        memcpy(mData, cstr, mSize);
    }

    MemBuffer::~MemBuffer() {
        free(mData);
        mData = NULL;
        mSize = 0;
    }

    void MemBuffer::ensureSize(const size_t size) {
        if(size > mSize) {
            mData = (uint8_t*) realloc(mData, size);
            mSize = size;
        }
    }

    uint8_t& MemBuffer::operator[](const size_t n) {
        assert(mSize > n);
        return mData[n];
    }

}
