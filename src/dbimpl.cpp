
#include <string>
#include <iostream>

#include <leveldb/db.h>

#include "rapidjson/document.h"
#include "rapidjson/writer.h"
#include "rapidjson/stringbuffer.h"

#include <microdb/status.h>
#include <microdb/microdb.h>
#include "dbimpl.h"


using namespace std;
using namespace rapidjson;

extern "C" int yyparse();

namespace microdb {
    Status DB::Open(const std::string& dbdirpath, DB** dbptr) {
        DBImpl* retval = new DBImpl();
        
        leveldb::Options options;
        options.create_if_missing = true;
        
        leveldb::Status status = leveldb::DB::Open(options, dbdirpath, &retval->mLevelDB);
        if(!status.ok()){
            return ERROR;
        }
        
        string value;
        status = retval->mLevelDB->Get(leveldb::ReadOptions(), "microdb_meta", &value);
        if(status.ok()){
            Document doc;
            char* buf = (char*) value.c_str();
            doc.ParseInsitu(buf);
        } else {
            
            Document doc;
            doc.SetObject();
            doc.AddMember("current_version", 0, doc.GetAllocator());
            
            StringBuffer buffer;
            Writer<StringBuffer> writer(buffer);
            doc.Accept(writer);
            
            leveldb::Slice sval = buffer.GetString();
            retval->mLevelDB->Put(leveldb::WriteOptions(), "microdb_meta", sval);
        }
        
        
        
        
        
        *dbptr = retval;
        return OK;
    }
    
    DB::~DB() {
        
    }
    
    DBImpl::~DBImpl() {
        if(mLevelDB != nullptr){
          delete mLevelDB;
        }
    }
    
    Status DBImpl::Put(const std::string& key, const std::string& value) {
        
    }
}
