@file:JvmName("IMEMain")

package ink.meodinger.lpfx.util.ime

import ink.meodinger.lpfx.Config.enableJNI
import ink.meodinger.lpfx.Config.isWin
import ink.meodinger.lpfx.util.string.emptyString

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
 * All available languages (cultures) by lazy
 * @return A list contains all installed languages' culture codes
 */
val availableLanguages: List<String> by lazy {
    if (!enableJNI) return@lazy emptyList()

    if (isWin) return@lazy getLanguages().toList()
    else throw UnsupportedOperationException("Only Win")
}

/**
 * Get current language
 * @return Current language's culture code
 */
fun getCurrentLanguage(): String {
    if (!enableJNI) return emptyString()

    if (isWin) return getLanguage()
    else throw UnsupportedOperationException("Only Win")
}

/**
 * Set current language
 * @param lang The culture code of the wanted language
 * @return true if success, false if no installed language meet the requirement
 */
fun setCurrentLanguage(lang: String): Boolean {
    if (!enableJNI) return false

    if (isWin) return setLanguage(lang)
    else throw UnsupportedOperationException("Only Win")
}

/**
 * Set IME Conversion Mode
 * @param hWnd The window handle of target window, use getWindowHandle(Stage) to get the hWnd
 * @param sentenceMode Sentence mode
 * @param conversionModes Conversion modes that will be combined
 * @return true if success
 */
fun setImeConversionMode(hWnd: Long, sentenceMode: ImeSentenceMode, vararg conversionModes: ImeConversionMode): Boolean {
    if (!enableJNI) return false

    var conversionMode = 0
    for (mode in conversionModes) conversionMode = conversionMode or mode.value

    if (isWin) return setImeConversionMode(hWnd, conversionMode, sentenceMode.value)
    else throw UnsupportedOperationException("Only Win")
}
