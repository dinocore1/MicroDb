
#ifndef MICRODB_DB_H_
#define MICRODB_DB_H_

#include <stdio.h>

namespace microdb {

    static const int kMajorVersion = 0;
    static const int kMinorVersion = 1;


    class Iterator {
    public:
      Iterator();
      virtual ~Iterator();

      virtual void SeekToFirst() = 0;
      virtual void SeekToLast() = 0;

      virtual bool Valid() const = 0;
      virtual void Next() = 0;
      virtual void Prev() = 0;

      virtual std::string& GetKey() const = 0;
      virtual Value& GetValue() const = 0;
    };

    class DB {
    public:
        static Status Open(const std::string& dburl, DB** dbptr);

        virtual ~DB();
        
        //CRUD API
        virtual Status Insert(Value& key, Value& value) = 0;
        virtual Status Update(Value& key, Value& value) = 0;
        virtual Status Delete(const Value& key) = 0;

        //Query API
        virtual Iterator* QueryIndex(const std::string& index, const std::string& query) = 0;
        virtual Status AddIndex(const std::string& query) = 0;
        virtual Status DeleteIndex(const std::string& query) = 0;

        //Syncing API        
        //virtual Value GetHead() = 0;
        //virtual Status GetChangesSince(const Value& checkpoint, const std::string& query, Iterator& it) = 0;
        //virtual Status ApplyChanges(Iterator& changes) = 0;

    };
}

#endif
