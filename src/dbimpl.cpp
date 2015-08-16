
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

    DBImpl::~DBImpl() {}
    
    void loadDBObj(Driver& driver, const CMem& key, Value& dst) {
        MemSlice dataSlice;
        if(driver.Get(key, dataSlice) == OK){
            byte* ptr = dataSlice.get();
            size_t size = dataSlice.size();
            
            MemInputStream in(ptr, size);
            UBJSONReader reader(in);
            reader.read(dst);   
        }   
    }
    
    Status DBImpl::init() {
        
        mPrimaryIndex = &getPrimaryIndex();
        
        Value metaObj;
        loadDBObj(*mDBDriver, META_KEY, metaObj);
        
        if(metaObj.IsNull()) {
            mInstanceId = UUID::createRandom();
            metaObj.Set(KEY_INSTANCEID, mInstanceId.getString());
            metaObj.Set(KEY_INDICIES, Value());
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
    
    void InsertCallback(Driver* driver, Value& indexEntry, Value& value) {
        MemSlice keySlice, valueSlice;
		
		MemOutputStream keyOut;
		UBJSONWriter keyWriter(keyOut);
		keyWriter.write(indexEntry);
		toMemSlice(keyOut, keySlice);
		
		if(!value.IsNull()) {
			MemOutputStream valueOut;
			UBJSONWriter valueWriter(valueOut);
			valueWriter.write(value);
			
			toMemSlice(valueOut, valueSlice);
		}
		driver->Insert(keySlice, valueSlice);
    }
    
    Status DBImpl::Insert(Value& key, Value& value) {
        
        if(!value.IsObject()) {
            return ERROR;
        }
        
        auto cb = std::bind(InsertCallback, mDBDriver.get(), _1, _2);
        
        mDBDriver->BeginTransaction();
        
        mPrimaryIndex->index(value, [&](Value& indexEntry, Value& obj) {
            key = indexEntry;
            InsertCallback(mDBDriver.get(), indexEntry, obj);
        });
        
        for(auto entry : mIndicies) {
            entry.second->index(value, cb);
        }
        mDBDriver->CommitTransaction();
        
        return OK;
    }
    
    void DeleteCallback(Driver* driver, Value& indexEntry) {
        MemSlice keySlice;
        MemOutputStream keyOut;
		UBJSONWriter keyWriter(keyOut);
		keyWriter.write(indexEntry);
		toMemSlice(keyOut, keySlice);
        
        driver->Delete(keySlice);
    }
    
    Status DBImpl::Delete(const Value& key) {
        
        Value value;
        MemSlice keySlice, valueSlice;
        toMemSlice(key, keySlice);
        mDBDriver->Get(keySlice, valueSlice);
        
        auto cb = std::bind(DeleteCallback, mDBDriver.get(), _1);
        
        mDBDriver->BeginTransaction();
        for(auto entry : mIndicies) {
            entry.second->index(value, cb);
        }
        
        mPrimaryIndex->index(value, cb);
        
        mDBDriver->CommitTransaction();
        
    }

}
