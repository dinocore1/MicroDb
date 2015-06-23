//
//  uuid.h
//  MicroDB
//
//  Created by Paul Soucy on 6/22/15.
//
//

#ifndef MicroDB_uuid_h
#define MicroDB_uuid_h

#include <string>

namespace microdb {
    class UUID {
    public:
        static UUID createRandom();
        
        std::string getString();
    };
}

#endif
