package info.meodinger.lpfx.options

import info.meodinger.lpfx.ViewMode

import java.io.IOException


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.options
 */

/**
 * The settings that user set through CSettingsDialog
 */
object Settings : AbstractProperties() {

    const val DefaultGroupNameList = "DefaultGroupNameList"
    const val DefaultGroupColorList = "DefaultGroupColorList"
    const val IsGroupCreateOnNewTrans = "isGroupCreateOnNew"
    const val ViewModePreference = "ViewModePreference"
    const val LogLevelPreference = "LogLevelPreference"
    const val LabelRadius = "LabelRadius"
    const val LabelAlpha = "LabelAlpha"

    override val default = listOf(
        CProperty(DefaultGroupNameList, "框内", "框内外"),
        CProperty(DefaultGroupColorList, "FF0000", "0000FF"),
        CProperty(IsGroupCreateOnNewTrans, true, true),
        CProperty(ViewModePreference, ViewMode.GroupMode, ViewMode.IndexMode), // Input, Label
        CProperty(LogLevelPreference, Logger.LogType.INFO),
        CProperty(LabelRadius, 24.0),
        CProperty(LabelAlpha, "80")
    )

    init {
        this.properties.addAll(listOf(
            CProperty(DefaultGroupNameList),
            CProperty(DefaultGroupColorList),
            CProperty(IsGroupCreateOnNewTrans),
            CProperty(ViewModePreference),
            CProperty(LogLevelPreference),
            CProperty(LabelRadius),
            CProperty(LabelAlpha)
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
                ViewMode.getMode(preference)
            } catch (e: Exception) {
                throw CPropertyException.propertyValueInvalid(ViewModePreference, preference).initCause(e)
            }

        val logLevel = this[LogLevelPreference].asString()
        try {
            Logger.LogType.getType(logLevel)
        } catch (e: Exception) {
            throw CPropertyException.propertyValueInvalid(LogLevelPreference, logLevel).initCause(e)
        }

        val labelRadius = this[LabelRadius].asDouble()
        if (labelRadius < 0) throw CPropertyException.propertyValueInvalid(LabelRadius, labelRadius)

        val labelAlpha = this[LabelAlpha].asInteger(16)
        if (labelAlpha < 0 || labelAlpha > 255) throw CPropertyException.propertyValueInvalid(LabelAlpha, labelAlpha)

    }
}