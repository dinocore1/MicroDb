
#ifndef MicroDB_dbimpl_h
#define MicroDB_dbimpl_h

#include <vector>
#include <set>
#include <memory>

#include <microdb/status.h>
#include <microdb/value.h>
#include <microdb/microdb.h>

using namespace std;

namespace microdb {
    
    class DBImpl : public DB {
    private:
        UUID mInstanceId;
        unique_ptr< Driver > mDBDriver;
        set< Index > mIndicies;
        
    public:
        
        DBImpl();
        virtual ~DBImpl();
        
        //CRUD API
        virtual Status Insert(Value& key, Value& value);
        virtual Status Update(Value& key, Value& value);
        virtual Status Delete(const Value& key);

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
