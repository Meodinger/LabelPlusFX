#include "pch.h"
#include "ime_util.h"

#define WCHAR_RATIO (sizeof(WCHAR) / sizeof(CHAR))

#pragma managed

using namespace System::Runtime::InteropServices;

void string2jstring(JNIEnv* env, System::String^ ori, jstring* dst)
{
    size_t size   = WCHAR_RATIO * ori->Length;
    CHAR*  chars  = (CHAR*)malloc(sizeof(CHAR) * size);
    WCHAR* wchars = (WCHAR*)Marshal::StringToHGlobalUni(ori).ToPointer();
    
    wcstombs_s(0, chars, size, wchars, size);

    *dst = env->NewStringUTF(chars);

    free(chars);
}

void jstring2string(JNIEnv* env, jstring ori, System::String^* dst)
{
    const char* chars = env->GetStringUTFChars(ori, 0);

    *dst = gcnew System::String(chars);

    env->ReleaseStringUTFChars(ori, chars);
}

JNIEXPORT jlong JNICALL Java_ink_meodinger_lpfx_ime_IMEUtil_getActiveWindow(JNIEnv*, jclass)
{
    return (jlong)GetForegroundWindow();
}

JNIEXPORT jlong JNICALL Java_ink_meodinger_lpfx_ime_IMEUtil_getWindowHandle(JNIEnv* env, jclass clazz, jstring title)
{
    size_t len  = WCHAR_RATIO * env->GetStringLength(title);
    auto wchars = (WCHAR*)malloc(sizeof(WCHAR) * len);
    auto string = env->GetStringUTFChars(title, JNI_FALSE);

    mbstowcs_s(0, wchars, len, string, len);

    env->ReleaseStringUTFChars(title, JNI_FALSE);

    return (jlong)FindWindow(NULL, wchars);
}
