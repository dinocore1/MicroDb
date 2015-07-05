
#ifndef MicroDB_dbimpl_h
#define MicroDB_dbimpl_h

#include <vector>
#include <memory>

#include <leveldb/db.h>

#include <microdb/status.h>
#include <microdb/microdb.h>

#include "uuid.h"
#include "viewquery.h"
#include "membuffer.h"

#include <rapidjson/rapidjson.h>
#include <rapidjson/document.h>

using namespace std;

namespace microdb {
    

    class IndexDataumBuilder {
    private:
        MemBuffer mData;
        size_t mLocation;
        
    public:
        IndexDataumBuilder();
        
        void addString(const char* cstr);
        void addString(const char* cstr, unsigned int len);
        void addNumber(double value);
        
        leveldb::Slice getSlice();
        
    };
    
    class IndexDataum {
    private:
        const char* mData;
        const unsigned int mLength;
        
    public:
        IndexDataum(const char* data, const unsigned int length);
        
        
        uint8_t getType();
        leveldb::Slice& getString();
        double getNumber();
        
        int compare(IndexDataum& other);
        
        
    };

    class DBImpl : public DB {
    private:
        UUID mInstanceId;
        leveldb::DB* mLevelDB;
        vector< ViewQuery* > mViews;
        
    public:
        
        DBImpl();
        virtual ~DBImpl();
        
        Status init(leveldb::DB* db);
        

        Status Insert(const std::string& value, std::string* key);
        Status Update(const std::string& key, const std::string& value);
        Status Delete(const std::string& key);
        
    };
    
}

#endif
