#include <com_maintab_JniString.h>

JNIEXPORT jstring JNICALL Java_com_maintab_JniString_getJniStringFromNative  (JNIEnv *env, jobject)
{
    return env->NewStringUTF("  I am a new JniString, Haha roc,  Do you like me? ");
}