
#include "dbfunctions.h"
#include "dbimpl.h"

using namespace rapidjson;

namespace microdb {

    rapidjson::Value& indexableValue(Environment* env, const std::vector< Selector* >& args) {
        Value retval;
        
        if(args.size() >= 1) {
            Value& data = args[0]->select(env);
            if(data.IsNumber()) {
                std::string indexValue = IndexDataum::convert(data.GetDouble());
                retval.SetString(indexValue.data(), indexValue.size(), env->getGlobalAllocator());
            } else if(data.IsString()) {
                std::string indexValue = IndexDataum::convert(data.GetString(), data.GetStringLength());
                retval.SetString(indexValue.data(), indexValue.size(), env->getGlobalAllocator());
            }
            
        }
        
        return retval.Move();
        
    }
    
}

