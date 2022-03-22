#include "pch.h"
#include "ime_util.h"

#pragma managed

using namespace System::Runtime::InteropServices;

void string2jstring(JNIEnv* env, System::String^ ori, jstring* dst)
{
    size_t buffer = sizeof(WCHAR) / sizeof(CHAR) * ori->Length;
    WCHAR* wchars = (WCHAR*)Marshal::StringToHGlobalUni(ori).ToPointer();
    CHAR* chars = (CHAR*)malloc(buffer);

    wcstombs_s(0, chars, buffer, wchars, buffer);

    *dst = env->NewStringUTF(chars);

    free(chars);
}

void jstring2string(JNIEnv* env, jstring ori, System::String^* dst)
{
    const char* chars = env->GetStringUTFChars(ori, 0);

    *dst = gcnew System::String(chars);

    env->ReleaseStringUTFChars(ori, chars);
}

JNIEXPORT jlong JNICALL Java_ink_meodinger_lpfx_util_ime_IMEUtil_getWindowHandle(JNIEnv* env, jclass clazz, jstring jString)
{

	size_t size = sizeof(WCHAR) * env->GetStringLength(jString);
	auto string = env->GetStringUTFChars(jString, JNI_FALSE);
	auto wchars = (WCHAR*)malloc(size);

	mbstowcs_s(0, wchars, size, string, size);

	HWND handle = FindWindow(NULL, wchars);
	env->ReleaseStringUTFChars(jString, JNI_FALSE);

	return (jlong)handle;
}
