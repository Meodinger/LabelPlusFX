package info.meodinger.lpfx.options

import java.io.IOException

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.options
 */
object Preference : AbstractProperties() {

    const val MAIN_DIVIDER = "MainDivider"
    const val RIGHT_DIVIDER = "RightDivider"

    override val default = listOf(
        CProperty(MAIN_DIVIDER, 0.63),
        CProperty(RIGHT_DIVIDER, 0.6)
    )

    init {
        this.properties.addAll(listOf(
            CProperty(MAIN_DIVIDER),
            CProperty(RIGHT_DIVIDER)
        ))
    }

    @Throws(IOException::class, CPropertyException::class)
    override fun load() = load(Options.preference, this)
    @Throws(IOException::class)
    override fun save() = save(Options.preference, this)
    @Throws(CPropertyException::class)
    override fun check() {
        super.check()

        val mainDivider = this[MAIN_DIVIDER].asDouble()
        if (mainDivider < 0 || mainDivider > 1)
            throw CPropertyException.propertyValueInvalid(MAIN_DIVIDER, mainDivider)

        val rightDivider = this[RIGHT_DIVIDER].asDouble()
        if (rightDivider < 0 || rightDivider > 1)
            throw CPropertyException.propertyValueInvalid(RIGHT_DIVIDER, rightDivider)
    }
}