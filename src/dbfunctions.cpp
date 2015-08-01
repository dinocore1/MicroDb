
#include "dbfunctions.h"
#include "dbimpl.h"
#include "sha256.h"

#include "rapidjson/document.h"
#include "rapidjson/writer.h"
#include "rapidjson/stringbuffer.h"

using namespace rapidjson;

namespace microdb {


    void hash(Environment* env, rapidjson::Value& retval, const std::vector< Selector* >& args) {

        if(args.size() >= 1) {
            StringBuffer keyBuffer;
            Writer<StringBuffer> keyWriter(keyBuffer);
            rapidjson::Value argValue;
            args[0]->select(env, argValue);
            argValue.Accept(keyWriter);

            std::string hashStr = sha256(keyBuffer.GetString());

            retval.SetString(hashStr.c_str(), hashStr.size(), env->getGlobalAllocator());
        } else {
            retval.SetNull();
        }
    }

}
