package ink.meodinger.lpfx.options

import java.io.IOException


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * The preferences that user set while using
 */
object Preference : AbstractProperties() {

    const val MAIN_DIVIDER = "MainDivider"
    const val RIGHT_DIVIDER = "RightDivider"
    const val TEXTAREA_FONT_SIZE = "TextAreaFontSize"

    override val default = listOf(
        CProperty(MAIN_DIVIDER, 0.63),
        CProperty(RIGHT_DIVIDER, 0.6),
        CProperty(TEXTAREA_FONT_SIZE, 12)
    ).toPropertiesMap()

    init { useDefault() }

    @Throws(IOException::class)
    override fun load() = load(Options.preference, this)

    @Throws(IOException::class)
    override fun save() = save(Options.preference, this)

    override fun checkAndFix(): Boolean {
        var fixed = false

        val mainDivider = this[MAIN_DIVIDER].asDouble()
        if (mainDivider < 0 || mainDivider > 1) {
            this[MAIN_DIVIDER] = default[MAIN_DIVIDER]!!
            fixed = true
        }

        val rightDivider = this[RIGHT_DIVIDER].asDouble()
        if (rightDivider < 0 || rightDivider > 1) {
            this[RIGHT_DIVIDER] = default[RIGHT_DIVIDER]!!
            fixed = true
        }

        val textAreaFontSize = this[TEXTAREA_FONT_SIZE].asInteger()
        if (textAreaFontSize < 12 || textAreaFontSize > 64) {
            this[TEXTAREA_FONT_SIZE] = default[TEXTAREA_FONT_SIZE]!!
            fixed = true
        }

        return fixed
    }
}
