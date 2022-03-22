@file:JvmName("IMEMain")

package ink.meodinger.lpfx.util.ime

import ink.meodinger.lpfx.util.platform.isWin

/**
 * Author: Meodinger
 * Date: 2022/3/19
 * Have fun with my code!
 */

/**
 * IME Lib Main
 */

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
private external fun setImeConversionMode(mode: Int)

val languages: List<String> by lazy(::getAvailableLanguages)
fun getAvailableLanguages(): List<String> {
    if (isWin) return getLanguages().toList()
    else throw UnsupportedOperationException("Only usable in Win")
}

fun getCurrentLanguage(): String {
    if (isWin) return getLanguage()
    else throw UnsupportedOperationException("Only usable in Win")
}

fun setCurrentLanguage(lang: String): Boolean {
    if (isWin) return setLanguage(lang)
    else throw UnsupportedOperationException("Only usable in Win")
}

@Deprecated("Currently not usable")
fun setImeConversionMode(mode: ImeConversionMode) {
    if (isWin) setImeConversionMode(mode.value)
    else throw UnsupportedOperationException("Only usable in Win")
}
