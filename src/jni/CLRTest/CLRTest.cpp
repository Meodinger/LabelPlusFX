#include <iostream>

#pragma managed
#using "IMEInterface.dll"

using namespace std;
using namespace IMEInterface;

int main()
{
	int index;

    auto langs = IMEMain::GetInstalledLanguages();
	for (size_t i = 0; i < langs->Length; i++)
	{
		printf("%s, ", langs[i]);
	}
	printf("\n");

	cin >> index;
	auto lang = langs[index];
	IMEMain::SetInputLanguage(lang);
	printf("%s\n", lang);

	cin >> index;
	IMEMain::SetImeConversionMode(index);
}