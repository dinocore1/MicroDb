
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
      std::unique_ptr<Driver::Iterator> mIt;
      const std::string mIndexName;
      MemSlice mKeySlice, mValueSlice;

      public:
      
      ViewQuery mQuery;
      
      IteratorImpl(Driver::Iterator*, const std::string& indexName);
      virtual ~IteratorImpl();

      virtual void SeekToFirst();
      virtual void SeekTo(const Value& key);

      virtual bool Valid();
      virtual void Next();
      virtual void Prev();

      virtual Value GetKey();
      virtual Value GetPrimaryKey();
      virtual Value GetValue();  
    };
    
    class DBImpl : public DB {
    private:
        UUID mInstanceId;
        std::unique_ptr< Driver > mDBDriver;
        Index* mPrimaryIndex;
        std::map<std::string, std::unique_ptr<Index> > mIndicies;
        
    public:
        
        DBImpl(std::unique_ptr<Driver> ptr);
        virtual ~DBImpl();
        
        Status init();
        
        //CRUD API
        virtual Status Insert(Value& key, Value& value);
        virtual Status Update(Value& key, Value& value);
        virtual Status Delete(const Value& key);
        virtual void BeginTransaction();
		virtual void CommitTransaction();
		virtual void RollBackTransaction();

        //Query API
        virtual Iterator* QueryIndex(const std::string& index, const std::string& query);
        virtual Status AddIndex(const std::string& indexName, const std::string& query);
        virtual Status DeleteIndex(const std::string& indexName);

        //Syncing API        
        //virtual Value GetHead();
        //virtual Status GetChangesSince(const Value& checkpoint, const std::string& query, Iterator& it);
        //virtual Status ApplyChanges(Iterator& changes);
        
    };
    
}

#endif
