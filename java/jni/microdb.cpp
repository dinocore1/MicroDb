#include "log.h"
#include <jni.h>

extern jint driver_OnLoad(JNIEnv* env);
extern jint iterator_OnLoad(JNIEnv* env);


jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    if(iterator_OnLoad(env)) {
        return -1;
    }

    if(driver_OnLoad(env)) {
        return -1;
    }

    return JNI_VERSION_1_6;
}