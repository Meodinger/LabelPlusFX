package info.meodinger.lpfx.options

import info.meodinger.lpfx.type.CProperty

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.options
 */
object Settings : AbstractProperties() {

    const val DefaultColorList = "DefaultColorList"
    const val DefaultGroupList = "DefaultGroupList"

    init {
        this.properties.addAll(
            listOf(
                CProperty(
                    DefaultColorList,
                    "FF0000", "0000FF", "008000",
                    "1E90FF", "FFD700", "FF00FF",
                    "A0522D", "FF4500", "9400D3"
                ),
                CProperty(
                    DefaultGroupList,
                    "框外", "框内"
                )
            )
        )
    }

    override fun load() = load(Options.settings, this)
    override fun save() = save(Options.settings, this)
}