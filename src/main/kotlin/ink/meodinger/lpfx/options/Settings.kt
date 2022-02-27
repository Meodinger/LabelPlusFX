package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.ViewMode
import ink.meodinger.lpfx.component.CLabelPane
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue

import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
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

    const val VARIABLE_FILENAME = "%FILE%"
    const val VARIABLE_DIRNAME  = "%DIR%"
    const val VARIABLE_PROJECT  = "%PROJECT%"

    // ----- Property Names ----- //

    const val DefaultGroupNameList     = "DefaultGroupNameList"
    const val DefaultGroupColorHexList = "DefaultGroupColorList"
    const val IsGroupCreateOnNewTrans  = "IsGroupCreateOnNew"
    const val ScaleOnNewPictureOrdinal = "ScaleOnNewPictureOrdinal" // 0 - default, 1 - 100%, 2 - Fit, 3 - Last
    const val ViewModeOrdinals         = "ViewModeOrdinals"         // Input, Label
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
        CProperty(ScaleOnNewPictureOrdinal, CLabelPane.NewPictureScale.DEFAULT.ordinal),
        CProperty(ViewModeOrdinals, ViewMode.IndexMode.ordinal, ViewMode.GroupMode.ordinal),
        CProperty(LogLevelOrdinal, Logger.LogLevel.INFO.ordinal),
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

    private val defaultGroupNameListProperty: ListProperty<String> = SimpleListProperty()
    fun defaultGroupNameListProperty(): ListProperty<String> = defaultGroupNameListProperty
    var defaultGroupNameList: ObservableList<String> by defaultGroupNameListProperty

    private val defaultGroupColorHexListProperty: ListProperty<String> = SimpleListProperty()
    fun defaultGroupColorHexListProperty(): ListProperty<String> = defaultGroupColorHexListProperty
    var defaultGroupColorHexList: ObservableList<String> by defaultGroupNameListProperty

    private val isGroupCreateOnNewTransListProperty: ListProperty<Boolean> = SimpleListProperty()
    fun isGroupCreateOnNewTransListProperty(): ListProperty<Boolean> = isGroupCreateOnNewTransListProperty
    var isGroupCreateOnNewTransList: ObservableList<Boolean> by isGroupCreateOnNewTransListProperty

    private val scaleOnNewPictureProperty: ObjectProperty<CLabelPane.NewPictureScale> = SimpleObjectProperty()
    fun newPictureScaleProperty(): ObjectProperty<CLabelPane.NewPictureScale> = scaleOnNewPictureProperty
    var newPictureScalePicture: CLabelPane.NewPictureScale by scaleOnNewPictureProperty

    private val viewModesProperty: ListProperty<ViewMode> = SimpleListProperty()
    fun viewModesProperty(): ListProperty<ViewMode> = viewModesProperty
    var viewModes: ObservableList<ViewMode> by viewModesProperty

    private val logLevelProperty: ObjectProperty<Logger.LogLevel> = SimpleObjectProperty()
    fun logLevelProperty(): ObjectProperty<Logger.LogLevel> = logLevelProperty
    var logLevel: Logger.LogLevel by logLevelProperty

    private val labelRadiusProperty: DoubleProperty = SimpleDoubleProperty()
    fun labelRadiusProperty(): DoubleProperty = labelRadiusProperty
    var labelRadius: Double by labelRadiusProperty

    private val labelAlphaProperty: StringProperty = SimpleStringProperty()
    fun labelAlphaProperty(): StringProperty = labelAlphaProperty
    var labelAlpha: String by labelAlphaProperty

    private val ligatureRulesProperty: ListProperty<Pair<String, String>> = SimpleListProperty()
    fun ligatureRulesProperty(): ListProperty<Pair<String, String>> = ligatureRulesProperty
    var ligatureRules: ObservableList<Pair<String, String>> by ligatureRulesProperty

    private val instantTranslateProperty: BooleanProperty = SimpleBooleanProperty()
    fun instantTranslateProperty(): BooleanProperty = instantTranslateProperty
    var instantTranslate: Boolean by instantTranslateProperty

    private val useMeoFileAsDefaultProperty: BooleanProperty = SimpleBooleanProperty()
    fun useMeoFileAsDefaultProperty(): BooleanProperty = useMeoFileAsDefaultProperty
    var useMeoFileAsDefault: Boolean by useMeoFileAsDefaultProperty

    private val useExportNameTemplateProperty: BooleanProperty = SimpleBooleanProperty()
    fun useExportNameTemplateProperty(): BooleanProperty = useExportNameTemplateProperty
    var useExportNameTemplate: Boolean by useExportNameTemplateProperty

    private val exportNameTemplateProperty: StringProperty = SimpleStringProperty()
    fun exportNameTemplateProperty(): StringProperty = exportNameTemplateProperty
    var exportNameTemplate: String by exportNameTemplateProperty

    init { useDefault() }

    @Throws(IOException::class)
    override fun load() {
        load(Options.settings, this)

        defaultGroupNameList = FXCollections.observableList(this[DefaultGroupNameList].asStringList())
        defaultGroupColorHexList = FXCollections.observableList(this[DefaultGroupColorHexList].asStringList())
        isGroupCreateOnNewTransList = FXCollections.observableList(this[IsGroupCreateOnNewTrans].asBooleanList())
        newPictureScalePicture = CLabelPane.NewPictureScale.values()[this[ScaleOnNewPictureOrdinal].asInteger()]
        viewModes = FXCollections.observableList(this[ViewModeOrdinals].asIntegerList().map { ViewMode.values()[it] })
        logLevel = Logger.LogLevel.values()[this[LogLevelOrdinal].asInteger()]
        labelRadius = this[LabelRadius].asDouble()
        labelAlpha = this[LabelAlpha].asString()
        ligatureRules = FXCollections.observableList(this[LigatureRules].asPairList())
        instantTranslate = this[InstantTranslate].asBoolean()
        useMeoFileAsDefault = this[UseMeoFileAsDefault].asBoolean()
        useExportNameTemplate = this[UseExportNameTemplate].asBoolean()
        exportNameTemplate = this[ExportNameTemplate].asString()
    }

    @Throws(IOException::class)
    override fun save() {
        this[DefaultGroupNameList].set(defaultGroupNameList)
        this[DefaultGroupColorHexList].set(defaultGroupColorHexList)
        this[IsGroupCreateOnNewTrans].set(isGroupCreateOnNewTransList)
        this[ScaleOnNewPictureOrdinal].set(newPictureScalePicture.ordinal)
        this[ViewModeOrdinals].set(viewModes.map(Enum<*>::ordinal))
        this[LogLevelOrdinal].set(logLevel.ordinal)
        this[LabelRadius].set(labelRadius)
        this[LabelAlpha].set(labelAlpha)
        this[LigatureRules].set(ligatureRules)
        this[InstantTranslate].set(instantTranslate)
        this[UseMeoFileAsDefault].set(useMeoFileAsDefault)
        this[UseExportNameTemplate].set(useExportNameTemplate)
        this[ExportNameTemplate].set(exportNameTemplate)

        save(Options.settings, this)
    }

}
