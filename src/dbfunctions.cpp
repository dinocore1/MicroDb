
#include "microdb.h"


#include "dbfunctions.h"

namespace microdb {

    void createUUID(Environment* env, Value& retval, const std::vector< Selector* >& args) {
		UUID id = UUID::createRandom();
		retval = id.getString();
	}

    void hash(Environment* env, Value& retval, const std::vector< Selector* >& args) {
        char* buf;
        size_t size;
            
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
    
    void setValue(Environment* env, Value& retval, const std::vector< Selector* >& args) {
        
        if(args.size() == 3) {
            Value obj, key, value;
            args[0]->select(env, obj);
            
            if(obj.IsObject()) {
                args[1]->select(env, key);
                if(key.IsString()) {
                    args[2]->select(env, value);
                    obj[key.asString()] = value;
                }
            }
            retval = std::move(obj);
        }
    }

} // namespace microdb
