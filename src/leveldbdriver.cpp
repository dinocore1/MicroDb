#include "microdb.h"
#include "leveldbdriver.h"

#include <leveldb/write_batch.h>

using namespace std;

namespace microdb {
	
	LevelDBDriver::Iterator::Iterator(leveldb::Iterator* it)
	: mIt(it) {}
	
	Status LevelDBDriver::Iterator::GetKey(MemSlice& key) const {
		leveldb::Slice keySlice = mIt->key();
		key = CMem((void*)keySlice.data(), keySlice.size(), false);
	}
	
	Status LevelDBDriver::Iterator::GetValue(MemSlice& value) const {
		leveldb::Slice valueSlice = mIt->value();
		value = CMem((void*)valueSlice.data(), valueSlice.size(), false);
	}
	
	Status LevelDBDriver::Iterator::Seek(const MemSlice& key) {
		mIt->Seek( leveldb::Slice((const char*)key.get(), key.size()) );
		return OK;
	}
	
	bool LevelDBDriver::Iterator::IsValid() const {
		return mIt->Valid();
	}
	
	Status LevelDBDriver::Iterator::Next() {
		mIt->Next();
		return OK;
	}
	
	Status LevelDBDriver::Iterator::Prev() {
		mIt->Prev();
		return OK;
	}
	
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
		
		if(mWriteBatch) {
			mWriteBatch->Put(lkey, lvalue);
			return OK;
			
		} else {
			leveldb::Status s = mDB->Put(leveldb::WriteOptions(), lkey, lvalue);
			return s.ok() ? OK : ERROR;
		}
	}
	
	Status LevelDBDriver::Get(const MemSlice& key, MemSlice& value) {
		leveldb::Slice lkey((const char*)key.get(), key.size());
		
		std::string strVal;
		leveldb::Status s = mDB->Get(leveldb::ReadOptions(), lkey, &strVal);
		if(s.ok()) {
			value = STDStrSlice( std::move(strVal) );
			return OK;
		} else {
			return ERROR;
		}
		
	}
	
	Status LevelDBDriver::Delete(const MemSlice& key) {
		
		leveldb::Slice lkey((const char*)key.get(), key.size());
		if(mWriteBatch) {
			mWriteBatch->Delete(lkey);
			return OK;
		} else {
			leveldb::Status s;
			s = mDB->Delete(leveldb::WriteOptions(), lkey);
			return s.ok() ? OK : ERROR;	
		}
	}
	
	Iterator* LevelDBDriver::CreateIterator() {
		return new LevelDBDriver::Iterator(mDB->NewIterator(leveldb::ReadOptions()));
	}
	
	void LevelDBDriver::BeginTransaction() {
		if(!mWriteBatch) {
			mWriteBatch = std::unique_ptr< leveldb::WriteBatch > (new leveldb::WriteBatch());
		}
	}
	
	void LevelDBDriver::CommitTransaction() {
		if(mWriteBatch) {
			mDB->Write(leveldb::WriteOptions(), mWriteBatch.get());
			mWriteBatch.release();
		}
	}
	
	void LevelDBDriver::RollBackTransaction() {
		if(mWriteBatch) {
			mWriteBatch.release();
		}
	}
	
} //namespace microdb