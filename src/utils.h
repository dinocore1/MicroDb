#ifndef UTILS_H_
#define UTILS_H_

#include <string>

namespace microdb {
	
	typedef uint8_t byte;
	
	class slice_t {
		public:
		virtual ~slice_t() {};
		
		virtual byte* get() const = 0;
		virtual size_t size() const = 0;
		
		bool IsValid() { return size() > 0; };
	};
	
	class CMem : public slice_t {
		private:
		bool mIsOwner;
		byte* mPtr;
		size_t mSize;
		
		void destruct();
		
		public:
		CMem();
		CMem(const CMem&);
		CMem(CMem&&);
		~CMem();
		
		static CMem copy(void* ptr, const size_t size);
		
		CMem(const char* cstr);
		CMem(void* ptr, const size_t size, bool isOwner);
		
		byte* get() const;
		size_t size() const;
	};
	
	class STDStrSlice : public slice_t {
		private:
		std::string mString;
		
		public:
		STDStrSlice();
		STDStrSlice(std::string&&);
		
		~STDStrSlice();
		
		byte* get() const;
		size_t size() const;
	};
	
	class MemSlice : public slice_t {
		private:
		uint8_t mType;
		const slice_t* getData() const;
		void destroy();
		
		union container_t {
			CMem CMemObj;
			STDStrSlice STDStrSliceObj;
			
			container_t() {}
			~container_t() {}
		};
		
		container_t mData;
		
		public:
		
		MemSlice();
		virtual ~MemSlice();
		MemSlice(CMem&&);
		MemSlice(STDStrSlice&&);
		
		//MemSlice& operator= (const MemSlice&);
		//void CopyFrom(const MemSlice&);
		
		MemSlice& operator= (MemSlice&&);
		void MoveFrom(MemSlice&&);
		
		
		byte* get() const;
		size_t size() const;
		
	};
	
}

#endif // UTILS_H_