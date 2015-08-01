
#ifndef DBFUNCTIONS_H_
#define DBFUNCTIONS_H_

#include "viewquery.h"

namespace microdb {


void hash(Environment* env, rapidjson::Value& retval, const std::vector< Selector* >& args);


}


#endif /* DBFUNCTIONS_H_ */
