#include "pch.h"
#include "ime4j.h"

#pragma managed
#using "IMEInterface.dll"

using namespace System::Runtime::InteropServices;
using namespace IMEInterface;

JNIEXPORT jobjectArray JNICALL Java_ink_meodinger_lpfx_util_ime_IMEMain_getLanguages(JNIEnv* env, jclass clazz)
{
	auto langs = IMEInterface::IMEMain::GetInstalledLanguages();
    auto array = env->NewObjectArray((jsize)langs->Length, env->FindClass("java/lang/String"), 0);
    for (size_t i = 0; i < langs -> Length; i++)
    {
        size_t buffer = sizeof(WCHAR) / sizeof(CHAR) * langs[i]->Length;
        WCHAR* wchars = (WCHAR*)Marshal::StringToHGlobalUni(langs[i]).ToPointer();
        CHAR*  chars  = (CHAR*)malloc(buffer);

        wcstombs_s(0, chars, buffer, wchars, buffer);

        env->SetObjectArrayElement(array, (jsize) i, env->NewStringUTF(chars));
    }

    return array;
}

JNIEXPORT void JNICALL Java_ink_meodinger_lpfx_util_ime_IMEMain_setLanguage(JNIEnv* env, jclass clazz, jstring jString)
{
    auto chars  = env->GetStringUTFChars(jString, 0);
    auto string = gcnew System::String(chars);
    IMEInterface::IMEMain::SetInputLanguage(string);
}

