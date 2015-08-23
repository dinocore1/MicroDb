#ifndef MICRODB_LOG_
#define MICRODB_LOG_

#include <android/log.h>

#define  LOG_TAG    "microdb-jni"
#define  ALOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)


#endif //MICRODB_LOG_