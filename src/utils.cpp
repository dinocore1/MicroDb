
#include "utils.h"

namespace microdb {
	
	MemSlice()
	: mPtr(nullptr), mSize(0) { }
	
	MemSlice(const std::string& str)
	: mPtr(str.data()), mSize(str.size()) { }
	
	MemSlice(const byte* ptr, const size_t size)
	: mPtr(ptr), mSize(size) { }
	
	bool MemSlice::IsValid() {
		return mSize > 0;
	}
	
	operator MemSlice::bool() const {
		return IsValid();
	}
	
} // namespace microdb