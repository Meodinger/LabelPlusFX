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
private external fun setImeConversionMode(hWnd: Long, cMode: Int, sMode: Int): Boolean

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
fun setImeConversionMode(hWnd: Long, sMode: ImeSentenceMode, vararg cModes: ImeConversionMode): Boolean {
    if (!enableJNI) return false

    var cmode = 0
    for (conversions in cModes) cmode = cmode or conversions.value

    if (isWin) return setImeConversionMode(hWnd, cmode, sMode.value)
    else throw UnsupportedOperationException("Only usable in Win")
}
