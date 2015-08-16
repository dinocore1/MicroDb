
#include <cmath>
#include <string>
#include <iostream>

#include "microdb.h"

#include "dbimpl.h"
#include "leveldbdriver.h"


using namespace std;

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
    
    #define META_KEY "meta"
    #define KEY_INSTANCEID "instance"
    #define KEY_INDICIES "indicies"
    #define KEY_ID "id"
    
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
        
        Index& primaryIndex = getPrimaryIndex();
        mIndicies[primaryIndex.getName()] = &primaryIndex;
        
        Value metaObj;
        loadDBObj(*mDBDriver, META_KEY, metaObj);
        
        if(metaObj.IsNull()) {
            mInstanceId = UUID::createRandom();
            metaObj.Set(KEY_INSTANCEID, mInstanceId.getString());
            metaObj.Set(KEY_INDICIES, Value());
        } else {
            mInstanceId.parse(metaObj[KEY_INSTANCEID].asString().c_str());
            
            //indicies
            Value indicies = metaObj[KEY_INDICIES];
            const int numIndicies = indicies.Size();
            for(int i=0;i<numIndicies;i++){
                Value index = indicies[i];
                Index* idxPtr = new Index();
                idxPtr->setDriver(mDBDriver.get());
                idxPtr->fromValue(index);
                mIndicies[idxPtr->getName()] = idxPtr;
            }
        }
        
        return OK;
    }
    
    Status DBImpl::Insert(Value& key, Value& value) {
        
        if(!value.IsObject()) {
            return ERROR;
        }
        
        mDBDriver->BeginTransaction();
        for(auto entry : mIndicies) {
            entry.second->index(value);
        }
        mDBDriver->CommitTransaction();
        
        key = value[KEY_ID];
        
        return OK;
    }

}
