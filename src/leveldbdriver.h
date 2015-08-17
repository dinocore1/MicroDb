#ifndef LEVELDBDRIVER_H_
#define LEVELDBDRIVER_H_

#include <memory>
#include <leveldb/db.h>

namespace microdb {
	
	class LevelDBDriver : public Driver {
		public:
		class Iterator : public Driver::Iterator {
			private:
			std::unique_ptr<leveldb::Iterator> mIt;
			
			public:
			Iterator(leveldb::Iterator* it);
			virtual ~Iterator() {};
			
			virtual Status GetKey(MemSlice& key) const;
			virtual Status GetValue(MemSlice& value) const;
			
			/**
			* place the iterator on the first occurance of key, 
			* or if key does not exist, the key location right
			* before where key would be inserted.
			*/
			virtual Status Seek(const MemSlice& key);
			virtual bool IsValid() const;
			virtual Status Next();
			virtual Status Prev();
		};
		
		private:
		leveldb::DB* mDB;
		std::unique_ptr< leveldb::WriteBatch > mWriteBatch;
		
		public:
		LevelDBDriver();
		Status open(const std::string& dbfile);
		virtual ~LevelDBDriver();
		
		virtual void SetCompareFunction(compareFun);
		
		virtual Status Insert(const MemSlice& key, const MemSlice& value);
		virtual Status Get(const MemSlice& key, MemSlice& value);
		virtual Status Delete(const MemSlice& key);
		
		virtual Iterator* CreateIterator();
		
		virtual void BeginTransaction();
		virtual void CommitTransaction();
		virtual void RollBackTransaction();
		
	};
	
}

#endif // LEVELDBDRIVER_H_