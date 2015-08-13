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
		
		virtual ~MemSlice();
		MemSlice();
		MemSlice(const std::string&);
		MemSlice(const byte*, const size_t);
		
		virtual byte* get() const;
		virtual size_t size() const;
		
		
		virtual bool IsValid() const;
		operator bool() const;
		
	};
	
	class MemBuffer : public MemSlice {
		
		public:
		
		MemBuffer();
		MemBuffer(MemBuffer&);
		MemBuffer(MemSlice&&);
		MemBuffer(const std::string&);
		MemBuffer(const byte*, const size_t);
		
		virtual ~MemBuffer() {};
		
		virtual byte* get() const;
		virtual size_t size() const;
		
		
		virtual bool IsValid() const;
	};
}

#endif // UTILS_H