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

/**
 * Get all available languages (cultures, not actual input methods)
 */
private external fun getLanguages(): Array<String>

/**
 * Get current language
 */
private external fun getLanguage(): String

/**
 * Set current language
 */
private external fun setLanguage(lang: String): Boolean

/**
 * Set IME Conversion Mode
 */
private external fun setImeConversionMode(hWnd: Long, conversionMode: Int, sentenceMode: Int): Boolean

// ----- JVM Method ----- //

val languages: List<String> by lazy(::getAvailableLanguages)
fun getAvailableLanguages(): List<String> {
    if (!enableJNI) return emptyList()

    if (isWin) return getLanguages().toList()
    else throw UnsupportedOperationException("Only usable in Win")
}

fun getCurrentLanguage(): String {
    if (!enableJNI) return emptyString()

    if (isWin) return getLanguage()
    else throw UnsupportedOperationException("Only usable in Win")
}

fun setCurrentLanguage(lang: String): Boolean {
    if (!enableJNI) return false

    if (isWin) return setLanguage(lang)
    else throw UnsupportedOperationException("Only usable in Win")
}

@Deprecated("Currently not functional")
fun setImeConversionMode(hWnd: Long, sentenceMode: ImeSentenceMode, vararg conversionModes: ImeConversionMode): Boolean {
    if (!enableJNI) return false

    var conversionMode = 0
    for (mode in conversionModes) conversionMode = conversionMode or mode.value

    if (isWin) return setImeConversionMode(hWnd, conversionMode, sentenceMode.value)
    else throw UnsupportedOperationException("Only usable in Win")
}

@Deprecated("Currently not functional")
fun setImeInputMode(hWnd: Long, mode: ImeMode): Boolean {
    if (!enableJNI) return false

    val conversion = when (mode) {
        ImeMode.OFF           -> 0b0000
        ImeMode.HIRAGANA      -> 0b1001
        ImeMode.KATAKANA      -> 0b1011
        ImeMode.ALPHA         -> 0b1000
        ImeMode.KATAKANA_HALF -> 0b0011
        ImeMode.ALPHA_HALF    -> 0b0000
    }

    if (isWin) return setImeConversionMode(hWnd, conversion, ImeSentenceMode.AUTOMATIC.value)
    else throw UnsupportedOperationException("Only usable in Win")
}
