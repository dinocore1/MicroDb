
#ifndef MicroDB_dbimpl_h
#define MicroDB_dbimpl_h

#include <leveldb/db.h>

#include <microdb/status.h>
#include <microdb/microdb.h>

namespace microdb {

    class DBImpl : public DB {
        
    public:
        leveldb::DB* mLevelDB;
        
        virtual ~DBImpl();
        
        Status Put(const std::string& key, const std::string& value);
        
    };
    
}

#endif
