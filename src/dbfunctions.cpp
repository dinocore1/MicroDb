
#include "dbfunctions.h"
#include "dbimpl.h"
#include "sha256.h"

#include "rapidjson/document.h"
#include "rapidjson/writer.h"
#include "rapidjson/stringbuffer.h"

using namespace rapidjson;

namespace microdb {

    
    rapidjson::Value& hash(Environment* env, const std::vector< Selector* >& args) {
        Value retval;
        
        if(args.size() >= 1) {
            StringBuffer keyBuffer;
            Writer<StringBuffer> keyWriter(keyBuffer);
            args[0]->select(env).Accept(keyWriter);
            
            std::string hashStr = sha256(keyBuffer.GetString());
            
            retval.SetString(hashStr.c_str(), hashStr.size(), env->getGlobalAllocator());
        } else {
            retval.SetNull();
        }
        
        return retval.Move();
    }
    
}

