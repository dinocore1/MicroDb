#ifndef DRIVER_H_
#define DRIVER_H_

namespace microdb {
	
	typedef int(*compareFun)(const MemSlice& a, const MemSlice& b);
	
	class Driver {
		public:
		
		class Iterator {
			public:
			virtual ~Iterator() {}
			
			virtual Status GetKey(MemSlice& key) const = 0;
			virtual Status GetValue(MemSlice& value) const = 0;
			
			/**
			* place the iterator on the first occurance of key, 
			* or if key does not exist, the key location right
			* before where key would be inserted.
			*/
			virtual Status Seek(const MemSlice& key) = 0;
			virtual bool IsValid() const = 0;
			virtual Status Next() = 0;
			virtual Status Prev() = 0;
		};
		
		virtual ~Driver() {}
		virtual void SetCompareFunction(compareFun) = 0;
		
		virtual Status Insert(const MemSlice& key, const MemSlice& value) = 0;
		virtual Status Get(const MemSlice& key, MemSlice& value) = 0;
		virtual Status Delete(const MemSlice& key) = 0;
		
		virtual Status CreateIterator(Iterator& it) = 0;
		
		virtual void BeginTransactions() = 0;
		virtual void CommitTransaction() = 0;
		virtual void RollBackTransaction() = 0;
		
	};
} // namespace microdb

#endif // DRIVER_H_