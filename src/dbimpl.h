
#ifndef MicroDB_dbimpl_h
#define MicroDB_dbimpl_h

#include <leveldb/db.h>

#include <microdb/status.h>
#include <microdb/microdb.h>

namespace microdb {

    class DBImpl : public DB {
    public:
        const std::string mInstanceId;
        
        leveldb::DB* mLevelDB;
        
        DBImpl(const std::string& id);
        
        virtual ~DBImpl();
        
        Status Put(const std::string& value, std::string* key);
        
    };
    
}

#endif
