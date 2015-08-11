
#include <microdb/value.h>
#include "dbfunctions.h"
#include "dbimpl.h"
#include "sha256.h"

namespace microdb {


    void hash(Environment* env, Value& retval, const std::vector< Selector* >& args) {

        if(args.size() >= 1) {
            StringBuffer keyBuffer;
            Writer<StringBuffer> keyWriter(keyBuffer);
            Value argValue;
            args[0]->select(env, argValue);
            argValue.Accept(keyWriter);

            std::string hashStr = sha256(keyBuffer.GetString());

            retval.SetString(hashStr.c_str(), hashStr.size(), env->getGlobalAllocator());
        } else {
            retval.SetNull();
        }
    }

}
