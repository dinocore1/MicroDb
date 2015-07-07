
#ifndef MICRODB_STATUS_H_
#define MICRODB_STATUS_H_

namespace microdb {

typedef unsigned short Status;

static const Status OK = 0;
static const Status ERROR = 1;
static const Status PARSE_ERROR = 2;
static const Status EXISTS_ERROR = 3;
    
    
}

#endif
