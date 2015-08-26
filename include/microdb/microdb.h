
#ifndef MICRODB_DB_H_
#define MICRODB_DB_H_

#include <stdio.h>

namespace microdb {

    static const int kMajorVersion = 0;
    static const int kMinorVersion = 1;


    class Iterator {
    public:
      
      virtual ~Iterator() {};

      virtual void SeekToFirst() = 0;
      virtual void SeekTo(const Value& key) = 0;
      
      virtual bool Valid() = 0;
      virtual void Next() = 0;
      virtual void Prev() = 0;

      virtual Value GetKey() = 0;
      virtual Value GetPrimaryKey() = 0;
      virtual Value GetValue() = 0;
    };

    class DB {
    public:
        static Status Open(const std::string& dburl, DB** dbptr);

        virtual ~DB();
        
        //CRUD API
        virtual Status Get(const Value& key, Value& value) = 0;
        virtual Status Insert(Value& key, Value& value) = 0;
        virtual Status Delete(const Value& key) = 0;
        virtual void BeginTransaction() = 0;
		virtual void CommitTransaction() = 0;
		virtual void RollBackTransaction() = 0;

        //Query API
        virtual Iterator* QueryIndex(const std::string& index, const std::string& query) = 0;
        virtual Status AddIndex(const std::string& indexName, const std::string& query) = 0;
        virtual Status DeleteIndex(const std::string& indexName) = 0;

        //Syncing API        
        //virtual Value GetHead() = 0;
        //virtual Status GetChangesSince(const Value& checkpoint, const std::string& query, Iterator& it) = 0;
        //virtual Status ApplyChanges(Iterator& changes) = 0;

    };
}

#endif
