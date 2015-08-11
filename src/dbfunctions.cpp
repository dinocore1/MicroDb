
#include <microdb/value.h>
#include <microdb/serialize.h>

#include "dbfunctions.h"
#include "dbimpl.h"
#include "sha256.h"

namespace microdb {


    void hash(Environment* env, Value& retval, const std::vector< Selector* >& args) {
        char* buf;
        uint32_t size;
            
        if(args.size() >= 1) {
            MemOutputStream out;
            UBJSONWriter writer(out);
            
            Value argValue;
            args[0]->select(env, argValue);
            
            writer.write(argValue);

            out.GetData((void*&)buf, size);
            
            retval = sha256( std::string(buf, size) );
        } else {
            retval.SetNull();
        }
    }

}
