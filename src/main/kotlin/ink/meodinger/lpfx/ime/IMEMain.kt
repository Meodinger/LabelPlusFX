@file:JvmName("IMEMain")

package ink.meodinger.lpfx.ime

import ink.meodinger.lpfx.Config.isWin

/**
 * Author: Meodinger
 * Date: 2022/3/19
 * Have fun with my code!
 */

/**
 * IME Lib Main
 */

// ----- Native Methods ----- //

private external fun getLanguages(): Array<String>

private external fun getLanguage(): String

private external fun setLanguage(lang: String): Boolean

private external fun setImeConversionMode(hWnd: Long, conversionMode: Int, sentenceMode: Int): Boolean

// ----- JVM Method ----- //

/**
 * All available languages (cultures)
 * @return A list contains all installed languages' culture codes
 */
val AvailableLanguages: List<String> get() {
    if (isWin) return getLanguages().toList() else throw UnsupportedOperationException("Only on Windows")
}

/**
 * Get current language
 * @return Current language's culture code
 */
fun getCurrentLanguage(): String {
    if (isWin) return getLanguage() else throw UnsupportedOperationException("Only on Windows")
}

/**
 * Set current language
 * @param lang The culture code of the wanted language
 * @return true if success, false if no installed language meet the requirement
 */
fun setCurrentLanguage(lang: String): Boolean {
    if (isWin) return setLanguage(lang) else throw UnsupportedOperationException("Only on Windows")
}

/**
 * Set IME Conversion Mode
 * @param hWnd The window handle of target window, use getWindowHandle(Stage) to get the hWnd
 * @param sentenceMode Sentence mode
 * @param conversionModes Conversion modes that will be combined
 * @return true if success
 */
fun setImeConversionMode(hWnd: Long, sentenceMode: ImeSentenceMode, vararg conversionModes: ImeConversionMode): Boolean {
    var conversionMode = 0
    for (mode in conversionModes) conversionMode = conversionMode or mode.value

    if (isWin) return setImeConversionMode(hWnd, conversionMode, sentenceMode.value)
    else throw UnsupportedOperationException("Only on Windows")
}
