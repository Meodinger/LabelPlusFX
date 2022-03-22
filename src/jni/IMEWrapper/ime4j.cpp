#include "pch.h"
#include "ime4j.h"

using namespace IMEInterface;

JNIEXPORT jobjectArray JNICALL Java_ink_meodinger_lpfx_util_ime_IMEMain_getLanguages(JNIEnv* env, jclass clazz)
{
	auto langs = IMEMain::GetInstalledLanguages();
    auto array = env->NewObjectArray((jsize)langs->Length, env->FindClass("java/lang/String"), 0);
    for (size_t i = 0; i < langs -> Length; i++)
    {
        env->SetObjectArrayElement(array, (jsize) i, string2jsting(env, langs[i]));
    }

    return array;
}

JNIEXPORT jstring JNICALL Java_ink_meodinger_lpfx_util_ime_IMEMain_getLanguage(JNIEnv* env, jclass clazz)
{
    auto lang = IMEMain::GetInputLanguage();

    return string2jsting(env, lang);
}


JNIEXPORT jboolean JNICALL Java_ink_meodinger_lpfx_util_ime_IMEMain_setLanguage(JNIEnv* env, jclass clazz, jstring jString)
{
    auto string = jstring2string(env, jString);

    return IMEMain::SetInputLanguage(string) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_ink_meodinger_lpfx_util_ime_IMEMain_setImeConversionMode(JNIEnv* env, jclass clazz, jint jInt)
{
    // IMEMain::SetImeConversionMode((int) jInt);
}


