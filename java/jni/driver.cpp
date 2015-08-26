
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

static Value byteArrayToValue(JNIEnv* env, jbyteArray array) {
    const size_t len = env->GetArrayLength(array);
    jbyte* arrayData = env->GetByteArrayElements(array, NULL);
    
    MemInputStream in((const byte*)arrayData, len);
    UBJSONReader reader(in);
    
    Value retval;
    reader.read(retval);
    env->ReleaseByteArrayElements(array, arrayData, JNI_ABORT);
    return retval;
}

static jbyteArray valueToByteArray(JNIEnv* env, const Value& data) {
    MemOutputStream out;
    UBJSONWriter writer(out);
    writer.write(data);
    
    jbyte* buf;
    size_t len;
    out.GetData((void*&)buf, len);
    
    jbyteArray retval = env->NewByteArray(len);
    env->SetByteArrayRegion(retval, 0, len, buf);
    return retval;
}

static jbyteArray get(JNIEnv* env, jobject thiz, jbyteArray key) {
    DB* database = (DB*)env->GetLongField(thiz, gNativeDriverClass.mNativePtr);
    
    jbyteArray retval = NULL;
    Value keyValue = byteArrayToValue(env, key);
    Value valueValue;
    if(database->Get(key, valueValue) == OK) {
        retval = valueToByteArray(env, valueValue);
    }

    return retval;
}

static jbyteArray insert(JNIEnv* env, jobject thiz, jbyteArray data) {
    DB* database = (DB*)env->GetLongField(thiz, gNativeDriverClass.mNativePtr);
    
    jbyteArray retval = NULL;
    Value valueValue = byteArrayToValue(env, data);
    Value key;
    if(database->Insert(key, valueValue) == OK) {
        retval = valueToByteArray(env, key);
    }

    
    return retval;
}

static jobject queryIndex(JNIEnv* env, jobject thiz, jstring indexName) {
    
}

static void addIndex(JNIEnv* env, jobject thiz, jstring indexName, jstring indexQuery) {
    
}

static void deleteIndex(JNIEnv* env, jobject thiz, jstring indexName) {
    
}


JNIEXPORT JNINativeMethod gDriverMethods[] = {
    { "open", "(Ljava/lang/String;Lcom/devsmart/microdb/NativeDriver;)Z", (void*)open_db },
    { "close", "()V", (void*)close_db },
    { "get", "([B)[B", (void*)get },
    { "insert", "([B)[B", (void*)insert },
    { "queryIndex", "(Ljava/lang/String;)Lcom/devsmart/microdb/NativeIterator;", (void*)queryIndex },
    { "addIndex", "(Ljava/lang/String;Ljava/lang/String;)V", (void*)addIndex },
    { "deleteIndex", "(Ljava/lang/String;)V", (void*)deleteIndex }
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
