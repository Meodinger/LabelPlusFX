package info.meodinger.lpfx.options

import info.meodinger.lpfx.ViewMode
import info.meodinger.lpfx.getViewMode
import info.meodinger.lpfx.type.CProperty

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.options
 */
object Settings : AbstractProperties() {

    const val DefaultColorList = "DefaultColorList"
    const val DefaultGroupList = "DefaultGroupList"
    const val IsCreateOnNewTrans = "isCreateOnLoad"
    const val ViewModePreference = "ViewModePreference"

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
                ),
                CProperty(
                    IsCreateOnNewTrans,
                    true, true
                ),
                CProperty(
                    ViewModePreference,
                    ViewMode.GroupMode, ViewMode.IndexMode // Input, Label
                )
            )
        )
    }

    override fun load() = load(Options.settings, this)
    override fun save() = save(Options.settings, this)
    override fun check() {
        val colorList = this[DefaultColorList].asStringList()
        for (color in colorList) if (color.length != 6) throw IllegalStateException("exception.illegal_state.color_hex_invalid")

        val nameList = this[DefaultGroupList].asStringList()
        for (name in nameList) if (name.contains(Regex("s+"))) throw IllegalStateException("exception.illegal_state.name_has_whitespace")
        if (nameList.size > colorList.size) throw IllegalStateException("exception.illegal_state.name_more_than_color")

        val isCreateList = this[IsCreateOnNewTrans].asBooleanList()
        if (isCreateList.size != nameList.size) throw IllegalStateException("exception.illegal_state.isCreate_not_equal_to_name")

        val viewModePreferenceList = this[ViewModePreference].asStringList()
        for (preference in viewModePreferenceList) getViewMode(preference)
    }
}