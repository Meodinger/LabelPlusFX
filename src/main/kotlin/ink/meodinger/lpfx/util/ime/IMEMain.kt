@file:JvmName("IMEMain")

package ink.meodinger.lpfx.util.ime

import ink.meodinger.lpfx.util.platform.enableJNI
import ink.meodinger.lpfx.util.platform.isWin
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
 */
val languages: List<String> by lazy(::getAvailableLanguages)

/**
 * Get all available languages (cultures)
 */
fun getAvailableLanguages(): List<String> {
    if (!enableJNI) return emptyList()

    if (isWin) return getLanguages().toList()
    else throw UnsupportedOperationException("Only usable in Win")
}

/**
 * Get current language
 */
fun getCurrentLanguage(): String {
    if (!enableJNI) return emptyString()

    if (isWin) return getLanguage()
    else throw UnsupportedOperationException("Only usable in Win")
}

/**
 * Set current language
 */
fun setCurrentLanguage(lang: String): Boolean {
    if (!enableJNI) return false

    if (isWin) return setLanguage(lang)
    else throw UnsupportedOperationException("Only usable in Win")
}

/**
 * Set IME Conversion Mode
 */
fun setImeConversionMode(hWnd: Long, sentenceMode: ImeSentenceMode, vararg conversionModes: ImeConversionMode): Boolean {
    if (!enableJNI) return false

    var conversionMode = 0
    for (mode in conversionModes) conversionMode = conversionMode or mode.value

    if (isWin) return setImeConversionMode(hWnd, conversionMode, sentenceMode.value)
    else throw UnsupportedOperationException("Only usable in Win")
}

/**
 * Set Japan IME Conversion Mode
 */
fun setJapanInputMode(hWnd: Long, mode: JapanMode): Boolean {
    if (!enableJNI) return false

    val conversion = when (mode) {
        JapanMode.HIRAGANA      -> 0b1001
        JapanMode.KATAKANA      -> 0b1011
        JapanMode.ALPHA         -> 0b1000
        JapanMode.KATAKANA_HALF -> 0b0011
        JapanMode.ALPHA_HALF    -> 0b0000
    }

    if (isWin) return setImeConversionMode(hWnd, conversion, ImeSentenceMode.AUTOMATIC.value)
    else throw UnsupportedOperationException("Only usable in Win")
}
