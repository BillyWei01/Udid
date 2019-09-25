
#include <jni.h>
#include <string>
#include <sys/sysinfo.h>
#include <sys/system_properties.h>
#include<android/log.h>
#include <sys/utsname.h>

//#define TAG "jniTag"
//#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__)

extern "C"
JNIEXPORT jlong JNICALL
Java_com_horizon_did_NativeLib_getTotalRamSize(JNIEnv *env, jclass type) {
    struct sysinfo p;
    sysinfo(&p);
    return p.totalram;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_horizon_did_NativeLib_getSystemProperty(JNIEnv *env, jclass type, jstring name_) {
    const char *name = env->GetStringUTFChars(name_, 0);
    char ch[256]={0};
    __system_property_get(name, ch);
    env->ReleaseStringUTFChars(name_, name);
    return env->NewStringUTF(ch);
}

