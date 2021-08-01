package info.meodinger.lpfx.options

import info.meodinger.lpfx.type.CProperty

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.options
 */
object Config : AbstractProperties() {

    const val MAIN_DIVIDER = "MainDivider"
    const val RIGHT_DIVIDER = "RightDivider"

    init {
        this.properties.addAll(
            listOf(
                CProperty("MainDivider", 0.63),
                CProperty("RightDivider", 0.6)
            )
        )
    }

    override fun load() = load(Options.config, this)
    override fun save() = save(Options.config, this)
}