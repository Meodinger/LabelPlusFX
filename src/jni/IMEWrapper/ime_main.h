#include "pch.h"

#ifndef _IME_MAIN_H
#define _IME_MAIN_H
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
 * Method:    getLanguage
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_ink_meodinger_lpfx_util_ime_IMEMain_getLanguage(JNIEnv*, jclass);

/*
 * Class:     ink_meodinger_lpfx_util_ime_IMEMain
 * Method:    setLanguage
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_ink_meodinger_lpfx_util_ime_IMEMain_setLanguage(JNIEnv*, jclass, jstring);

/*
 * Class:     ink_meodinger_lpfx_util_ime_IMEMain
 * Method:    setImeConversionMode
 * Signature: (JII)Z
 */
JNIEXPORT jboolean JNICALL Java_ink_meodinger_lpfx_util_ime_IMEMain_setImeConversionMode(JNIEnv*, jclass, jlong, jint, jint);

#ifdef __cplusplus
}
#endif
#endif