#include "common.h"

#include "log.h"
#include <jni.h>

#ifndef NELEM
# define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))
#endif

#include <microdb/status.h>
#include <microdb/value.h>
#include <microdb/serialize.h>
#include <microdb/microdb.h>

using namespace microdb;

nativeIteratorClass_t gNativeIteratorClass;

static jbyteArray getKey(JNIEnv* env, jobject thiz) {
    jbyteArray retval = env->NewByteArray(0);    
}



JNIEXPORT JNINativeMethod gIteratorMethods[] = {
    { "key", "()[B", (void*)getKey }
};

jint iterator_OnLoad(JNIEnv* env) {
    const char* className = "com/devsmart/microdb/NativeIterator";
    jclass clazz = env->FindClass(className);
    if(clazz == NULL) {
        ALOGE("Can not find %s", className);
        return -1;
    }
    
    gNativeIteratorClass.clazz = (jclass)env->NewGlobalRef(clazz);
    gNativeIteratorClass.mNativePtr = env->GetFieldID(clazz, "mNativePtr", "J");
    
    if(env->RegisterNatives(clazz, gIteratorMethods, NELEM(gIteratorMethods)) < 0) {
        ALOGE("RegisterNatives failed for %s", className);
        return -1;
    }

    return 0;
}