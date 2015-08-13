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
			char* pzName;
			unqlite_config(mDB, UNQLITE_CONFIG_GET_KV_NAME, &pzName);
			printf("name: %s\n", pzName);
			return OK;
		}
	}
	
	void UnQliteDriver::SetCompareFunction(compareFun) {}
		
	Status UnQliteDriver::Insert(const MemSlice& key, const MemSlice& value) {
		int rc = unqlite_kv_store(mDB, 
			key.get(), key.size(), 
			value.get(), value.size());
		return rc == UNQLITE_OK ? OK : ERROR;
	}
	
	Status UnQliteDriver::Get(const MemSlice* key) {}
	Status UnQliteDriver::Delete(const MemSlice& key) {}
		
	Status UnQliteDriver::CreateIterator(Iterator& it) {}
	
	Status UnQliteDriver::BeginTransactions() {}
	Status UnQliteDriver::CommitTransaction() {}
	Status UnQliteDriver::RollBackTransaction() {}
	
} //namespace microdb