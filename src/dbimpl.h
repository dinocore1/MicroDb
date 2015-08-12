
#ifndef MicroDB_dbimpl_h
#define MicroDB_dbimpl_h

#include <vector>
#include <set>
#include <memory>

#include <microdb/status.h>
#include <microdb/value.h>
#include <microdb/microdb.h>

#include "index.h"
#include "uuid.h"

using namespace std;

namespace microdb {
    
    class DBImpl : public DB {
    private:
        UUID mInstanceId;
        set< Index > mIndices;
        
    public:
        
        DBImpl();
        virtual ~DBImpl();
        
        //CRUD API
        virtual Status Insert(const Value& value, std::string& key) = 0;
        virtual Status Update(const std::string& key, const Value& value) = 0;
        virtual Status Delete(const std::string& key) = 0;

        //Query API
        virtual Status Query(const std::string& query, Iterator& it) = 0;
        virtual Status AddIndex(const std::string& query) = 0;
        virtual Status DeleteIndex(const std::string& query) = 0;

        //Syncing API        
        //virtual Value GetHead() = 0;
        //virtual Status GetChangesSince(const Value& checkpoint, const std::string& query, Iterator& it) = 0;
        //virtual Status ApplyChanges(Iterator& changes) = 0;
        
    };
    
}

#endif
