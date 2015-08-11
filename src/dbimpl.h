
#ifndef MicroDB_dbimpl_h
#define MicroDB_dbimpl_h

#include <vector>
#include <set>
#include <memory>

#include <leveldb/db.h>

#include <microdb/status.h>
#include <microdb/microdb.h>

#include "uuid.h"
#include "viewquery.h"
#include "membuffer.h"


#define TYPE_MASK 0xE0
#define LENG_MASK 0x1F

#define TYPE_LONG 0x80

#define TYPE_NUMBER 0x09
#define TYPE_STRING 0x20
#define TYPE_SHORT_STRING 0x20
#define TYPE_LONG_STRING 0xA0

#define MAX_SHORT_STRING 0x1F
#define MAX_LONG_STRING 0x1FFF

using namespace std;

namespace microdb {
    

    class IndexDataumBuilder {
    private:
        MemBuffer mData;
        size_t mLocation;
        
    public:
        IndexDataumBuilder();
        
        IndexDataumBuilder& move();
        
        IndexDataumBuilder& addString(const char* cstr);
        IndexDataumBuilder& addString(const char* cstr, unsigned int len);
        IndexDataumBuilder& addString(const std::string& str);
        IndexDataumBuilder& addNumber(double value);
        
        std::string build();
        leveldb::Slice getSlice();
        
    };
    
    class IndexDataum {
    private:
        const uint8_t* mData;
        const size_t mSize;
        size_t mLocation;
        
        IndexDataum(const void* data, const size_t size, size_t location);
        
    public:
        IndexDataum(const void* data, const size_t size);
        IndexDataum(const leveldb::Slice& a);
        
        void reset();
        bool hasNext();
        void next();
        uint8_t getType();
        
        bool starts_with(IndexDataum& value);
        
        IndexDataum getString(leveldb::Slice& retval);
        IndexDataum getNumber(double& retval);
        
        int compare(IndexDataum& other);
    };

    class DBImpl : public DB {
    private:
        UUID mInstanceId;
        unique_ptr<leveldb::DB> mLevelDB;
        set< ViewQuery > mViews;
        
    public:
        
        DBImpl();
        virtual ~DBImpl();
        
        static const leveldb::Slice& metaKey();
        
        Status init(leveldb::DB* db);
        
        Status AddView(const std::string& viewName, const std::string& mapQuery);
        Status DeleteView(const std::string& viewName);

        Status Insert(const std::string& value, std::string* key);
        Status Update(const std::string& key, const std::string& value);
        Status Delete(const std::string& key);
        
    };
    
}

#endif
