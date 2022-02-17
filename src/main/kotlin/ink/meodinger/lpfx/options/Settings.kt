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

    const val VARIABLE_FILENAME = "%FILE%"
    const val VARIABLE_DIRNAME  = "%DIR%"
    const val VARIABLE_PROJECT  = "%PROJECT%"

    // ----- Property Names ----- //

    const val DefaultGroupNameList     = "DefaultGroupNameList"
    const val DefaultGroupColorHexList = "DefaultGroupColorList"
    const val IsGroupCreateOnNewTrans  = "IsGroupCreateOnNew"
    const val ScaleOnNewPicture        = "ScaleOnNewPicture"    // 0 - 100%, 1 - Fit, 2 - Last
    const val ViewModeOrdinals         = "ViewModeOrdinals"     // Input, Label
    const val LogLevelOrdinal          = "LogLevelOrdinal"
    const val LabelRadius              = "LabelRadius"
    const val LabelAlpha               = "LabelAlpha"
    const val LigatureRules            = "LigatureRules"
    const val InstantTranslate         = "InstantTranslate"
    const val UseMeoFileAsDefault      = "UseMeoFileAsDefault"
    const val UseExportNameTemplate    = "UseExportNameTemplate"
    const val ExportNameTemplate       = "ExportNameTemplate"

    // ----- Default ----- //

    override val default = listOf(
        CProperty(DefaultGroupNameList, "框内", "框外"),
        CProperty(DefaultGroupColorHexList, "FF0000", "0000FF"),
        CProperty(IsGroupCreateOnNewTrans, true, true),
        CProperty(ScaleOnNewPicture, NEW_PIC_SCALE_100),
        CProperty(ViewModeOrdinals, ViewMode.IndexMode.ordinal, ViewMode.GroupMode.ordinal),
        CProperty(LogLevelOrdinal, Logger.LogType.INFO.ordinal),
        CProperty(LabelRadius, 24.0),
        CProperty(LabelAlpha, "80"),
        CProperty(LigatureRules,
            "("      to "「",
            ")"      to "」",
            "（"     to "『",
            "）"     to "』",
            "star"   to "⭐",
            "square" to "♢",
            "heart"  to "♡",
            "music"  to "♪",
            "cc"     to "◎",
            "*"      to "※",
        ),
        CProperty(InstantTranslate, false),
        CProperty(UseMeoFileAsDefault, true),
        CProperty(UseExportNameTemplate, false),
        CProperty(ExportNameTemplate, "%FILE% 翻译：XXX"),
    )

    init { useDefault() }

    @Throws(IOException::class)
    override fun load() = load(Options.settings, this)

    @Throws(IOException::class)
    override fun save() = save(Options.settings, this)

}
