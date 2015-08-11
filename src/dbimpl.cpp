
#include <cmath>
#include <string>
#include <iostream>

#include <leveldb/db.h>
#include <leveldb/write_batch.h>
#include <leveldb/comparator.h>

#include <microdb/value.h>
#include <microdb/serialize.h>
#include <microdb/status.h>
#include <microdb/microdb.h>
#include "dbimpl.h"
#include "uuid.h"
#include "dbfunctions.h"


using namespace std;
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

    void indexMapEnvEmit(Environment* env, Value& retval, const std::vector< Selector* >& args);

    class IndexMapEnv : public Environment {

    protected:
        unsigned int mCount;
        const std::string* mObjId;
        const ViewQuery* mView;
        WriteBatch* mWriteBatch;


        inline void generateKey(IndexDataumBuilder& builder, Value& key) {
            builder.addString("i");
            builder.addString(mView->mName);

            if(key.IsNumber()) {
                builder.addNumber(key.asFloat());
            } else {
                char* buf;
                uint32_t size;
        
                MemOutputStream out;
                UBJSONWriter writer(out);
                writer.write(key);
                out.GetData((void*&)buf, size);
                builder.addString(buf, size);
            }

            builder.addString(*mObjId);
            builder.addNumber(mCount++);

        }

    public:


        IndexMapEnv()
        : mWriteBatch(nullptr) {
            mFunctions["emit"] = indexMapEnvEmit;
            mFunctions["hash"] = hash;
        }

        virtual ~IndexMapEnv() {}

        void execute(const std::string& objId, Value& obj, const ViewQuery* view, WriteBatch* writeBatch = nullptr) {
            mWriteBatch = writeBatch;
            mObjId = &objId;
            mView = view;
            mVariables.clear();
            SetVar("obj", obj);
            mCount = 0;
            mView->execute(this);
        }

        virtual void emit(const std::vector< Selector* >& args) = 0;


    };

    class CreateIndexMapEnv : public IndexMapEnv {
    public:

        CreateIndexMapEnv()
        : IndexMapEnv() { }

        void emit(const std::vector< Selector* >& args) {
            if(!args.empty()) {
                IndexDataumBuilder builder;
                {
                Value argValue;
                args[0]->select(this, argValue);
                generateKey(builder, argValue);
                }

                char* buf = nullptr;
                uint32_t size = 0;
        
                MemOutputStream out;
                UBJSONWriter writer(out);
                    
                if(args.size() >= 2) {
                    Value argValue;
                    args[1]->select(this, argValue);
                    
                    writer.write(argValue);
                    out.GetData((void*&)buf, size);
                }

                mWriteBatch->Put(builder.getSlice(), leveldb::Slice(buf, size));
            }
        }
    };

    class DeleteIndexMapEnv : public IndexMapEnv {
    public:

        DeleteIndexMapEnv()
        : IndexMapEnv() { }

        void emit(const std::vector< Selector* >& args) {
            if(!args.empty()) {
                IndexDataumBuilder builder;
                Value argValue;
                args[0]->select(this, argValue);
                generateKey(builder, argValue);
                mWriteBatch->Delete(builder.getSlice());
            }
        }
    };

    void indexMapEnvEmit(Environment* env, Value& retval, const std::vector< Selector* >& args) {
        IndexMapEnv* mapEnv = (IndexMapEnv*)env;
        mapEnv->emit(args);
        retval.SetNull();
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
        //if(mLevelDB != nullptr){
        //  delete mLevelDB;
        //}
    }

    char META_KEY[5] = { TYPE_SHORT_STRING | 4, 'm', 'e', 't', 'a' };
    leveldb::Slice META_KEY_SLICE(META_KEY, 5);

    char VIEW_KEY[5] = { TYPE_SHORT_STRING | 4, 'v', 'i', 'e', 'w' };
    leveldb::Slice VIEW_KEY_SLICE(VIEW_KEY, 5);

    const leveldb::Slice& DBImpl::metaKey() {
        return META_KEY_SLICE;
    }

    Status DBImpl::init(leveldb::DB* db) {
        mLevelDB = unique_ptr<leveldb::DB>(db);

        Value metaDoc;
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

            Value queryValue;
            queryValue.Parse(value.data());

            ViewQuery query(keyValue.ToString());

            query.compile(queryValue["map"].GetString());
            mViews.insert(query);

        }


        return OK;
    }

    Status DBImpl::AddView(const std::string& viewName, const std::string& mapQuery) {

        ViewQuery query(viewName);

        if(mViews.find(query) != mViews.end()) {
            return EXISTS_ERROR;
        }

        if(!query.compile(mapQuery.c_str())){
            return PARSE_ERROR;
        }

        mViews.insert(query);

        //build index
        IndexDataumBuilder idxPrefix;
        idxPrefix.addString("o");
        leveldb::Slice prefix = idxPrefix.getSlice();

        CreateIndexMapEnv createIndex;


        unique_ptr<leveldb::Iterator> indexIt(mLevelDB->NewIterator(leveldb::ReadOptions()));
        for(indexIt->Seek(prefix); indexIt->Valid() && indexIt->key().starts_with(prefix); indexIt->Next()) {
            std::string objId(indexIt->key().data(), indexIt->key().size());
            std::string objStrValue(indexIt->value().data(), indexIt->value().size());

            Value objValue;
            objValue.ParseInsitu((char*)objStrValue.c_str());

            WriteBatch batch;
            createIndex.execute(objId, objValue, &query, &batch);
            mLevelDB->Write(WriteOptions(), &batch);
        }

        Value viewDoc;
        viewDoc.SetObject();

        viewDoc.AddMember("map", StringRef(mapQuery.data(), mapQuery.length()), viewDoc.GetAllocator());

        IndexDataumBuilder builder;
        builder.addString("view");
        builder.addString(viewName);

        StringBuffer buffer;
        Writer<StringBuffer> writer(buffer);
        viewDoc.Accept(writer);

        mLevelDB->Put(WriteOptions(), builder.getSlice(), buffer.GetString());

        return OK;
    }

    Status DBImpl::DeleteView(const std::string& viewName) {
        ViewQuery query(viewName);

        if(mViews.find(query) != mViews.end()) {
            return EXISTS_ERROR;
        }

        unique_ptr<leveldb::Iterator> indexIt(mLevelDB->NewIterator(leveldb::ReadOptions()));

        IndexDataumBuilder idxPrefix;
        idxPrefix.addString("i");
        idxPrefix.addString(viewName);
        leveldb::Slice prefix = idxPrefix.getSlice();

        for(indexIt->Seek(prefix); indexIt->Valid() && indexIt->key().starts_with(prefix); indexIt->Next()) {
            mLevelDB->Delete(WriteOptions(), indexIt->key());
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
        batch.Put(IndexDataumBuilder().addString("o").addString(objKey).getSlice(), value);


        CreateIndexMapEnv createIndex;

        for(const ViewQuery& view : mViews) {
            createIndex.execute(objKey, doc, &view, &batch);
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

        DeleteIndexMapEnv deleteIndex;
        CreateIndexMapEnv createIndex;

        batch.Delete(dbkey);
        batch.Put(dbkey, value);

        for(const ViewQuery& view : mViews) {
            deleteIndex.execute(key, oldDoc, &view, &batch);
            createIndex.execute(key, newDoc, &view, &batch);
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

        DeleteIndexMapEnv deleteIndex;

        batch.Delete(dbkey);

        for(const ViewQuery& view : mViews) {
            deleteIndex.execute(key, doc, &view, &batch);
        }

        mLevelDB->Write(WriteOptions(), &batch);

        return OK;
    }

}
