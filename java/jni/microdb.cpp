
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

static struct {
	jclass clazz;
    jfieldID mNativePtr;
} gNativeDriverClass;


static jboolean open_db(JNIEnv* env, jclass clazz, jstring dbpath, jobject driver) {

    jboolean retval = JNI_FALSE;
    const char* str = env->GetStringUTFChars(dbpath, NULL);
    DB* database;
    if(DB::Open(str, &database) == OK){

        env->SetLongField(driver, gNativeDriverClass.mNativePtr, (jlong)database);
        retval = JNI_TRUE;
    } else {
        retval = JNI_FALSE;
    }

    env->ReleaseStringUTFChars(dbpath, str);

    return retval;
}

static void close_db(JNIEnv* env, jobject thiz) {
    DB* database = (DB*)env->GetLongField(thiz, gNativeDriverClass.mNativePtr);
    if(database != NULL) {
        delete database;
    }
    env->SetLongField(thiz, gNativeDriverClass.mNativePtr, 0);
}

static jbyteArray load_obj(JNIEnv* env, jobject thiz, jbyteArray key) {

    jbyteArray retval = env->NewByteArray(0);
    return retval;
}

static jbyteArray save_obj(JNIEnv* env, jobject thiz, jbyteArray data) {

    jbyteArray retval = env->NewByteArray(0);
    return retval;
}

JNIEXPORT JNINativeMethod gDriverMethods[] = {
    { "open", "(Ljava/lang/String;Lcom/devsmart/microdb/NativeDriver;)Z", (void*)open_db },
    { "close", "()V", (void*)close_db },
    { "load", "([B)[B", (void*)load_obj },
    { "save", "([B)[B", (void*)save_obj }
};

static jint driver_OnLoad(JNIEnv* env) {
    const char* className = "com/devsmart/microdb/NativeDriver";
    jclass clazz = env->FindClass(className);
    if(clazz == NULL) {
        ALOGE("Can not find %s", className);
        return -1;
    }

    gNativeDriverClass.clazz = (jclass)env->NewGlobalRef(clazz);
    gNativeDriverClass.mNativePtr = env->GetFieldID(clazz, "mNativePtr", "J");

    if(env->RegisterNatives(clazz, gDriverMethods, NELEM(gDriverMethods)) < 0) {
        ALOGE("RegisterNatives failed for %s", className);
        return -1;
    }

    return 0;

}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    if(driver_OnLoad(env)) {
        return -1;
    }

    return JNI_VERSION_1_6;
}
