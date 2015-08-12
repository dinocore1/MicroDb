#ifndef UTILS_H
#define UTILS_H

#include <string>

namespace microdb {
	
	typedef uint8_t byte;
	
	class MemSlice {
		public:
		const byte* mPtr;
		const size_t mSize;
		
		MemSlice(const std::string&);
		MemSlice(const byte*, const size_t);
		
	};
}

#endif // UTILS_H