package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.ViewMode
import ink.meodinger.lpfx.component.CLabelPane
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get

import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.io.IOException
import kotlin.math.roundToInt


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * The settings that user set through CSettingsDialog
 */
object Settings : AbstractProperties("Settings") {

    // ----- Constants ----- //

    const val VARIABLE_FILENAME = "%FILE%"
    const val VARIABLE_DIRNAME  = "%DIR%"
    const val VARIABLE_PROJECT  = "%PROJECT%"

    // ----- Property Names ----- //

    const val DefaultGroupNameList     = "DefaultGroupNameList"
    const val DefaultGroupColorHexList = "DefaultGroupColorList"
    const val IsGroupCreateOnNewTrans  = "IsGroupCreateOnNew"
    const val NewPictureScale          = "ScaleOnNewPicture"
    const val ViewModes                = "ViewMode"
    const val LogLevel                 = "LogLevel"
    const val LabelRadius              = "LabelRadius"
    const val LabelColorOpacity        = "LabelColorOpacity"
    const val LabelTextOpaque          = "LabelTextOpaque"
    const val LigatureRules            = "LigatureRules"
    const val InstantTranslate         = "InstantTranslate"
    const val UseMeoFileAsDefault      = "UseMeoFileAsDefault"
    const val UseExportNameTemplate    = "UseExportNameTemplate"
    const val ExportNameTemplate       = "ExportNameTemplate"
    const val AutoCheckUpdate          = "AutoCheckUpdate"

    // ----- Default ----- //

    override val default = listOf(
        CProperty(DefaultGroupNameList, I18N["settings.group.default.1"], I18N["settings.group.default.2"]),
        CProperty(DefaultGroupColorHexList, "FF0000", "0000FF"),
        CProperty(IsGroupCreateOnNewTrans, true, true),
        CProperty(NewPictureScale, CLabelPane.NewPictureScale.DEFAULT.ordinal),
        CProperty(ViewModes, ViewMode.IndexMode.ordinal, ViewMode.GroupMode.ordinal),
        CProperty(LogLevel, Logger.LogLevel.INFO.ordinal),
        CProperty(LabelRadius, 24.0),
        CProperty(LabelColorOpacity, "80"),
        CProperty(LabelTextOpaque, false),
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
        CProperty(UseMeoFileAsDefault, false),
        CProperty(UseExportNameTemplate, false),
        CProperty(ExportNameTemplate, I18N["settings.other.template.default"]),
        CProperty(AutoCheckUpdate, true),
    )

    private val defaultGroupNameListProperty: ListProperty<String> = SimpleListProperty()
    fun defaultGroupNameListProperty(): ListProperty<String> = defaultGroupNameListProperty
    var defaultGroupNameList: ObservableList<String> by defaultGroupNameListProperty

    private val defaultGroupColorHexListProperty: ListProperty<String> = SimpleListProperty()
    fun defaultGroupColorHexListProperty(): ListProperty<String> = defaultGroupColorHexListProperty
    var defaultGroupColorHexList: ObservableList<String> by defaultGroupColorHexListProperty

    private val isGroupCreateOnNewTransListProperty: ListProperty<Boolean> = SimpleListProperty()
    fun isGroupCreateOnNewTransListProperty(): ListProperty<Boolean> = isGroupCreateOnNewTransListProperty
    var isGroupCreateOnNewTransList: ObservableList<Boolean> by isGroupCreateOnNewTransListProperty

    private val newPictureScaleProperty: ObjectProperty<CLabelPane.NewPictureScale> = SimpleObjectProperty()
    fun newPictureScaleProperty(): ObjectProperty<CLabelPane.NewPictureScale> = newPictureScaleProperty
    var newPictureScalePicture: CLabelPane.NewPictureScale by newPictureScaleProperty

    private val viewModesProperty: ListProperty<ViewMode> = SimpleListProperty()
    fun viewModesProperty(): ListProperty<ViewMode> = viewModesProperty
    var viewModes: ObservableList<ViewMode> by viewModesProperty

    private val logLevelProperty: ObjectProperty<Logger.LogLevel> = SimpleObjectProperty()
    fun logLevelProperty(): ObjectProperty<Logger.LogLevel> = logLevelProperty
    var logLevel: Logger.LogLevel by logLevelProperty

    private val labelRadiusProperty: DoubleProperty = SimpleDoubleProperty()
    fun labelRadiusProperty(): DoubleProperty = labelRadiusProperty
    var labelRadius: Double by labelRadiusProperty

    private val labelColorOpacityProperty: DoubleProperty = SimpleDoubleProperty()
    fun labelColorOpacityProperty(): DoubleProperty = labelColorOpacityProperty
    var labelColorOpacity: Double by labelColorOpacityProperty

    private val labelTextOpaqueProperty: BooleanProperty = SimpleBooleanProperty()
    fun labelTextOpaqueProperty(): BooleanProperty = labelTextOpaqueProperty
    var labelTextOpaque: Boolean by labelTextOpaqueProperty

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

    private val autoCheckUpdateProperty: BooleanProperty = SimpleBooleanProperty()
    fun autoCheckUpdateProperty(): BooleanProperty = autoCheckUpdateProperty
    var autoCheckUpdate: Boolean by autoCheckUpdateProperty

    init { useDefault() }

    @Throws(IOException::class, NumberFormatException::class)
    override fun load() {
        load(Options.settings, this)

        defaultGroupNameList        = FXCollections.observableList(this[DefaultGroupNameList].asStringList())
        defaultGroupColorHexList    = FXCollections.observableList(this[DefaultGroupColorHexList].asStringList())
        isGroupCreateOnNewTransList = FXCollections.observableList(this[IsGroupCreateOnNewTrans].asBooleanList())
        newPictureScalePicture      = CLabelPane.NewPictureScale.values()[this[NewPictureScale].asInteger()]
        viewModes                   = FXCollections.observableList(this[ViewModes].asIntegerList().map(ViewMode.values()::get))
        logLevel                    = Logger.LogLevel.values()[this[LogLevel].asInteger()]
        labelRadius                 = this[LabelRadius].asDouble()
        labelColorOpacity           = this[LabelColorOpacity].asInteger(16) / 255.0
        labelTextOpaque             = this[LabelTextOpaque].asBoolean()
        ligatureRules               = FXCollections.observableList(this[LigatureRules].asPairList())
        instantTranslate            = this[InstantTranslate].asBoolean()
        useMeoFileAsDefault         = this[UseMeoFileAsDefault].asBoolean()
        useExportNameTemplate       = this[UseExportNameTemplate].asBoolean()
        exportNameTemplate          = this[ExportNameTemplate].asString()
        autoCheckUpdate             = this[AutoCheckUpdate].asBoolean()
    }

    @Throws(IOException::class)
    override fun save() {
        this[DefaultGroupNameList]    .set(defaultGroupNameList)
        this[DefaultGroupColorHexList].set(defaultGroupColorHexList)
        this[IsGroupCreateOnNewTrans] .set(isGroupCreateOnNewTransList)
        this[NewPictureScale]         .set(newPictureScalePicture.ordinal)
        this[ViewModes]               .set(viewModes.map(Enum<*>::ordinal))
        this[LogLevel]                .set(logLevel.ordinal)
        this[LabelRadius]             .set(labelRadius)
        this[LabelColorOpacity]       .set((labelColorOpacity * 255).roundToInt().toString(16).padStart(2, '0'))
        this[LabelTextOpaque]         .set(labelTextOpaque)
        this[LigatureRules]           .set(ligatureRules)
        this[InstantTranslate]        .set(instantTranslate)
        this[UseMeoFileAsDefault]     .set(useMeoFileAsDefault)
        this[UseExportNameTemplate]   .set(useExportNameTemplate)
        this[ExportNameTemplate]      .set(exportNameTemplate)
        this[AutoCheckUpdate]         .set(autoCheckUpdate)

        save(Options.settings, this)
    }

}
