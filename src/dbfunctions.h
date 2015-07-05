
#ifndef DBFUNCTIONS_H_
#define DBFUNCTIONS_H_

#include "viewquery.h"

namespace microdb {


rapidjson::Value& hash(Environment* env, const std::vector< Selector* >& args);

    
}


#endif /* DBFUNCTIONS_H_ */