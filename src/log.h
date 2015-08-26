#ifndef MICRODB_LOG_H_
#define MICRODB_LOG_H_

#ifdef OS_ANDROID

#include <android/log.h>

#define  LOG_TAG    "microdb"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#else

#define LOGI(...)
#define LOGE(...)

#endif

#endif // MICRODB_LOG_H_