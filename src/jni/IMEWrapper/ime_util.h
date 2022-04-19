#include "jni.h"

#ifndef _IME_UTIL_H
#define _IME_UTIL_H
#ifdef __cplusplus
extern "C" {
#endif

void string2jstring(JNIEnv*, System::String^, jstring*);

void jstring2string(JNIEnv*, jstring, System::String^*);

/*
 * Class:     ink_meodinger_lpfx_util_ime_IMEUtil
 * Method:    getActiveWindow
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_ink_meodinger_lpfx_util_ime_IMEUtil_getActiveWindow(JNIEnv*, jclass);

/*
 * Class:     ink_meodinger_lpfx_util_ime_IMEUtil
 * Method:    getWindowHandle
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_ink_meodinger_lpfx_util_ime_IMEUtil_getWindowHandle(JNIEnv*, jclass, jstring);

#ifdef __cplusplus
}
#endif
#endif