
#include "uuid.h"

#include <random>

namespace microdb {

std::random_device uuid_random;

UUID UUID::createRandom() {
    UUID retval;

    for(int i=0;i<16;i++){
      retval.mData[i] = uuid_random();
    }

    return retval;
}

}
