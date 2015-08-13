#ifndef UNQLITEDRIVER_H_
#define UNQLITEDRIVER_H_

extern "C" {
#include <unqlite.h>
}

namespace microdb {
	
	class UnQliteDriver : public Driver {
		private:
		unqlite* mDB;
		
		public:
		UnQliteDriver();
		Status open(const std::string& dbfile);
		virtual ~UnQliteDriver();
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

#endif // UNQLITEDRIVER_H_