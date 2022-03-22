@file:JvmName("IMEMain")

package ink.meodinger.lpfx.util.ime

import ink.meodinger.lpfx.util.platform.isWin
import javafx.application.Platform
import javafx.scene.Node

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
 * Set current language
 */
private external fun setLanguage(lang: String)

/**
 * Set IME Conversion Mode
 */
private external fun setImeConversionMode(mode: Int)


fun getAvailableLanguages(): List<String> {
    if (isWin) return getLanguages().toList()
    else throw UnsupportedOperationException("Only usable in Win")
}

fun setCurrentLanguage(langTag: LangTag) {
    if (isWin) setLanguage(langTag.name)
    else throw UnsupportedOperationException("Only usable in Win")
}

@Deprecated("Currently not usable")
fun setImeConversionMode(mode: ImeConversionMode) {
    if (isWin) setImeConversionMode(mode.value)
    else throw UnsupportedOperationException("Only usable in Win")
}

/*
 * TODO
 *
 * fun setLanguage(preferred: LangTag, vararg alternatives: LangTag? = emptyArray())
 * Set Language to preferred language, or set to one of available alternative language.
 * If alternatives leave as default (empty array), will use default language map to find alternatives.
 * If alternatives set to null, will set to preferred lang or doesn't change current lang.
 *
 */
