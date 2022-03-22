#include <iostream>

#pragma managed

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
	printf("Lang: %s\n", lang);

	cin >> index;
	printf("Index: %d\n", index);
	// IMEMain::SetImeConversionMode(index);
}