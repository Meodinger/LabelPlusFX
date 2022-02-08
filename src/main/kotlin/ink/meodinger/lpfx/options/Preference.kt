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

    const val MAIN_DIVIDER       = "MainDivider"
    const val RIGHT_DIVIDER      = "RightDivider"
    const val TEXTAREA_FONT_SIZE = "TextAreaFontSize"
    const val LAST_UPDATE_NOTICE = "LastUpdateNotice"

    override val default = listOf(
        CProperty(MAIN_DIVIDER, 0.618),
        CProperty(RIGHT_DIVIDER, 0.618),
        CProperty(TEXTAREA_FONT_SIZE, 12),
        CProperty(LAST_UPDATE_NOTICE, 0)
    ).toPropertiesMap()

    init { useDefault() }

    @Throws(IOException::class)
    override fun load() = load(Options.preference, this)

    @Throws(IOException::class)
    override fun save() = save(Options.preference, this)

}
