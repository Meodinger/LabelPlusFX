#ifndef PCH_H
#define PCH_H

#include "framework.h"
#include "stdlib.h"
#include "jni.h"

#pragma managed
using namespace System::Runtime::InteropServices;

jstring string2jsting(JNIEnv*, System::String^);

System::String^ jstring2string(JNIEnv*, jstring);

#endif //PCH_H
