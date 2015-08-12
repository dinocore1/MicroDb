
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
        
    public:
        
        DBImpl();
        virtual ~DBImpl();
        
        //CRUD API
        virtual Status Insert(const Value& value, std::string& key);
        virtual Status Update(const std::string& key, const Value& value);
        virtual Status Delete(const std::string& key);

        //Query API
        virtual Status Query(const std::string& query, Iterator& it);
        virtual Status AddIndex(const std::string& query);
        virtual Status DeleteIndex(const std::string& query);

        //Syncing API        
        //virtual Value GetHead();
        //virtual Status GetChangesSince(const Value& checkpoint, const std::string& query, Iterator& it);
        //virtual Status ApplyChanges(Iterator& changes);
        
    };
    
}

#endif
