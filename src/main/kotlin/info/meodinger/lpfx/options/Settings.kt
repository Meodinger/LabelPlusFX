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

    const val DefaultGroupNameList = "DefaultGroupNameList"
    const val DefaultGroupColorList = "DefaultGroupColorList"
    const val IsGroupCreateOnNewTrans = "isGroupCreateOnNew"
    const val ViewModePreference = "ViewModePreference"

    override val default = listOf(
        CProperty(DefaultGroupNameList, "框内", "框内外"),
        CProperty(DefaultGroupColorList, "FF0000", "0000FF"),
        CProperty(IsGroupCreateOnNewTrans, true, true),
        CProperty(ViewModePreference, ViewMode.GroupMode, ViewMode.IndexMode) // Input, Label
    )

    init {
        this.properties.addAll(listOf(
            CProperty(DefaultGroupNameList),
            CProperty(DefaultGroupColorList),
            CProperty(IsGroupCreateOnNewTrans),
            CProperty(ViewModePreference)
        ))
    }

    override fun load() = load(Options.settings, this)
    override fun save() = save(Options.settings, this)
    override fun check() {
        super.check()

        val groupNameList = this[DefaultGroupNameList].asStringList()
        for (name in groupNameList) if (name.contains(Regex("s+"))) throw IllegalStateException("exception.illegal_state.name_has_whitespace")

        val groupColorList = this[DefaultGroupColorList].asStringList()
        for (color in groupColorList) if (color.length != 6) throw IllegalStateException("exception.illegal_state.color_hex_invalid")
        if (groupColorList.size != groupNameList.size) throw IllegalStateException("exception.illegal_state.name_color_not_equal")

        val isGroupCreateList = this[IsGroupCreateOnNewTrans].asBooleanList()
        if (isGroupCreateList.size != groupNameList.size) throw IllegalStateException("exception.illegal_state.name_isCreate_not_equal")

        val viewModePreferenceList = this[ViewModePreference].asStringList()
        for (preference in viewModePreferenceList) getViewMode(preference)
    }
}