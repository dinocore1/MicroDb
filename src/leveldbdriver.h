#ifndef LEVELDBDRIVER_H_
#define LEVELDBDRIVER_H_

#include <memory>
#include <leveldb/db.h>

namespace microdb {
	
	class LevelDBDriver : public Driver {
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
		
		virtual Status CreateIterator(Iterator& it);
		
		virtual void BeginTransaction();
		virtual void CommitTransaction();
		virtual void RollBackTransaction();
		
	};
	
}

#endif // LEVELDBDRIVER_H_