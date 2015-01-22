#include <com_roc_example_JniExample.h>

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_roc_example_JniExample
 * Method:    getString
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_roc_example_JniExample_getString
  (JNIEnv *env, jclass cls)
{
    return env->NewStringUTF("  I am a reboot form jni world, i know 2 x 2 = ");
}

/*
 * Class:     com_roc_example_JniExample
 * Method:    square
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_roc_example_JniExample_square
  (JNIEnv *env, jclass cls, jint number)
{
    return number * number;
}
#ifdef __cplusplus
}
#endif