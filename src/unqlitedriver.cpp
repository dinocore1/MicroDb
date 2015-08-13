#include "microdb.h"
#include "unqlitedriver.h"

namespace microdb {
	
	UnQliteDriver::UnQliteDriver()
	: mDB(nullptr) {
		
	}
	
	UnQliteDriver::~UnQliteDriver() {
		if(mDB != nullptr) {
			unqlite_close(mDB);
		}
	}
	
	Status UnQliteDriver::open(const std::string& dbfile) {
		if(unqlite_open(&mDB, dbfile.c_str(), UNQLITE_OPEN_CREATE) != UNQLITE_OK) {
			return ERROR;
		} else {
			return OK;
		}
	}
	
	void UnQliteDriver::SetCompareFunction(compareFun) {}
		
	Status UnQliteDriver::Insert(const MemSlice& key, const MemSlice& value) {}
	Status UnQliteDriver::Get(const MemSlice* key) {}
	Status UnQliteDriver::Delete(const MemSlice& key) {}
		
	Status UnQliteDriver::CreateIterator(Iterator& it) {}
	
	Status UnQliteDriver::BeginTransactions() {}
	Status UnQliteDriver::CommitTransaction() {}
	Status UnQliteDriver::RollBackTransaction() {}
	
} //namespace microdb