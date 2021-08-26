package info.meodinger.lpfx.options

import info.meodinger.lpfx.ViewMode
import info.meodinger.lpfx.getViewMode

import java.io.IOException

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

    @Throws(IOException::class, CPropertyException::class)
    override fun load() = load(Options.settings, this)
    @Throws(IOException::class)
    override fun save() = save(Options.settings, this)
    @Throws(CPropertyException::class)
    override fun check() {
        super.check()

        val groupNameList = this[DefaultGroupNameList].asStringList()
        for (name in groupNameList) if (name.contains(Regex("s+")))
            throw CPropertyException.propertyElementInvalid(DefaultGroupNameList, name)

        val groupColorList = this[DefaultGroupColorList].asStringList()
        for (color in groupColorList) if (color.length != 6)
            throw CPropertyException.propertyElementInvalid(DefaultGroupColorList, color)
        if (groupColorList.size != groupNameList.size)
            throw CPropertyException.propertyListSizeInvalid(DefaultGroupColorList, groupColorList.size)

        val isGroupCreateList = this[IsGroupCreateOnNewTrans].asBooleanList()
        if (isGroupCreateList.size != groupNameList.size)
            throw CPropertyException.propertyListSizeInvalid(IsGroupCreateOnNewTrans, isGroupCreateList.size)

        val viewModePreferenceList = this[ViewModePreference].asStringList()
        for (preference in viewModePreferenceList)
            try {
                getViewMode(preference)
            } catch (e: Exception) {
                throw CPropertyException.propertyValueInvalid(ViewModePreference, preference).initCause(e)
            }

    }
}