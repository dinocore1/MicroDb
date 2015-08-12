
#include "utils.h"

namespace microdb {
	
	MemSlice(const std::string& str)
	: mPtr(str.data()), mSize(str.size()) { }
	
	MemSlice(const byte* ptr, const size_t size)
	: mPtr(ptr), mSize(size) { }
	
} // namespace microdb