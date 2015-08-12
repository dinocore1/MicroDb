#ifndef DRIVER_H_
#define DRIVER_H_

namespace microdb {
	
	class Driver {
		public:
		
		Status Insert(const MemSlice& key, const MemSlice& value) = 0;
		Status Get(const MemSlice* key) = 0;
		Status Delete(const MemSlice& key) = 0;
		
	};
}

#endif // DRIVER_H_