
#include <cmath>
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
#include "dbfunctions.h"


using namespace std;
using namespace rapidjson;
using namespace leveldb;

extern "C" int yyparse();

#define DOC_META "microdb_meta"
#define KEY_INSTANCEID "id"

namespace microdb {
    
    rapidjson::Value& indexMapEnvEmit(Environment* env, const std::vector< Selector* >& args);
    
    class IndexMapEnv : public Environment {
        
    private:
        ViewQuery* mView;
        unsigned int mCount;
        
    protected:
        WriteBatch* mWriteBatch;
        const std::string mObjId;
        
        inline std::string generateKey(rapidjson::Value& key) {
            Document keyDoc;
            keyDoc.SetArray();
            keyDoc.PushBack(StringRef(mView->mName.c_str()), keyDoc.GetAllocator());
            keyDoc.PushBack(key, keyDoc.GetAllocator());
            keyDoc.PushBack(StringRef(mObjId.c_str()), keyDoc.GetAllocator());
            keyDoc.PushBack(mCount++, keyDoc.GetAllocator());
            
            StringBuffer keyBuffer;
            Writer<StringBuffer> keyWriter(keyBuffer);
            keyDoc.Accept(keyWriter);
            
            return keyBuffer.GetString();
        }
        
    public:
        
        
        IndexMapEnv(const std::string& objId, WriteBatch* writeBatch)
        : mObjId(objId), mWriteBatch(writeBatch), mCount(0) {
            mFunctions["emit"] = indexMapEnvEmit;
        }
        
        void execute(rapidjson::Value& obj, ViewQuery* view) {
            mView = view;
            mVariables.clear();
            SetVar("obj", obj);
            mCount = 0;
            mView->execute(this);
        }
        
        virtual rapidjson::Value& emit(const std::vector< Selector* >& args) = 0;
        
        
    };
    
    class CreateIndexMapEnv : public IndexMapEnv {
    public:
        
        CreateIndexMapEnv(const std::string& objId, WriteBatch* writeBatch)
        : IndexMapEnv(objId, writeBatch) { }
        
        rapidjson::Value& emit(const std::vector< Selector* >& args) {
            if(!args.empty()) {
                Value& emitKey = args[0]->select(this);
                std::string keyStr = generateKey(emitKey);
                
                StringBuffer valueBuffer;
                Writer<StringBuffer> valueWriter(valueBuffer);
                if(args.size() >= 2) {
                    args[1]->select(this).Accept(valueWriter);
                }
                
                mWriteBatch->Put(keyStr, valueBuffer.GetString());
            }
            
            return Value(kNullType).Move();
        }
    };
    
    class DeleteIndexMapEnv : public IndexMapEnv {
    public:
        
        DeleteIndexMapEnv(const std::string& objId, WriteBatch* writeBatch)
        : IndexMapEnv(objId, writeBatch) { }
        
        rapidjson::Value& emit(const std::vector< Selector* >& args) {
            if(!args.empty()) {
                Value& emitKey = args[0]->select(this);
                std::string keyStr = generateKey(emitKey);
                mWriteBatch->Delete(keyStr);
            }
            
            return Value(kNullType).Move();
        }
    };
    
    rapidjson::Value& indexMapEnvEmit(Environment* env, const std::vector< Selector* >& args) {
        IndexMapEnv* mapEnv = (IndexMapEnv*)env;
        return mapEnv->emit(args);
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
        
        const std::string objKey = UUID::createRandom().getString();
        if(keyout != nullptr) {
            *keyout = objKey;
        }
        batch.Put("o" + objKey, value);

        CreateIndexMapEnv createIndex(objKey, &batch);
        
        for(ViewQuery* view : mViews) {
            createIndex.execute(doc, view);
        }
        
        
        mLevelDB->Write(WriteOptions(), &batch);
        
        return OK;
        
    }
    
    Status DBImpl::Update(const std::string& key, const std::string& value) {
        
        Document oldDoc, newDoc;
        newDoc.Parse(value.c_str());
        
        if(newDoc.HasParseError() || !newDoc.IsObject()) {
            return PARSE_ERROR;
        }
        
        const std::string dbkey = "o" + key;
        
        std::string oldDocStr;
        mLevelDB->Get(ReadOptions(), dbkey, &oldDocStr);
        oldDoc.ParseInsitu((char*)oldDocStr.c_str());
        
        WriteBatch batch;
        
        DeleteIndexMapEnv deleteIndex(key, &batch);
        CreateIndexMapEnv createIndex(key, &batch);
        
        batch.Delete(dbkey);
        batch.Put(dbkey, value);

        for(ViewQuery* view : mViews) {
            deleteIndex.execute(oldDoc, view);
            createIndex.execute(newDoc, view);
        }
        
        mLevelDB->Write(WriteOptions(), &batch);
        
        return OK;

    }
    
    Status DBImpl::Delete(const std::string &key) {
        
        const std::string dbkey = "o" + key;
        
        std::string valueStr;
        mLevelDB->Get(ReadOptions(), dbkey, &valueStr);
        
        Document doc;
        doc.ParseInsitu((char*)valueStr.c_str());
        
        if(doc.HasParseError() || !doc.IsObject()) {
            return PARSE_ERROR;
        }
        
        WriteBatch batch;
        
        DeleteIndexMapEnv deleteIndex(key, &batch);
        
        batch.Delete(dbkey);
        
        for(ViewQuery* view : mViews) {
            deleteIndex.execute(doc, view);
        }
        
        mLevelDB->Write(WriteOptions(), &batch);
        
        return OK;
    }
    
    std::string IndexDataum::convert(double value) {
        char data[9];
        data[0] = NUMBER_TYPE;
        memcpy(&data[1], &value, 8);
        
        return std::string(data, 9);
    }
    
    std::string IndexDataum::convert(const char* data, const unsigned int len) {
        std::string retval(len+1, 0);
        char* buff = (char*) retval.data();
        buff[0] = STRING_TYPE;
        memcpy(buff, data, len);
        
        return retval;
    }
    
    IndexDataum::IndexDataum(const char* data, const unsigned int length)
    : mData(data), mLength(length) { }
    
    uint8_t IndexDataum::getType() {
        return mData[0];
    }
    
    inline const char* IndexDataum::getString() {
        return &mData[1];
    }
    
    inline double IndexDataum::getNumber() {
        return *(double*)&mData[1];
    }
    
    int IndexDataum::compare(microdb::IndexDataum &other) {
        
        int retval = getType() - other.getType();
        if(retval == 0) {
            switch (retval) {
                case STRING_TYPE:
                    retval = strcmp(getString(), other.getString());
                    break;
                    
                case NUMBER_TYPE:
                    double a = getNumber();
                    double b = other.getNumber();
                    
                    if(a == b ||
                       std::abs(a-b)<std::abs(std::min(a,b))*std::numeric_limits<double>::epsilon()) {
                        retval = 0;
                    } else if (a < b) {
                        retval = -1;
                    } else {
                        retval = 1;
                    }
                    break;
            }
            
        }
        
        return retval;
    }
}
