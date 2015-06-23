
#include <string>
#include <iostream>

#include <leveldb/db.h>

#include "rapidjson/document.h"
#include "rapidjson/writer.h"
#include "rapidjson/stringbuffer.h"

#include <microdb/status.h>
#include <microdb/microdb.h>
#include "dbimpl.h"
#include "uuid.h"


using namespace std;
using namespace rapidjson;

extern "C" int yyparse();

#define DOC_META "microdb_meta"
#define KEY_INSTANCEID "id"

namespace microdb {
    Status DB::Open(const std::string& dbdirpath, DB** dbptr) {
        
        leveldb::DB* levelDB;
        leveldb::Options options;
        options.create_if_missing = true;
        
        leveldb::Status status = leveldb::DB::Open(options, dbdirpath, &levelDB);
        if(!status.ok()){
            return ERROR;
        }
        
        Document metaDoc;
        string value;
        status = levelDB->Get(leveldb::ReadOptions(), DOC_META, &value);
        if(status.ok()){
            char* buf = (char*) value.c_str();
            metaDoc.ParseInsitu(buf);
            
            
        } else {
            
            UUID id = UUID::createRandom();
            metaDoc.SetObject();
            metaDoc.AddMember(KEY_INSTANCEID, StringRef(id.getString().c_str()), metaDoc.GetAllocator());
            metaDoc.AddMember("current_version", 0, metaDoc.GetAllocator());
            
            StringBuffer buffer;
            Writer<StringBuffer> writer(buffer);
            metaDoc.Accept(writer);
            
            status = levelDB->Put(leveldb::WriteOptions(), DOC_META, buffer.GetString());
        }
        
        
        DBImpl* retval = new DBImpl(metaDoc[KEY_INSTANCEID].GetString());
        retval->mLevelDB = levelDB;
        
        
        *dbptr = retval;
        return OK;
    }
    
    DB::~DB() {
        
    }
    
    DBImpl::DBImpl(const std::string& id)
    : mInstanceId(id) {
        
    }
    
    DBImpl::~DBImpl() {
        if(mLevelDB != nullptr){
          delete mLevelDB;
        }
    }
    
    Status DBImpl::Put(const std::string& value, std::string* key = nullptr) {
        
        Document doc;
        doc.Parse(value.c_str());
        
        return OK;
        
    }
}
