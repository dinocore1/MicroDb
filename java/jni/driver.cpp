#include "common.h"

#include "log.h"


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

static jbyteArray get(JNIEnv* env, jobject thiz, jbyteArray key) {
    DB* database = (DB*)env->GetLongField(thiz, gNativeDriverClass.mNativePtr);
    
    jbyteArray retval = NULL;
    Value keyValue = byteArrayToValue(env, key);
    Value valueValue;
    if(database->Get(keyValue, valueValue) == OK) {
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

static void deleteobj(JNIEnv* env, jobject thiz, jbyteArray key) {
    DB* database = (DB*)env->GetLongField(thiz, gNativeDriverClass.mNativePtr);
    Value keyValue = byteArrayToValue(env, key);
    database->Delete(keyValue);
}

static jboolean queryIndex(JNIEnv* env, jobject thiz, jstring indexName, jobject it) {
    DB* database = (DB*)env->GetLongField(thiz, gNativeDriverClass.mNativePtr);
    
    const char* indexNameStr = env->GetStringUTFChars(indexName, NULL);
    
    Iterator* retit = database->QueryIndex(indexNameStr, "");
    env->SetLongField(it, gNativeIteratorClass.mNativePtr, (jlong)retit);
    
    env->ReleaseStringUTFChars(indexName, indexNameStr);
    
    return retit != NULL;
}

static jboolean addIndex(JNIEnv* env, jobject thiz, jstring indexName, jstring indexQuery) {
    DB* database = (DB*)env->GetLongField(thiz, gNativeDriverClass.mNativePtr);
    
    const char* indexNameStr = env->GetStringUTFChars(indexName, NULL);
    const char* queryStr = env->GetStringUTFChars(indexQuery, NULL);
    
    Status retCode = database->AddIndex(indexNameStr, queryStr);
    jboolean retval = retCode == OK ? JNI_TRUE : JNI_FALSE;
    
    env->ReleaseStringUTFChars(indexName, indexNameStr);
    env->ReleaseStringUTFChars(indexQuery, queryStr);
    
    return retval;
    
    
}

static void deleteIndex(JNIEnv* env, jobject thiz, jstring indexName) {
    
}


JNIEXPORT JNINativeMethod gDriverMethods[] = {
    { "open", "(Ljava/lang/String;Lcom/devsmart/microdb/NativeDriver;)Z", (void*)open_db },
    { "close", "()V", (void*)close_db },
    { "get", "([B)[B", (void*)get },
    { "insert", "([B)[B", (void*)insert },
    { "delete", "([B)V", (void*)deleteobj },
    { "queryIndex", "(Ljava/lang/String;Lcom/devsmart/microdb/NativeIterator;)Z", (void*)queryIndex },
    { "addIndex", "(Ljava/lang/String;Ljava/lang/String;)Z", (void*)addIndex },
    { "deleteIndex", "(Ljava/lang/String;)V", (void*)deleteIndex }
};

jint driver_OnLoad(JNIEnv* env) {
    const char* className = "com/devsmart/microdb/NativeDriver";
    jclass clazz = env->FindClass(className);
    if(clazz == NULL) {
        LOGE("Can not find %s", className);
        return -1;
    }

    gNativeDriverClass.clazz = (jclass)env->NewGlobalRef(clazz);
    gNativeDriverClass.mNativePtr = env->GetFieldID(clazz, "mNativePtr", "J");

    if(env->RegisterNatives(clazz, gDriverMethods, NELEM(gDriverMethods)) < 0) {
        LOGE("RegisterNatives failed for %s", className);
        return -1;
    }

    return 0;

}
