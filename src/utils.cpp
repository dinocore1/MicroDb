
#include "utils.h"

namespace microdb {
	
	MemSlice::~MemSlice() {
		
	}
	
	MemSlice::MemSlice()
	: mPtr(nullptr), mSize(0) { }
	
	MemSlice::MemSlice(const std::string& str)
	: mPtr((byte*)str.data()), mSize(str.size()) { }
	
	MemSlice::MemSlice(const byte* ptr, const size_t size)
	: mPtr((byte*)ptr), mSize(size) { }
	
	byte* MemSlice::get() const {
		return mPtr;
	}
	
	size_t MemSlice::size() const {
		return mSize;
	}
	
	bool MemSlice::IsValid() const {
		return mSize > 0;
	}
	
	MemSlice::operator bool() const {
		return IsValid();
	}
	
} // namespace microdb