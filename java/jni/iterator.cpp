#include "common.h"
#include "log.h"

using namespace microdb;

nativeIteratorClass_t gNativeIteratorClass;

static jbyteArray getKey(JNIEnv* env, jobject thiz) {
    Iterator* it = (Iterator*)env->GetLongField(thiz, gNativeIteratorClass.mNativePtr);
    
    jbyteArray retval = NULL;
    Value value = it->GetKey();
    retval = valueToByteArray(env, value);
    return retval;    
}

static jbyteArray primaryKey(JNIEnv* env, jobject thiz) {
    Iterator* it = (Iterator*)env->GetLongField(thiz, gNativeIteratorClass.mNativePtr);
    
    jbyteArray retval = NULL;
    Value value = it->GetPrimaryKey();
    retval = valueToByteArray(env, value);
    return retval;
}

static jbyteArray value(JNIEnv* env, jobject thiz) {
    Iterator* it = (Iterator*)env->GetLongField(thiz, gNativeIteratorClass.mNativePtr);
    
    jbyteArray retval = NULL;
    Value value = it->GetValue();
    retval = valueToByteArray(env, value);
    return retval;
}

static void destroy(JNIEnv* env, jobject thiz) {
    Iterator* it = (Iterator*)env->GetLongField(thiz, gNativeIteratorClass.mNativePtr);
    delete it;
    env->SetLongField(thiz, gNativeIteratorClass.mNativePtr, 0);
}

static void seek(JNIEnv* env, jobject thiz, jbyteArray key) {
    Iterator* it = (Iterator*)env->GetLongField(thiz, gNativeIteratorClass.mNativePtr);
    
    Value keyValue = byteArrayToValue(env, key);
    it->SeekTo(keyValue);
}

static jboolean valid(JNIEnv* env, jobject thiz) {
    Iterator* it = (Iterator*)env->GetLongField(thiz, gNativeIteratorClass.mNativePtr);
    
    return it->Valid() ? JNI_TRUE : JNI_FALSE;
}

static void next(JNIEnv* env, jobject thiz) {
    Iterator* it = (Iterator*)env->GetLongField(thiz, gNativeIteratorClass.mNativePtr);
    
    it->Next();
}

static void prev(JNIEnv* env, jobject thiz) {
    Iterator* it = (Iterator*)env->GetLongField(thiz, gNativeIteratorClass.mNativePtr);
    
    it->Prev();
}

JNIEXPORT JNINativeMethod gIteratorMethods[] = {
    { "key", "()[B", (void*)getKey },
    { "primaryKey", "()[B", (void*)primaryKey },
    { "value", "()[B", (void*)value },
    { "destroy", "()V", (void*)destroy },
    { "seek", "([B)V", (void*)seek },
    { "valid", "()Z", (void*)valid },
    { "next", "()V", (void*)next },
    { "prev", "()V", (void*)prev }
    
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