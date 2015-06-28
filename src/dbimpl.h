
#ifndef MicroDB_dbimpl_h
#define MicroDB_dbimpl_h

#include <vector>
#include <memory>

#include <leveldb/db.h>

#include <microdb/status.h>
#include <microdb/microdb.h>

#include "uuid.h"
#include "viewquery.h"

#include <rapidjson/rapidjson.h>
#include <rapidjson/document.h>

using namespace std;

namespace microdb {

    class DBImpl : public DB {
    private:
        UUID mInstanceId;
        leveldb::DB* mLevelDB;
        vector< unique_ptr<ViewQuery> > mViews;
        
    public:
        
        DBImpl();
        virtual ~DBImpl();
        
        Status init(leveldb::DB* db);
        

        
        Status Put(const std::string& value, std::string* key);
        
    };
    
}

#endif
