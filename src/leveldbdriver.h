#ifndef LEVELDBDRIVER_H_
#define LEVELDBDRIVER_H_

#include <leveldb/db.h>

namespace microdb {
	
	class LevelDBDriver : public Driver {
		private:
		leveldb::DB* mDB;
		
		public:
		LevelDBDriver();
		Status open(const std::string& dbfile);
		virtual ~LevelDBDriver();
		
		virtual void SetCompareFunction(compareFun);
		
		virtual Status Insert(const MemSlice& key, const MemSlice& value);
		virtual Status Get(const MemSlice* key);
		virtual Status Delete(const MemSlice& key);
		
		virtual Status CreateIterator(Iterator& it);
		
		virtual Status BeginTransactions();
		virtual Status CommitTransaction();
		virtual Status RollBackTransaction();
		
	};
	
}

#endif // LEVELDBDRIVER_H_