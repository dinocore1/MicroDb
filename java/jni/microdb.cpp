
#include "common.h"
#include "log.h"


extern jint driver_OnLoad(JNIEnv* env);
extern jint iterator_OnLoad(JNIEnv* env);

using namespace microdb;

Value byteArrayToValue(JNIEnv* env, jbyteArray array) {
    const size_t len = env->GetArrayLength(array);
    jbyte* arrayData = env->GetByteArrayElements(array, NULL);
    
    MemInputStream in((const byte*)arrayData, len);
    UBJSONReader reader(in);
    
    Value retval;
    reader.read(retval);
    env->ReleaseByteArrayElements(array, arrayData, JNI_ABORT);
    return retval;
}

jbyteArray valueToByteArray(JNIEnv* env, const Value& data) {
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