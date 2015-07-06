
#include <cmath>
#include <string>
#include <iostream>

#include <leveldb/db.h>
#include <leveldb/write_batch.h>
#include <leveldb/comparator.h>

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


#define DOC_META "microdb_meta"
#define KEY_INSTANCEID "id"

namespace microdb {
    
    class MicroDBComparator : public leveldb::Comparator {
        
    public:
        
        int Compare(const leveldb::Slice& a, const leveldb::Slice& b) const {
            IndexDataum aIndex(a);
            IndexDataum bIndex(b);
            
            int retval = aIndex.compare(bIndex);
            return retval;
        }
        
        const char* Name() const { return "MicroDBComparator"; }
        void FindShortestSeparator(std::string*, const leveldb::Slice&) const { }
        void FindShortSuccessor(std::string*) const { }
        
    };
    
    static MicroDBComparator MICRODBCOMPARATOR;
    
    MicroDBComparator* MicroDBComparator() {
        return &MICRODBCOMPARATOR;
    }
    
    rapidjson::Value& indexMapEnvEmit(Environment* env, const std::vector< Selector* >& args);
    
    class IndexMapEnv : public Environment {
        
    private:
        ViewQuery* mView;
        unsigned int mCount;
        
    protected:
        WriteBatch* mWriteBatch;
        const std::string mObjId;
        
        inline void generateKey(IndexDataumBuilder& builder, rapidjson::Value& key) {
            builder.addString("i");
            builder.addString(mView->mName);
            
            if(key.IsNumber()) {
                builder.addNumber(key.GetDouble());
            } else {
                StringBuffer keyBuffer;
                Writer<StringBuffer> keyWriter(keyBuffer);
                key.Accept(keyWriter);
                builder.addString(keyBuffer.GetString());
            }
            
            builder.addString(mObjId);
            builder.addNumber(mCount++);
            
        }
        
    public:
        
        
        IndexMapEnv(const std::string& objId, WriteBatch* writeBatch)
        : mObjId(objId), mWriteBatch(writeBatch), mCount(0) {
            mFunctions["emit"] = indexMapEnvEmit;
            mFunctions["hash"] = hash;
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
                IndexDataumBuilder builder;
                generateKey(builder, args[0]->select(this));
                
                StringBuffer valueBuffer;
                Writer<StringBuffer> valueWriter(valueBuffer);
                if(args.size() >= 2) {
                    args[1]->select(this).Accept(valueWriter);
                }
                
                mWriteBatch->Put(builder.getSlice(), valueBuffer.GetString());
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
                IndexDataumBuilder builder;
                generateKey(builder, args[0]->select(this));
                mWriteBatch->Delete(builder.getSlice());
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
        options.comparator = MicroDBComparator();
        
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
    
    char META_KEY[5] = { TYPE_SHORT_STRING | 4, 'm', 'e', 't', 'a' };
    leveldb::Slice META_KEY_SLICE(META_KEY, 5);
    
    char VIEW_KEY[5] = { TYPE_SHORT_STRING | 4, 'v', 'i', 'e', 'w' };
    leveldb::Slice VIEW_KEY_SLICE(VIEW_KEY, 5);
    
    const leveldb::Slice& DBImpl::metaKey() {
        return META_KEY_SLICE;
    }
    
    Status DBImpl::init(leveldb::DB* db) {
        mLevelDB = db;
        
        Document metaDoc;
        string value;
        leveldb::Status status = mLevelDB->Get(leveldb::ReadOptions(), META_KEY_SLICE, &value);
        if(status.ok()){
            metaDoc.ParseInsitu((char*)value.c_str());
            
        } else {
            string instanceId = UUID::createRandom().getString();
            metaDoc.SetObject();
            metaDoc.AddMember(KEY_INSTANCEID, StringRef(instanceId.c_str()), metaDoc.GetAllocator());
            
            StringBuffer buffer;
            Writer<StringBuffer> writer(buffer);
            metaDoc.Accept(writer);
            
            status = mLevelDB->Put(leveldb::WriteOptions(), META_KEY_SLICE, buffer.GetString());
        }
        
        mInstanceId = UUID(metaDoc[KEY_INSTANCEID].GetString());
        
        unique_ptr<leveldb::Iterator> it(mLevelDB->NewIterator(leveldb::ReadOptions()));
        
        
        for(it->Seek(VIEW_KEY_SLICE); it->Valid() && it->key().starts_with(VIEW_KEY_SLICE); it->Next()){
            IndexDataum key(it->key());
            leveldb::Slice keyValue;
            key.getString(keyValue).getString(keyValue);
            
            
            Slice value = it->value();
            
            rapidjson::Document queryValue;
            queryValue.Parse(value.data());
            
            ViewQuery* query = new ViewQuery();
            
            query->mName = keyValue.ToString();
            
            query->compile(queryValue["map"].GetString());
            
            mViews.push_back(query);
            
        }
        
        
        return OK;
    }
    
    Status DBImpl::SetView(const std::string& viewName, const std::string& mapQuery) {
        
        unique_ptr<ViewQuery> query(new ViewQuery());
        if(!query->compile(mapQuery.c_str())){
            return PARSE_ERROR;
        }
        
        rapidjson::Document viewDoc;
        viewDoc.SetObject();
        
        viewDoc.AddMember("map", StringRef(mapQuery.data(), mapQuery.length()), viewDoc.GetAllocator());
        
        IndexDataumBuilder builder;
        builder.addString("view");
        builder.addString(viewName);
        
        StringBuffer buffer;
        Writer<StringBuffer> writer(buffer);
        viewDoc.Accept(writer);
        
        
        mLevelDB->Put(WriteOptions(), builder.getSlice(), buffer.GetString());
        
        mViews.push_back(query.release());
        
        
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
        batch.Put(IndexDataumBuilder().addString("o").addString(objKey).getSlice(), value);

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
    
}
