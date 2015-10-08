#ifndef MICRODB_LOG_
#define MICRODB_LOG_

#define  LOG_TAG    "microdb-jni"

#ifdef ANDROID
#include <android/log.h>
#  define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#  define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#  define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#  define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#else
#  define LOGD(...) 
#  define LOGI(...) 
#  define LOGE(...) 
#  define LOGW(...) 
#endif


#endif //MICRODB_LOG_