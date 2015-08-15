
#ifndef DBFUNCTIONS_H_
#define DBFUNCTIONS_H_

#include "viewquery.h"

namespace microdb {


	void hash(Environment* env, Value& retval, const std::vector< Selector* >& args);
		
	/**
	* sets and obj's value
	* arg[0] [arg[1]] <= arg[2]
	*/
	void setValue(Environment*, Value& retval, const std::vector< Selector*> & args);


} //namespace microdb


#endif /* DBFUNCTIONS_H_ */
