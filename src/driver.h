#ifndef DRIVER_H_
#define DRIVER_H_

namespace microdb {
	
	typedef int(*compareFun)(const MemSlice& a, const MemSlice& b);
	
	class Iterator {
		public:
		virtual ~Iterator() {}
		
		
		Status GetKey(MemBuffer& key) = 0;
		Status GetValue(MemBuffer& value) = 0;
		
		Status Seek(const MemSlice& key) = 0;
		Status Next() = 0;
		Status Prev() = 0;
	}
	
	class Driver {
		public:
		
		void SetCompareFunction(compareFun) = 0;
		
		Status Insert(const MemSlice& key, const MemSlice& value) = 0;
		Status Get(const MemSlice* key) = 0;
		Status Delete(const MemSlice& key) = 0;
		
		Status BeginTransactions() = 0;
		Status CommitTransaction() = 0;
		Status RollBackTransaction() = 0;
		
	};
}

#endif // DRIVER_H_