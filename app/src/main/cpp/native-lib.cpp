#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_desperado_mediaforandroid_jni_JNITest_sayHello(JNIEnv *env, jobject instance,
                                                        jstring name_) {
    jboolean isCopy;
    const char *name = env->GetStringUTFChars(name_, &isCopy);
    printf("is copy: %d\n", isCopy);

    if (name == NULL) {
        return NULL;
    }

    char buffer[128];
    sprintf(buffer, "hello %s\n", name);

    env->ReleaseStringUTFChars(name_, name);

    jsize len = env->GetStringLength(name_);
    char buf[128] = "hello";
    char *pbuff = buf + 6;
    env->GetStringUTFRegion(name_, 0, len, pbuff);

    return env->NewStringUTF(pbuff);
}