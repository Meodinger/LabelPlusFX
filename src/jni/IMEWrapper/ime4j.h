#include "jni.h"

#ifndef _IME4J_H
#define _IME4J_H
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     ink_meodinger_lpfx_util_ime_IMEMain
 * Method:    getLanguages
 * Signature: ()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_ink_meodinger_lpfx_util_ime_IMEMain_getLanguages(JNIEnv*, jclass);

/*
 * Class:     ink_meodinger_lpfx_util_ime_IMEMain
 * Method:    getLanguages
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_ink_meodinger_lpfx_util_ime_IMEMain_setLanguage(JNIEnv*, jclass, jstring);

/*
 * Class:     ink_meodinger_lpfx_util_ime_IMEMain
 * Method:    setImeConversionMode
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_ink_meodinger_lpfx_util_ime_IMEMain_setImeConversionMode(JNIEnv*, jclass, jint);

#ifdef __cplusplus
}
#endif
#endif