// Pre-compile File

#include "pch.h"

jstring string2jsting(JNIEnv* env, System::String^ string)
{
    size_t buffer = sizeof(WCHAR) / sizeof(CHAR) * string->Length;
    WCHAR* wchars = (WCHAR*)Marshal::StringToHGlobalUni(string).ToPointer();
    CHAR*  chars  = (CHAR*)malloc(buffer);

    wcstombs_s(0, chars, buffer, wchars, buffer);

    return env->NewStringUTF(chars);
}

System::String^ jstring2string(JNIEnv* env, jstring jString)
{
    auto chars = env->GetStringUTFChars(jString, 0);
    
    return gcnew System::String(chars);
}