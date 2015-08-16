#include <cstdlib>
#include <cstring>

#include "microdb.h"
#include "utils.h"

namespace microdb {
	
	CMem copy(void* ptr, const size_t size) {
		void* cpy = malloc(size);
		memcpy(cpy, ptr, size);
		return CMem(cpy, size, true);
	}
	
	void CMem::destruct() {
		if(mIsOwner) {
			free(mPtr);
		}
		mPtr = nullptr;
		mSize = 0;
		mIsOwner = false;
	}
	
	CMem::CMem()
	: mIsOwner(false), mPtr(nullptr), mSize(0) {}
	
	CMem::CMem(const CMem& o)
	: mIsOwner(false), mPtr(o.mPtr), mSize(o.mSize) { }
	
	CMem::CMem(CMem&& o) {
		mPtr = o.mPtr;
		mSize = o.mSize;
		
		mIsOwner = o.mIsOwner;
		if(o.mIsOwner) {	
			o.mIsOwner = false;
		}
	}
	
	CMem::CMem(const char* cstr)
	: CMem((void*)cstr, strlen(cstr), false) {}
	
	CMem::CMem(void* ptr, const size_t size, bool isOwner)
	: mIsOwner(isOwner), mPtr((byte*)ptr), mSize(size) {}
	
	CMem::~CMem() {
		destruct();
	}
	
	byte* CMem::get() const {
		return mPtr;
	}
	
	size_t CMem::size() const {
		return mSize;
	}
	
	STDStrSlice::STDStrSlice() {}
	
	STDStrSlice::~STDStrSlice() {}
	
	STDStrSlice::STDStrSlice(std::string&& o) {
		mString = std::move(o);
	}
	
	byte* STDStrSlice::get() const {
		return (byte*)mString.data();
	}
	
	size_t STDStrSlice::size() const {
		return mString.size();
	}
	
	
	#define TYPE_NULL 0
	#define TYPE_CMEM 1
	#define TYPE_STDSTR 2
	
	MemSlice::MemSlice()
	: mType(TYPE_NULL) {
		
	}
	
	MemSlice::MemSlice(const CMem& o)
	: mType(TYPE_CMEM) {
		new ( &(mData.CMemObj)) CMem(o);
	}
	
	MemSlice::MemSlice(CMem&& o)
	: mType(TYPE_CMEM) {
		new ( &(mData.CMemObj)) CMem( std::move(o) );
	}
	
	MemSlice::MemSlice(STDStrSlice&& str)
	: mType(TYPE_STDSTR) {
		
		new ( &(mData.STDStrSliceObj)) STDStrSlice(std::move(str));
	}
	
	void MemSlice::destroy() {
		switch(mType) {
			case TYPE_CMEM:
				mData.CMemObj.~CMem();
				break;
				
			case TYPE_STDSTR:
				mData.STDStrSliceObj.~STDStrSlice();
			
			default:
				break;
		}
		mType = TYPE_NULL;
	}
	
	MemSlice::~MemSlice() {
		destroy();
	}
	
	const slice_t* MemSlice::getData() const {
		switch(mType) {
			case TYPE_NULL:
			return nullptr;
			
			case TYPE_CMEM:
			return &mData.CMemObj;
			
			case TYPE_STDSTR:
			return &mData.STDStrSliceObj;
			
			default:
			return nullptr;
			
		}
	}
	
	//MemSlice& MemSlice::operator= (const MemSlice& o) {
	//	CopyFrom(o);
	//	return *this;	
	//}
	
	//void MemSlice::CopyFrom(const MemSlice& o) {
		
	//}
	
	MemSlice& MemSlice::operator= (MemSlice&& o) {
		MoveFrom( std::move(o) );
		return *this;
	}
	
	void MemSlice::MoveFrom(MemSlice&& o) {
		destroy();
		
		switch(o.mType) {
			case TYPE_CMEM:
				new ( &(mData.CMemObj)) CMem( std::move(o.mData.CMemObj) );
				break;
			
			case TYPE_STDSTR:
				new ( &(mData.STDStrSliceObj)) STDStrSlice( std::move(o.mData.STDStrSliceObj) );
				break;
				
			default:
				break;
			
		}
		mType = o.mType;
	}
	
	byte* MemSlice::get() const {
		return getData()->get();
	}
	
	size_t MemSlice::size() const {
		return getData()->size();
	}
	
} // namespace microdb