#ifndef UTILS_H
#define UTILS_H

#include <string>

namespace microdb {
	
	typedef uint8_t byte;
	
	class MemSlice {
		protected:
		byte* mPtr;
		size_t mSize;
		
		public:
		
		MemSlice();
		MemSlice(const std::string&);
		MemSlice(const byte*, const size_t);
		
		virtual ~MemSlice() {};
		
		virtual const byte* get();
		virtual const size_t size();
		
		
		virtual bool IsValid();
		operator bool() const;
		
	};
	
	class MemBuffer : public MemSlice {
		
		public:
		
		MemBuffer();
		MemBuffer(MemBuffer&);
		MemBuffer(MemSlice&&);
		MemBuffer(const std::string&);
		MemBuffer(const byte*, const size_t);
		
		virtual ~MemBuffer();
		
		virtual const byte* get();
		virtual const size_t size();
		
		
		virtual bool IsValid();
	};
}

#endif // UTILS_H