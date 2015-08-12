
#include <cmath>
#include <string>
#include <iostream>

#include <microdb/value.h>
#include <microdb/serialize.h>
#include <microdb/status.h>
#include <microdb/microdb.h>
#include "dbimpl.h"
#include "uuid.h"
#include "dbfunctions.h"


using namespace std;

#define DOC_META "microdb_meta"
#define KEY_INSTANCEID "id"

namespace microdb {


/*
    void indexMapEnvEmit(Environment* env, Value& retval, const std::vector< Selector* >& args);

    class IndexMapEnv : public Environment {

    protected:
        unsigned int mCount;
        const std::string* mObjId;
        const ViewQuery* mView;

    public:
        IndexMapEnv() {
            mFunctions["emit"] = indexMapEnvEmit;
            mFunctions["hash"] = hash;
        }

        virtual ~IndexMapEnv() {}

        void execute(const std::string& objId, Value& obj, const ViewQuery* view) {
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
                {
                Value argValue;
                args[0]->select(this, argValue);
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
*/

    Status DB::Open(const std::string& dburl, DB** dbptr) {

        
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

}
