
#include <cmath>
#include <string>
#include <iostream>

#include "microdb.h"

#include "dbimpl.h"
#include "leveldbdriver.h"


using namespace std;
using namespace std::placeholders;

namespace microdb {
    
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
    
    void loadDBObj(Driver* driver, const CMem& key, Value& dst) {
        MemSlice dataSlice;
        if(driver->Get(key, dataSlice) == OK){
            dst = MemSliceToValue(dataSlice);   
        }   
    }
    
    void saveDBObj(Driver* driver, const Value& key, const Value& value) {
        
        MemSlice keySlice, valueSlice;
        MemOutputStream keyOut, valueOut;
        keySlice = ValueToMemSlice(key, keyOut);
        valueSlice = ValueToMemSlice(value, valueOut);
        
        driver->Insert(keySlice, valueSlice);
    }
    
    void deleteDBObj(Driver* driver, const Value& key) {
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
                Index* idxPtr = new Index();
                idxPtr->fromValue(index);
                mIndicies[idxPtr->getName()] = idxPtr;
            }
        }
        
        return OK;
    }
    
    Status DBImpl::Insert(Value& returnKey, Value& value) {
        
        if(!value.IsObject()) {
            return ERROR;
        }
        
        mDBDriver->BeginTransaction();
        
        mPrimaryIndex->index(value, [&](Value key, Value obj, Value indexEntry) {
            returnKey = key;
            saveDBObj(mDBDriver.get(), indexEntry, obj);
        });
        
        auto cb = std::bind(saveDBObj, mDBDriver.get(), _3, _2);
        for(auto entry : mIndicies) {
            entry.second->index(value, cb);
        }
        mDBDriver->CommitTransaction();
        
        return OK;
    }
    
    Status DBImpl::Delete(const Value& key) {
        MemOutputStream out;
        MemSlice valueSlice;
        MemSlice keySlice = ValueToMemSlice(Index::createPrimaryIndexEntry(key), out);
        mDBDriver->Get(keySlice, valueSlice);
        Value value = MemSliceToValue(valueSlice);
        
        auto cb = std::bind(deleteDBObj, mDBDriver.get(), _3);
        
        mDBDriver->BeginTransaction();
        
        for(auto entry : mIndicies) {
            entry.second->index(value, cb);
        }
        
        mPrimaryIndex->index(value, cb);
        
        mDBDriver->CommitTransaction();
        
        return OK;
    }
    
    Status DBImpl::Update(Value& key, Value& value) {
        return ERROR;
    }
    
    Status DBImpl::Query(const std::string& query, Iterator& it) {
        return ERROR;
    }
    
    Status DBImpl::AddIndex(const std::string& query) {
        
    }
    
    Status DBImpl::DeleteIndex(const std::string& query) {
        
    }

}
