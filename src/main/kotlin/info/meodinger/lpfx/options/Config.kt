package info.meodinger.lpfx.options

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.options
 */
object Config : AbstractProperties() {

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

    override fun load() = load(Options.config, this)
    override fun save() = save(Options.config, this)
    override fun check() {
        super.check()

        val mainDivider = this[MAIN_DIVIDER].asDouble()
        if (mainDivider < 0 || mainDivider > 1) throw IllegalStateException("exception.illegal_state.main_divider_invalid")

        val rightDivider = this[RIGHT_DIVIDER].asDouble()
        if (rightDivider < 0 || rightDivider > 1) throw IllegalStateException("exception.illegal_state.right_divider_invalid")
    }
}