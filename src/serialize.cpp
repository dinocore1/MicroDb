
#include <microdb/value.h>
#include <microdb/serialize.h>

#include <cstring>
#include <algorithm>

using namespace std;

namespace microdb {
	
	#define DEFAULT_SIZE 1024
	
	MemOutputStream::MemOutputStream()
	: mBuffer(new int8_t[DEFAULT_SIZE]), mBufSize(DEFAULT_SIZE), mWriteIndex(0) {
	}
	
	void MemOutputStream::Write(const void* buf, const size_t len) {
		const size_t available = mBufSize - mWriteIndex;
		if(len > available) {
			//resize
			const size_t newBufSize = mBufSize*2;
			unique_ptr<int8_t[]> newBuf(new int8_t[newBufSize]);
			memcpy(newBuf.get(), mBuffer.get(), mWriteIndex);
			mBuffer = move(newBuf);
			mBufSize = newBufSize;
		}
		
		memcpy(&mBuffer.get()[mWriteIndex], buf, len);
		mWriteIndex += len;
	}
	
	void MemOutputStream::GetData(void*& buf, size_t& size) const {
		buf = mBuffer.get();
		size = mWriteIndex;
	}
	
	MemInputStream::MemInputStream(const byte* buf, const size_t size)
	: mBuffer(buf), mBufSize(size), mReadIndex(0) {
	}
	
	int MemInputStream::Read(byte* buf, const size_t max) {
		if(max == 0) {
			return 0;
		}
		const size_t bytesLeft = mBufSize - mReadIndex;
		const size_t bytesToRead = min(bytesLeft, max);
		if(bytesToRead == 0) {
			return -1;
		}
		memcpy(buf, &mBuffer[mReadIndex], bytesToRead);
		mReadIndex += bytesToRead;
		return bytesToRead;
	}
	
	
} // namespace microdb