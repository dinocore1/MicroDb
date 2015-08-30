#ifndef MICRODB_JNI_COMMON_H_
#define MICRODB_JNI_COMMON_H_

#include <jni.h>

#ifndef NELEM
# define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))
#endif

#include <microdb/status.h>
#include <microdb/value.h>
#include <microdb/serialize.h>
#include <microdb/microdb.h>

typedef struct {
    jclass clazz;
    jfieldID mNativePtr;
} nativeIteratorClass_t;

extern nativeIteratorClass_t gNativeIteratorClass;

microdb::Value byteArrayToValue(JNIEnv* env, jbyteArray array);
jbyteArray valueToByteArray(JNIEnv* env, const microdb::Value& data);

#endif // MICRODB_JNI_COMMON_H_