#include "microdb.h"
#include "leveldbdriver.h"

namespace microdb {
	
	LevelDBDriver::LevelDBDriver()
	: mDB(nullptr) { }
	
	LevelDBDriver::~LevelDBDriver() {
		if(mDB != nullptr) {
			delete mDB;
		}
	}
	
	Status LevelDBDriver::open(const std::string& dbpath) {
		leveldb::Options options;
  		options.create_if_missing = true;
  		leveldb::Status status = leveldb::DB::Open(options, dbpath.c_str(), &mDB);
		return status.ok() ? OK : ERROR;
	}
	
	void LevelDBDriver::SetCompareFunction(compareFun) {}
		
	Status LevelDBDriver::Insert(const MemSlice& key, const MemSlice& value) {
		leveldb::Slice lkey((const char*)key.get(), key.size());
		leveldb::Slice lvalue((const char*)value.get(), value.size());
		leveldb::Status s = mDB->Put(leveldb::WriteOptions(), lkey, lvalue);
		return s.ok() ? OK : ERROR;
		
	}
	
	Status LevelDBDriver::Get(const MemSlice* key) {
		
	}
	Status LevelDBDriver::Delete(const MemSlice& key) {
		
	}
	
	Status LevelDBDriver::CreateIterator(Iterator& it) {
		
	}
	
	Status LevelDBDriver::BeginTransactions() {}
	Status LevelDBDriver::CommitTransaction() {}
	Status LevelDBDriver::RollBackTransaction() {}
	
} //namespace microdb