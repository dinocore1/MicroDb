
#ifndef DBFUNCTIONS_H_
#define DBFUNCTIONS_H_

#include "viewquery.h"

namespace microdb {

//converts a json int value into an indexable value
rapidjson::Value& indexableInt(Environment* env, const std::vector< Selector* >& args);


rapidjson::Value& hash(Environment* env, const std::vector< Selector* >& args);

    
}


#endif /* DBFUNCTIONS_H_ */