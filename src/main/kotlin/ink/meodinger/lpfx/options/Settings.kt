package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.ViewMode
import ink.meodinger.lpfx.options.CProperty.CPropertyException

import java.io.IOException


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: ink.meodinger.lpfx.options
 */

/**
 * The settings that user set through CSettingsDialog
 */
object Settings : AbstractProperties() {

    const val DefaultGroupNameList = "DefaultGroupNameList"
    const val DefaultGroupColorHexList = "DefaultGroupColorList"
    const val IsGroupCreateOnNewTrans = "isGroupCreateOnNew"
    const val ScaleOnNewPicture = "ScaleOnNewPicture"    // 0 - 100%, 1 - Fit, 2 - Last
    const val ViewModePreference = "ViewModePreference"  // Input, Label
    const val LogLevelPreference = "LogLevelPreference"
    const val LabelRadius = "LabelRadius"
    const val LabelAlpha = "LabelAlpha"
    const val LigatureRules = "LigatureRules"

    override val default = listOf(
        CProperty(DefaultGroupNameList, "\u6846\u5185", "\u6846\u5916"),
        CProperty(DefaultGroupColorHexList, "FF0000", "0000FF"),
        CProperty(IsGroupCreateOnNewTrans, true, true),
        CProperty(ScaleOnNewPicture, 0),
        CProperty(ViewModePreference, ViewMode.GroupMode, ViewMode.IndexMode), // Input, Label
        CProperty(LogLevelPreference, Logger.LogType.INFO),
        CProperty(LabelRadius, 24.0),
        CProperty(LabelAlpha, "80"),
        CProperty(LigatureRules,
            "(" to "\u300c", ")" to "\u300d", "\uff08" to "\u300e", "\uff09" to "\u300f",
            "star" to "\u2b50","square" to "\u2662", "heart" to "\u2661", "music" to "\u266a",
            "*" to "\u203b", "cc" to "\u25ce",
        ),
    )

    init {
        this.properties.addAll(listOf(
            CProperty(DefaultGroupNameList),
            CProperty(DefaultGroupColorHexList),
            CProperty(IsGroupCreateOnNewTrans),
            CProperty(LigatureRules),
            CProperty(ScaleOnNewPicture),
            CProperty(ViewModePreference),
            CProperty(LogLevelPreference),
            CProperty(LabelRadius),
            CProperty(LabelAlpha)
        ))
    }

    @Throws(IOException::class, CPropertyException::class)
    override fun load() = load(Options.settings, this)
    @Throws(IOException::class)
    override fun save() = save(Options.settings, this, mapOf(
        DefaultGroupNameList to "Below three properties should have the same length",
        LigatureRules to "DO NOT USE `,` HERE\nMaybe cause problems",
        ScaleOnNewPicture to "0 - 100%, 1 - Fit, 2 - Last",
        ViewModePreference to "Input, Label",
        LogLevelPreference to "DEBUG, INFO, WARNING, ERROR, FATAL",
        LabelRadius to "Radius = 8.00 -> 48.00\nAlpha = 0x00 -> 0xFF"
    ))
    @Throws(CPropertyException::class)
    override fun check() {
        super.check()

        val groupNameList = this[DefaultGroupNameList].asStringList()
        for (name in groupNameList) if (name.contains(Regex("s+")))
            throw CPropertyException.propertyElementInvalid(DefaultGroupNameList, name)

        val groupColorList = this[DefaultGroupColorHexList].asStringList()
        for (color in groupColorList) if (color.length != 6)
            throw CPropertyException.propertyElementInvalid(DefaultGroupColorHexList, color)
        if (groupColorList.size != groupNameList.size)
            throw CPropertyException.propertyListSizeInvalid(DefaultGroupColorHexList, groupColorList.size)

        val isGroupCreateList = this[IsGroupCreateOnNewTrans].asBooleanList()
        if (isGroupCreateList.size != groupNameList.size)
            throw CPropertyException.propertyListSizeInvalid(IsGroupCreateOnNewTrans, isGroupCreateList.size)

        val scaleOnNewPicture = this[ScaleOnNewPicture].asInteger()
        if (scaleOnNewPicture < 0 || scaleOnNewPicture > 2)
            throw CPropertyException.propertyValueInvalid(ScaleOnNewPicture, scaleOnNewPicture)

        val viewModePreferenceList = this[ViewModePreference].asStringList()
        for (preference in viewModePreferenceList) try {
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
        if (labelRadius < 0)
            throw CPropertyException.propertyValueInvalid(LabelRadius, labelRadius)

        val labelAlpha = this[LabelAlpha].asInteger(16)
        if (labelAlpha < 0 || labelAlpha > 255)
            throw CPropertyException.propertyValueInvalid(LabelAlpha, labelAlpha)

        val ligatureRules = this[LigatureRules].asPairList()
        for (pair in ligatureRules) if (pair.first.isBlank())
            throw CPropertyException.propertyElementInvalid(LigatureRules, pair)
    }
}