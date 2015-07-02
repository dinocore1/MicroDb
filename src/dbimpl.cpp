
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
        
        DBImpl* retval = new DBImpl();
        retval->init(levelDB);
        
        *dbptr = retval;
        return OK;
    }
    
    DB::~DB() {
        
    }
    
    DBImpl::DBImpl() { }
    
    DBImpl::~DBImpl() {
        if(mLevelDB != nullptr){
          delete mLevelDB;
        }
    }
    
    Status DBImpl::init(leveldb::DB* db) {
        mLevelDB = db;
        
        Document metaDoc;
        string value;
        leveldb::Status status = mLevelDB->Get(leveldb::ReadOptions(), DOC_META, &value);
        if(status.ok()){
            char* buf = (char*) value.c_str();
            metaDoc.ParseInsitu(buf);
            
        } else {
            
            string instanceId = UUID::createRandom().getString();
            metaDoc.SetObject();
            metaDoc.AddMember(KEY_INSTANCEID, StringRef(instanceId.c_str()), metaDoc.GetAllocator());
            
            StringBuffer buffer;
            Writer<StringBuffer> writer(buffer);
            metaDoc.Accept(writer);
            
            status = mLevelDB->Put(leveldb::WriteOptions(), DOC_META, buffer.GetString());
        }
        
        mInstanceId = UUID(metaDoc[KEY_INSTANCEID].GetString());
        
        unique_ptr<leveldb::Iterator> it(mLevelDB->NewIterator(leveldb::ReadOptions()));
        
        
        for(it->Seek("view"); it->Valid() && it->key().starts_with("view"); it->Next()){
            
            
        }
        
        
        return OK;
    }
    
    Status DBImpl::Put(const std::string& value, std::string* key = nullptr) {
        
        Document doc;
        doc.Parse(value.c_str());
        
        if(doc.HasParseError() || !doc.IsObject()) {
            return PARSE_ERROR;
        }
        
        
        return OK;
        
    }
}
