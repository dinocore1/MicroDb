
#include <cmath>
#include <string>
#include <iostream>

#include "microdb.h"

#include "dbimpl.h"
#include "leveldbdriver.h"


using namespace std;
using namespace std::placeholders;

namespace microdb {
    
    IteratorImpl::IteratorImpl(Driver::Iterator* it, const std::string& indexName)
    : mIt(it), mIndexName(indexName) { }
    
    IteratorImpl::~IteratorImpl() {}
    
    bool IteratorImpl::Valid() {
        bool retval = mIt->IsValid();
        if(retval) {
            MemSlice keySlice;
            mIt->GetKey(keySlice);
            Value indexEntry = MemSliceToValue(keySlice);
            
            //ensure we are still in our index
            Value indexName = indexEntry[1];
            retval = indexName == mIndexName;
        }
        return retval;
    }
    
    void IteratorImpl::SeekToFirst() {
        MemOutputStream out;
        Value indexEntry;
        indexEntry.Add('i');
        indexEntry.Add(mIndexName);
        
        MemSlice flat = ValueToMemSlice(indexEntry, out);
        mIt->Seek(flat);
    }
    
    void IteratorImpl::SeekTo(const Value& key) {
        MemOutputStream out;
        Value indexEntry;
        indexEntry.Add('i');
        indexEntry.Add(mIndexName);
        indexEntry.Add(key);
        
        MemSlice flat = ValueToMemSlice(indexEntry, out);
        mIt->Seek(flat);
    }
    
    void IteratorImpl::Next() {
        mIt->Next();
    }
    
    void IteratorImpl::Prev() {
        mIt->Prev();
    }
    
    Value IteratorImpl::GetKey() {
        mIt->GetKey(mKeySlice);
        Value indexEntry = MemSliceToValue(mKeySlice);
        return indexEntry[2];
    }
    
    Value IteratorImpl::GetPrimaryKey() {
        mIt->GetKey(mKeySlice);
        Value indexEntry = MemSliceToValue(mKeySlice);
        return indexEntry[3];
    }
    
    Value IteratorImpl::GetValue() {
        mIt->GetValue(mValueSlice);
        return MemSliceToValue(mValueSlice);
    }
    
    
    Status DB::Open(const std::string& dburl, DB** dbptr) {
         
         unique_ptr< LevelDBDriver > driver(new LevelDBDriver());
         Status retcode = driver->open(dburl);
         if(retcode == OK) {
             DBImpl* db = new DBImpl( std::move(driver) );
             retcode = db->init();
             *dbptr = db;
         }

        return retcode;
    }

    DB::~DB() {}

    DBImpl::DBImpl(unique_ptr<Driver> ptr)
    : mDBDriver(std::move(ptr)) { }

    DBImpl::~DBImpl() {
        
    }
    
    static void loadDBObj(Driver* driver, const Value& key, Value& dst) {
        MemSlice keySlice, dataSlice;
        MemOutputStream keyOut;
        keySlice = ValueToMemSlice(key, keyOut);
        if(driver->Get(keySlice, dataSlice) == OK){
            dst = MemSliceToValue(dataSlice);   
        }
    }
    
    static void saveDBObj(Driver* driver, const Value& key, const Value& value) {
        
        MemSlice keySlice, valueSlice;
        MemOutputStream keyOut, valueOut;
        keySlice = ValueToMemSlice(key, keyOut);
        valueSlice = ValueToMemSlice(value, valueOut);
        
        driver->Insert(keySlice, valueSlice);
    }
    
    static void deleteDBObj(Driver* driver, const Value& key) {
        MemSlice keySlice;
        MemOutputStream keyOut;
        keySlice = ValueToMemSlice(key, keyOut);
        
        driver->Delete(keySlice);
    }
    
    Status DBImpl::init() {
        
        mPrimaryIndex = &Index::getPrimaryIndex();
        
        Value metaObj;
        loadDBObj(mDBDriver.get(), META_KEY, metaObj);
        
        if(metaObj.IsNull()) {
            mInstanceId = UUID::createRandom();
            metaObj.Set(KEY_INSTANCEID, mInstanceId.getString());
            metaObj.Set(KEY_INDICIES, Value());
            
            saveDBObj(mDBDriver.get(), META_KEY, metaObj);
        } else {
            mInstanceId.parse(metaObj[KEY_INSTANCEID].asString().c_str());
            
            //load all secondary indicies
            Value indicies = metaObj[KEY_INDICIES];
            const int numIndicies = indicies.Size();
            for(int i=0;i<numIndicies;i++){
                Value index = indicies[i];
                unique_ptr<Index> idxPtr(new Index());
                idxPtr->fromValue(index);
                mIndicies[idxPtr->getName()] = std::move(idxPtr);
            }
        }
        
        return OK;
    }
    
    Status DBImpl::Get(const Value& key, Value& value) {
        MemOutputStream out;
        MemSlice valueSlice;
        MemSlice keySlice = ValueToMemSlice(Index::createPrimaryIndexEntry(key), out);
        Status retval = mDBDriver->Get(keySlice, valueSlice);
        if(retval == OK) {
            value = MemSliceToValue(valueSlice);
        }
        
        return retval;
    }
    
    Status DBImpl::Insert(Value& returnKey, Value& value) {
        
        if(!value.IsObject()) {
            return ERROR;
        }
        
        //Transaction tr(mDBDriver.get());
        
        mPrimaryIndex->index(value, [&](Value key, Value obj, Value indexEntry) {
            returnKey = key;
            saveDBObj(mDBDriver.get(), indexEntry, obj);
        });
        
        auto cb = std::bind(saveDBObj, mDBDriver.get(), _3, _2);
        for(auto& entry : mIndicies) {
            entry.second->index(value, cb);
        }
        
        //tr.success();
        
        return OK;
    }
    
    Status DBImpl::Delete(const Value& key) {
        MemOutputStream out;
        MemSlice valueSlice;
        MemSlice keySlice = ValueToMemSlice(Index::createPrimaryIndexEntry(key), out);
        if(mDBDriver->Get(keySlice, valueSlice) == OK) {
            Value value = MemSliceToValue(valueSlice);
            
            auto cb = std::bind(deleteDBObj, mDBDriver.get(), _3);
            
            //Transaction tr(mDBDriver.get());
            
            for(auto& entry : mIndicies) {
                entry.second->index(value, cb);
            }
            
            mPrimaryIndex->index(value, cb);
            
            //tr.success();
        }
        
        return OK;
    }
    
    void DBImpl::BeginTransaction(){
        mDBDriver->BeginTransaction();
    }
	
    void DBImpl::CommitTransaction() {
        mDBDriver->CommitTransaction();
    }
    
	void DBImpl::RollBackTransaction(){
        mDBDriver->RollBackTransaction();
    }
    
    Iterator* DBImpl::QueryIndex(const std::string& index, const std::string& query) {
        
        IteratorImpl* retval = new IteratorImpl( mDBDriver->CreateIterator(), index );
        retval->mQuery.compile(query.c_str());
        
        retval->SeekToFirst();
        
        return retval;
        
    }
    
    Status DBImpl::AddIndex(const std::string& indexName, const std::string& querystr) {
        
        unique_ptr<ViewQuery> query(new ViewQuery());
        if(!query->compile(querystr.c_str())){
            return ERROR;
        }
        
        unique_ptr<Index> idx(new Index(indexName));
        idx->setQuery( std::move(query) );
        mIndicies[idx->getName()] = std::move(idx);
        
        return OK;
    }
    
    Status DBImpl::DeleteIndex(const std::string& indexName) {
        
        auto it = mIndicies.find(indexName);
        if(it != mIndicies.end()) {
            it->second->remove(mDBDriver.get());
            mIndicies.erase(it);
        }
        
        return OK;
    }

}
