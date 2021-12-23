package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.ViewMode

import java.io.IOException


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * The settings that user set through CSettingsDialog
 */
object Settings : AbstractProperties() {

    // ----- Constants ----- //

    const val NEW_PIC_SCALE_100 = 0
    const val NEW_PIC_SCALE_FIT = 1
    const val NEW_PIC_SCALE_LAST = 2

    // ----- Property Names ----- //

    const val DefaultGroupNameList = "DefaultGroupNameList"
    const val DefaultGroupColorHexList = "DefaultGroupColorList"
    const val IsGroupCreateOnNewTrans = "isGroupCreateOnNew"
    const val ScaleOnNewPicture = "ScaleOnNewPicture"    // 0 - 100%, 1 - Fit, 2 - Last
    const val ViewModePreference = "ViewModePreference"  // Input, Label
    const val LogLevelPreference = "LogLevelPreference"
    const val LabelRadius = "LabelRadius"
    const val LabelAlpha = "LabelAlpha"
    const val LigatureRules = "LigatureRules"

    // ----- Default ----- //

    override val default = listOf(
        CProperty(DefaultGroupNameList, "\u6846\u5185", "\u6846\u5916"),
        CProperty(DefaultGroupColorHexList, "FF0000", "0000FF"),
        CProperty(IsGroupCreateOnNewTrans, true, true),
        CProperty(ScaleOnNewPicture, NEW_PIC_SCALE_100),
        CProperty(ViewModePreference, ViewMode.IndexMode, ViewMode.GroupMode), // Input, Label
        CProperty(LogLevelPreference, Logger.LogType.INFO),
        CProperty(LabelRadius, 24.0),
        CProperty(LabelAlpha, "80"),
        CProperty(LigatureRules,
            "(" to "\u300c",
            ")" to "\u300d",
            "\uff08" to "\u300e",
            "\uff09" to "\u300f",
            "star" to "\u2b50",
            "square" to "\u2662",
            "heart" to "\u2661",
            "music" to "\u266a",
            "cc" to "\u25ce",
            "*" to "\u203b",
        ),
    ).toPropertiesMap()

    init { useDefault() }

    @Throws(IOException::class)
    override fun load() = load(Options.settings, this)

    @Throws(IOException::class)
    override fun save() = save(Options.settings, this, mapOf(
        DefaultGroupNameList to "Below three properties should have the same length",
        LigatureRules        to "DO NOT USE `,` HERE\nMaybe cause problems",
        ScaleOnNewPicture    to "0 - 100%, 1 - Fit, 2 - Last",
        ViewModePreference   to "Input, Label",
        LogLevelPreference   to "DEBUG, INFO, WARNING, ERROR, FATAL",
        LabelRadius          to "Radius = 8.00 -> 48.00\nAlpha = 0x00 -> 0xFF"
    ))

    override fun checkAndFix(): Boolean {
        var fixed = false

        var groupInvalid = false
        run checkGroup@ {
            val groupNameList = this[DefaultGroupNameList].asStringList()
            val groupColorHexList = this[DefaultGroupColorHexList].asStringList()
            val isGroupCreateList = this[IsGroupCreateOnNewTrans].asBooleanList()

            if (groupNameList.size != groupColorHexList.size || groupNameList.size != isGroupCreateList.size) {
                groupInvalid = true
                return@checkGroup
            }
            for (hex in groupColorHexList) if (hex.length != 6) {
                groupInvalid = true
                return@checkGroup
            }
        }
        if (groupInvalid) {
            this[DefaultGroupNameList] = default[DefaultGroupNameList]!!
            this[DefaultGroupColorHexList] = default[DefaultGroupColorHexList]!!
            this[IsGroupCreateOnNewTrans] = default[IsGroupCreateOnNewTrans]!!
            fixed = true
        }

        val scaleOnNewPicture = this[ScaleOnNewPicture].asInteger()
        if (scaleOnNewPicture < 0 || scaleOnNewPicture > 2) {
            this[ScaleOnNewPicture] = default[ScaleOnNewPicture]!!
            fixed = true
        }

        val viewModePreferenceList = this[ViewModePreference].asStringList()
        for (preference in viewModePreferenceList) try {
            ViewMode.getViewMode(preference)
        } catch (e: Exception) {
            this[ViewModePreference] = default[ViewModePreference]!!
            fixed = true
            break
        }

        val logLevel = this[LogLevelPreference].asString()
        try {
            Logger.LogType.getType(logLevel)
        } catch (e: Exception) {
            this[LogLevelPreference] = default[LogLevelPreference]!!
            fixed = true
        }

        val labelRadius = this[LabelRadius].asDouble()
        if (labelRadius < 8 || labelRadius > 48) {
            this[LabelRadius] = default[LabelRadius]!!
            fixed = true
        }

        val labelAlpha = this[LabelAlpha].asInteger(16)
        if (labelAlpha < 0 || labelAlpha > 255) {
            this[LabelAlpha] = default[LabelAlpha]!!
            fixed = true
        }

        val ligatureRules = this[LigatureRules].asPairList()
        for (pair in ligatureRules) if (pair.first.contains(Regex("[|, ]"))) {
            this[LigatureRules] = default[LigatureRules]!!
            fixed = true
        }

        return fixed
    }

}
