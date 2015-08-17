
#ifndef MicroDB_dbimpl_h
#define MicroDB_dbimpl_h

#include <vector>
#include <set>
#include <memory>

#include <microdb/status.h>
#include <microdb/value.h>
#include <microdb/microdb.h>

namespace microdb {
    
    class IteratorImpl : public Iterator {
      private:
      
        
      public:
      ViewQuery mQuery;
      
      IteratorImpl();
      virtual ~IteratorImpl();

      virtual void SeekToFirst();
      virtual void SeekToLast();

      virtual bool Valid() const;
      virtual void Next();
      virtual void Prev();

      virtual std::string& GetKey() const;
      virtual Value& GetValue() const;  
    };
    
    class DBImpl : public DB {
    private:
        UUID mInstanceId;
        std::unique_ptr< Driver > mDBDriver;
        Index* mPrimaryIndex;
        std::map<std::string, Index* > mIndicies;
        
    public:
        
        DBImpl(std::unique_ptr<Driver> ptr);
        virtual ~DBImpl();
        
        Status init();
        
        //CRUD API
        virtual Status Insert(Value& key, Value& value);
        virtual Status Update(Value& key, Value& value);
        virtual Status Delete(const Value& key);

        //Query API
        virtual Iterator* Query(const std::string& query);
        virtual Status AddIndex(const std::string& query);
        virtual Status DeleteIndex(const std::string& query);

        //Syncing API        
        //virtual Value GetHead();
        //virtual Status GetChangesSince(const Value& checkpoint, const std::string& query, Iterator& it);
        //virtual Status ApplyChanges(Iterator& changes);
        
    };
    
}

#endif
