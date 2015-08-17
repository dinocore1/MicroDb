#include "microdb.h"
#include "leveldbdriver.h"

#include <leveldb/write_batch.h>
#include <leveldb/comparator.h>

using namespace std;

namespace microdb {
	
	LevelDBDriver::Iterator::Iterator(leveldb::Iterator* it)
	: mIt(it) {}
	
	Status LevelDBDriver::Iterator::GetKey(MemSlice& key) const {
		leveldb::Slice keySlice = mIt->key();
		key = CMem((void*)keySlice.data(), keySlice.size(), false);
		return OK;
	}
	
	Status LevelDBDriver::Iterator::GetValue(MemSlice& value) const {
		leveldb::Slice valueSlice = mIt->value();
		value = CMem((void*)valueSlice.data(), valueSlice.size(), false);
		return OK;
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
	
	class LevelDBComparator : public leveldb::Comparator {
		public:
		int Compare(const leveldb::Slice& a, const leveldb::Slice& b) const {
			Value av = MemSliceToValue( CMem((void*)a.data(), a.size(), false) );
			Value bv = MemSliceToValue( CMem((void*)b.data(), b.size(), false) );
			
			return compareValue(av, bv);
		}
		
		// Ignore the following methods for now:
    	const char* Name() const { return "MicroDBComparator"; }
    	void FindShortestSeparator(std::string*, const leveldb::Slice&) const { }
    	void FindShortSuccessor(std::string*) const { }	
	};
	
	LevelDBComparator COMPARATOR;
	LevelDBComparator* getComparator() {
		return &COMPARATOR;
	}
	
	Status LevelDBDriver::open(const std::string& dbpath) {
		leveldb::Options options;
  		options.create_if_missing = true;
		options.comparator = getComparator();
  		leveldb::Status status = leveldb::DB::Open(options, dbpath.c_str(), &mDB);
		return status.ok() ? OK : ERROR;
	}
	
	void LevelDBDriver::SetCompareFunction(compareFun) {}
		
	Status LevelDBDriver::Insert(const MemSlice& key, const MemSlice& value) {
		leveldb::Slice lkey((const char*)key.get(), key.size());
		leveldb::Slice lvalue((const char*)value.get(), value.size());
		
		if(mTRStack.size() > 0) {
			mTRStack.top()->Put(lkey, lvalue);
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
		if(mTRStack.size() > 0) {
			mTRStack.top()->Delete(lkey);
			return OK;
		} else {
			leveldb::Status s;
			s = mDB->Delete(leveldb::WriteOptions(), lkey);
			return s.ok() ? OK : ERROR;	
		}
	}
	
	Driver::Iterator* LevelDBDriver::CreateIterator() {
		return new LevelDBDriver::Iterator(mDB->NewIterator(leveldb::ReadOptions()));
	}
	
	void LevelDBDriver::BeginTransaction() {
		mTRStack.emplace( new leveldb::WriteBatch() );
	}
	
	void LevelDBDriver::CommitTransaction() {
		if(mTRStack.size() > 0) {
			auto& ptr = mTRStack.top();
			mDB->Write(leveldb::WriteOptions(), ptr.get());
			mTRStack.pop();
		}
	}
	
	void LevelDBDriver::RollBackTransaction() {
		if(mTRStack.size() > 0) {
			auto& ptr = mTRStack.top();
			ptr->Clear();
			mTRStack.pop();
		}
	}
	
} //namespace microdb