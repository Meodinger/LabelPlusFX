using System;
using System.Runtime.InteropServices;
using System.Windows.Forms;
using System.Windows.Input;

namespace IMEInterface
{
    public static class IMEMain
    {

        public static string[] GetInstalledLanguages()
        {
            var languages = InputLanguage.InstalledInputLanguages;
            var langNames = new string[languages.Count];
            for (int i = 0; i < languages.Count; i++)
            {
                langNames[i] = languages[i].Culture.Name;
            }
            return langNames;
        }

        public static void SetInputLanguage(string name)
        {
            foreach (InputLanguage language in InputLanguage.InstalledInputLanguages)
            {
                if (language.Culture.Name.Equals(name))
                {
                    InputLanguage.CurrentInputLanguage = language;
                    break;
                }
            }
        }

        [Obsolete("Currently is not usable.", true)]
        public static void SetImeConversionMode(int mode)
        {
            // InputMethod.Current.ImeConversionMode = (ImeConversionModeValues)mode;
        }

    }
}
