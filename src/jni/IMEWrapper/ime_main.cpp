#include "pch.h"
#include "ime_util.h"
#include "ime_main.h"

#pragma comment(lib, "imm32.lib")
#pragma managed

using namespace IMEInterface;

JNIEXPORT jobjectArray JNICALL Java_ink_meodinger_lpfx_util_ime_IMEMain_getLanguages(JNIEnv* env, jclass clazz)
{
	auto langs = IMEMain::GetInstalledLanguages();
    auto array = env->NewObjectArray((jsize)langs->Length, env->FindClass("java/lang/String"), 0);

    jstring lang;
    for (int i = 0; i < langs -> Length; i++)
    {
        string2jstring(env, langs[i], &lang);
        env->SetObjectArrayElement(array, (jsize) i, lang);
    }

    return array;
}

JNIEXPORT jstring JNICALL Java_ink_meodinger_lpfx_util_ime_IMEMain_getLanguage(JNIEnv* env, jclass clazz)
{
    auto lang = IMEMain::GetInputLanguage();

    jstring string;
    string2jstring(env, lang, &string);
    return string;
}

JNIEXPORT jboolean JNICALL Java_ink_meodinger_lpfx_util_ime_IMEMain_setLanguage(JNIEnv* env, jclass clazz, jstring language)
{
    System::String^ string;
    jstring2string(env, language, &string);
    bool retval = IMEMain::SetInputLanguage(string);

    return retval ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_ink_meodinger_lpfx_util_ime_IMEMain_setImeConversionMode(JNIEnv* env, jclass clazz, jlong hWnd, jint conversionMode, jint sentenceMode)
{
    HWND hwnd = (HWND)hWnd;
    HIMC himc = ImmGetContext(hwnd);

    BOOL retval = TRUE;
    retval &= ImmSetOpenStatus(himc, TRUE);
    retval &= ImmSetConversionStatus(himc, conversionMode, sentenceMode);
    retval &= ImmReleaseContext(hwnd, himc);
    return retval;
}
