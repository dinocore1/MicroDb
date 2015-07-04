
#include <string>
#include <iostream>

#include <leveldb/db.h>
#include <leveldb/write_batch.h>

#include "rapidjson/document.h"
#include "rapidjson/writer.h"
#include "rapidjson/stringbuffer.h"

#include <microdb/status.h>
#include <microdb/microdb.h>
#include "dbimpl.h"
#include "uuid.h"


using namespace std;
using namespace rapidjson;
using namespace leveldb;

extern "C" int yyparse();

#define DOC_META "microdb_meta"
#define KEY_INSTANCEID "id"

namespace microdb {
    
    
    
    class IndexMapEnv : public Environment {
        
    public:
        ViewQuery* mView;
        WriteBatch* mWriteBatch;
        std::string mObjId;
        unsigned int mCount;
        
        IndexMapEnv()
        : mCount(0) {
            
        }
        
        void emit() {
            
        }
        
        void clear() {
            mVariables.clear();
        }
        
    };
    
    rapidjson::Value& indexAddEmit(Environment* env, const std::vector< Selector* >& args) {

        IndexMapEnv* mapEnv = (IndexMapEnv*)env;
        
        if(!args.empty()) {
            Value& emitKey = args[0]->select(env);
        

            Document keyDoc;
            keyDoc.SetArray();
            keyDoc.PushBack(StringRef(mapEnv->mView->mName.c_str()), keyDoc.GetAllocator());
            keyDoc.PushBack(emitKey, keyDoc.GetAllocator());
            keyDoc.PushBack(StringRef(mapEnv->mObjId.c_str()), keyDoc.GetAllocator());
            keyDoc.PushBack(mapEnv->mCount++, keyDoc.GetAllocator());
            
            StringBuffer keyBuffer;
            Writer<StringBuffer> keyWriter(keyBuffer);
            keyDoc.Accept(keyWriter);
            
            
            mapEnv->mWriteBatch->Put(keyBuffer.GetString(), "");
            
            
        }
        
        return Value(kNullType).Move();
    }
    
    rapidjson::Value& indexDeleteEmit(Environment* env, const std::vector< Selector* >& args) {
        
        IndexMapEnv* mapEnv = (IndexMapEnv*)env;
        
        if(!args.empty()) {
            Value& emitKey = args[0]->select(env);
            
            
            Document keyDoc;
            keyDoc.SetArray();
            keyDoc.PushBack(StringRef(mapEnv->mView->mName.c_str()), keyDoc.GetAllocator());
            keyDoc.PushBack(emitKey, keyDoc.GetAllocator());
            keyDoc.PushBack(StringRef(mapEnv->mObjId.c_str()), keyDoc.GetAllocator());
            keyDoc.PushBack(mapEnv->mCount++, keyDoc.GetAllocator());
            
            StringBuffer keyBuffer;
            Writer<StringBuffer> keyWriter(keyBuffer);
            keyDoc.Accept(keyWriter);
            
            
            mapEnv->mWriteBatch->Delete(keyBuffer.GetString());
            
            
        }
        
        return Value(kNullType).Move();
    }
    
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
            Slice key = it->key();
            Slice value = it->value();
            
            rapidjson::Document queryValue;
            queryValue.ParseInsitu((char*)value.data());
            
            ViewQuery* query = new ViewQuery();
            query->mName = key.ToString();
            query->mName.erase(0, 4);
            
            query->compile(queryValue["map"].GetString());
            
            mViews.push_back(query);
            
        }
        
        
        return OK;
    }
    
    Status DBImpl::Insert(const std::string& value, std::string* keyout = nullptr) {
        
        Document doc;
        doc.Parse(value.c_str());
        
        if(doc.HasParseError() || !doc.IsObject()) {
            return PARSE_ERROR;
        }
        
        WriteBatch batch;
        

        std::string key = UUID::createRandom().getString();
        batch.Put("o" + key, value);

        
        for(ViewQuery* view : mViews) {
            
        }
        
        
        mLevelDB->Write(WriteOptions(), &batch);
        
        return OK;
        
    }
    
    Status DBImpl::Delete(const std::string &key) {
        
        //get the object and run it thought each view's map function to
        //generate index keys, then delete these index keys
        
        return OK;
    }
}
