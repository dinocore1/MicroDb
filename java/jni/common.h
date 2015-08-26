#ifndef MICRODB_JNI_COMMON_H_
#define MICRODB_JNI_COMMON_H_

#include <jni.h>

typedef struct {
    jclass clazz;
    jfieldID mNativePtr;
} nativeIteratorClass_t;

extern nativeIteratorClass_t gNativeIteratorClass;

#endif // MICRODB_JNI_COMMON_H_