
#include "log.h"
#include <jni.h>

#include <microdb/status.h>
#include <microdb/value.h>
#include <microdb/serialize.h>
#include <microdb/microdb.h>

static struct {
	jclass clazz;
    jfieldID mNativePtr;
} gNativeDriverClass;

static jint driver_OnLoad(JNIEnv* env) {
    const char* className = "com/devsmart/microdb/NativeDriver";
    jclass clazz = env->FindClass(className);
    if(clazz == NULL) {
        ALOGE("Can not find %s", className);
        return -1;
    }

    gNativeDriverClass.clazz = (jclass)env->NewGlobalRef(clazz);
    gNativeDriverClass.mNativePtr = env->GetFieldID(clazz, "mNativePtr", "J");

}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    // Get jclass with env->FindClass.
    // Register methods with env->RegisterNatives.

    ALOGI("hello from native");

    return JNI_VERSION_1_6;
}
