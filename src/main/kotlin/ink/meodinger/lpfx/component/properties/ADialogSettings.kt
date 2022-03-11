package ink.meodinger.lpfx.component.properties

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.component.CLabel
import ink.meodinger.lpfx.component.CLabelPane
import ink.meodinger.lpfx.component.common.CColorPicker
import ink.meodinger.lpfx.component.common.CComboBox
import ink.meodinger.lpfx.component.common.CInputLabel
import ink.meodinger.lpfx.options.CProperty
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.util.color.isColorHex
import ink.meodinger.lpfx.util.color.toHexRGB
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.property.minus
import ink.meodinger.lpfx.util.property.onChange
import ink.meodinger.lpfx.util.property.onNew
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.SAMPLE_IMAGE
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.string.isMathematicalDecimal
import javafx.beans.binding.Bindings

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import javafx.util.Duration
import javafx.util.StringConverter
import kotlin.math.roundToInt


/**
 * Author: Meodinger
 * Date: 2021/8/25
 * Have fun with my code!
 */

/**
 * A Dialog Singleton for settings
 */
class ADialogSettings : AbstractPropertiesDialog() {

    companion object {
        private const val gRowShift = 1
        private const val rRowShift = 1
        private const val rIsFrom = "C_Is_From"
        private const val rRuleIndex = "C_Rule_Index"
    }

    private val gLabelHint = Label(I18N["settings.group.hint"])
    private val gLabelIsCreate = Label(I18N["settings.group.is_create_on_new"])
    private val gLabelName = Label(I18N["settings.group.name"])
    private val gLabelColor = Label(I18N["settings.group.color"])
    private val gGridPane = GridPane()

    private val rLabelHint = Label(I18N["settings.ligature.hint"])
    private val rLabelFrom = Label(I18N["settings.ligature.from"])
    private val rLabelTo = Label(I18N["settings.ligature.to"])
    private val rGridPane = GridPane()

    private val mComboInput = CComboBox<ViewMode>()
    private val mComboLabel = CComboBox<ViewMode>()
    private val mComboScale = CComboBox<CLabelPane.NewPictureScale>()

    private val lCLabel = CLabel(index = 8, color = Color.RED)
    private val lLabelPane = AnchorPane()
    private val lSliderRadius = Slider()
    private val lSliderAlpha = Slider()
    private val lLabelRadius = CInputLabel()
    private val lLabelAlpha = CInputLabel()

    private val xInstCheckBox = CheckBox(I18N["settings.other.inst_trans"])
    private val xUseMCheckBox = CheckBox(I18N["settings.other.meo_default"])
    private val xUseTCheckBox = CheckBox(I18N["settings.other.template_enable"])
    private val xTemplateField = TextField()

    init {
        title = I18N["settings.title"]
        dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
        withContent(TabPane()) {
            // DialogPane size depends on TabPane
            prefWidth = PANE_WIDTH
            prefHeight = PANE_HEIGHT
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

            add(I18N["settings.group.title"]) {
                withContent(BorderPane()) {
                    val stackPane = StackPane(gGridPane.apply {
                        alignment = Pos.TOP_CENTER
                        padding = Insets(COMMON_GAP)
                        vgap = COMMON_GAP
                        hgap = COMMON_GAP
                    })
                    val scrollPane = ScrollPane(stackPane)
                    stackPane.prefWidthProperty().bind(scrollPane.widthProperty() - COMMON_GAP)

                    center(scrollPane) { style = "-fx-background-color:transparent;" }
                    bottom(HBox()) {
                        alignment = Pos.CENTER_RIGHT
                        padding = Insets(COMMON_GAP, COMMON_GAP / 2, COMMON_GAP / 2, COMMON_GAP)
                        add(Button(I18N["settings.group.add"])) { does { createGroupRow() } }
                    }
                }
            }
            add(I18N["settings.ligature.title"]) {
                withContent(BorderPane()) {
                    val stackPane = StackPane(rGridPane.apply {
                        alignment = Pos.TOP_CENTER
                        padding = Insets(COMMON_GAP)
                        vgap = COMMON_GAP
                        hgap = COMMON_GAP
                    })
                    val scrollPane = ScrollPane(stackPane)
                    stackPane.prefWidthProperty().bind(scrollPane.widthProperty() - COMMON_GAP)

                    center(scrollPane) { style = "-fx-background-color:transparent;" }
                    bottom(HBox()) {
                        alignment = Pos.CENTER_RIGHT
                        padding = Insets(COMMON_GAP, COMMON_GAP / 2, COMMON_GAP / 2, COMMON_GAP)
                        add(Label(I18N["settings.ligature.sample"]))
                        add(HBox()) { boxHGrow = Priority.ALWAYS }
                        add(Button(I18N["settings.ligature.add"])) { does { createLigatureRow() } }
                    }
                }
            }
            add(I18N["settings.mode.title"]) {
                withContent(GridPane()) {
                    alignment = Pos.TOP_CENTER
                    padding = Insets(COMMON_GAP)
                    vgap = COMMON_GAP
                    hgap = COMMON_GAP

                    //   0         1
                    // 0 Input     | input | < > (ViewMode)
                    // 1 Label     | label | < > (ViewMode)
                    // 2
                    // 3 Scale on new picture
                    // 4 | selection | < >       (String)

                    val viewModeList = listOf(*ViewMode.values())
                    add(Label(I18N["mode.work.input"]), 0, 0)
                    add(mComboInput, 1, 0) {
                        items.setAll(viewModeList)
                        isWrapped = true
                    }
                    add(Label(I18N["mode.work.label"]), 0, 1)
                    add(mComboLabel, 1, 1) {
                        items.setAll(viewModeList)
                        isWrapped = true
                    }
                    add(HBox(), 0, 2)
                    add(Label(I18N["settings.mode.scale.label"]), 0, 3, 2, 1)
                    add(mComboScale, 0, 4, 2, 1) {
                        items.setAll(CLabelPane.NewPictureScale.values().toList())
                        isWrapped = true
                    }
                }
            }
            add(I18N["settings.label.title"]) {
                withContent(GridPane()) {
                    alignment = Pos.CENTER
                    padding = Insets(COMMON_GAP, COMMON_GAP, 0.0, COMMON_GAP)
                    vgap = COMMON_GAP
                    hgap = COMMON_GAP

                    // lGridPane.isGridLinesVisible = true
                    //   0         1           2
                    // 0 --------  Radius
                    // 1 |      |  ----O------ 24.0
                    // 2 |      |  Alpha
                    // 3 |      |  ------O---- 0x80
                    // 4 --------  *TEXT*
                    add(lLabelPane, 0, 0, 1, 5) {
                        val lLabelPaneBorderWidth = 2.0

                        border = Border(BorderStroke(
                            Color.DARKGRAY,
                            BorderStrokeStyle.SOLID,
                            CornerRadii(0.0),
                            BorderWidths(lLabelPaneBorderWidth)
                        ))
                        background = Background(BackgroundImage(
                            SAMPLE_IMAGE,
                            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                            BackgroundPosition.CENTER,
                            BackgroundSize.DEFAULT
                        ))

                        setPrefSize(320.0, 320.0)
                        add(lCLabel) {
                            radiusProperty().bind(lSliderRadius.valueProperty())
                            opacityProperty().bind(Bindings.createDoubleBinding(
                                { lSliderAlpha.value / 255 },
                                lSliderAlpha.valueProperty()
                            ))
                            // Draggable & drag-limitation
                            var shiftX = 0.0
                            var shiftY = 0.0
                            addEventHandler(MouseEvent.MOUSE_PRESSED) {
                                it.consume()

                                cursor = Cursor.MOVE

                                shiftX = anchorPaneLeft - it.sceneX
                                shiftY = anchorPaneTop - it.sceneY
                            }
                            addEventHandler(MouseEvent.MOUSE_DRAGGED) {
                                it.consume()

                                val newLayoutX = shiftX + it.sceneX
                                val newLayoutY = shiftY + it.sceneY

                                //  0--L-----    0 LR LR |
                                //  |  R         LR      |
                                //  |LR|-----    LR      |
                                //  |  |         --------|
                                val limitX = SAMPLE_IMAGE.width - prefWidth - 2 * lLabelPaneBorderWidth
                                val limitY = SAMPLE_IMAGE.height - prefHeight - 2 * lLabelPaneBorderWidth
                                if (newLayoutX < 0 || newLayoutX > limitX) return@addEventHandler
                                if (newLayoutY < 0 || newLayoutY > limitY) return@addEventHandler

                                anchorPaneLeft = newLayoutX
                                anchorPaneTop = newLayoutY
                            }
                            addEventHandler(MouseEvent.MOUSE_RELEASED) {
                                cursor = Cursor.HAND
                            }
                            radiusProperty().addListener(onChange {
                                val limitX = SAMPLE_IMAGE.width - prefWidth - 2 * lLabelPaneBorderWidth
                                val limitY = SAMPLE_IMAGE.height - prefHeight - 2 * lLabelPaneBorderWidth
                                if (anchorPaneLeft > limitX) anchorPaneLeft = limitX
                                if (anchorPaneTop > limitY) anchorPaneTop = limitY
                            })
                        }
                    }
                    add(Label(I18N["settings.label.helpText"]), 1, 4, 2, 1) {
                        isWrapText = true
                        textAlignment = TextAlignment.CENTER
                    }
                    add(Label(I18N["settings.label.radius"]), 1, 0)
                    add(lSliderRadius, 1, 1) {
                        prefWidth = 160.0

                        min = LABEL_RADIUS_MIN
                        max = LABEL_RADIUS_MAX
                        majorTickUnit = 8.0
                        minorTickCount = 3
                        blockIncrement = 2.0
                        isSnapToTicks = true
                        isShowTickMarks = true
                        isShowTickLabels = true
                        valueProperty().addListener(onNew<Number, Double> {
                            lLabelRadius.text = String.format("%05.2f", it)
                        })
                    }
                    add(lLabelRadius, 2, 1) {
                        textFormatter = TextFormatter { change ->
                            if (!change.isAdded) return@TextFormatter change

                            if (!change.text.isMathematicalDecimal())
                                return@TextFormatter change.apply { text = "" }
                            if (!change.controlNewText.isMathematicalDecimal())
                                return@TextFormatter change.apply { text = "" }

                            return@TextFormatter change
                        }
                        setOnChangeToLabel {
                            lSliderRadius.value = fieldText.padStart(5, '0').toDouble()

                            labelText = String.format("%05.2f", lSliderRadius.value)
                        }
                    }
                    add(Label(I18N["settings.label.alpha"]), 1, 2)
                    add(lSliderAlpha, 1, 3) {
                        prefWidth = 160.0

                        min = 0.0
                        max = 255.0  // Use 255 to have more precise values
                        majorTickUnit = 64.0
                        minorTickCount = 3
                        blockIncrement = 16.0
                        isShowTickMarks = true
                        isShowTickLabels = true
                        labelFormatter = object : StringConverter<Double>() {
                            override fun toString(double: Double): String = double.roundToInt().toString(16).padStart(2, '0')
                            override fun fromString(string: String): Double = string.toInt(16).toDouble()
                        }
                        valueProperty().addListener(onNew<Number, Double> {
                            val alphaPart = it.roundToInt().toString(16).padStart(2, '0')
                            lLabelAlpha.text = (if (lLabelAlpha.isEditing) "" else "0x") + alphaPart
                        })
                    }
                    add(lLabelAlpha, 2, 3) {
                        textFormatter = TextFormatter { change ->
                            if (!change.isAdded) return@TextFormatter change

                            if (change.text.uppercase().contains(Regex("[^0-9A-F]")))
                                return@TextFormatter change.apply { text = "" }
                            if (change.controlNewText.length > 2)
                                return@TextFormatter change.apply { text = "" }

                            return@TextFormatter change
                        }
                        setOnChangeToField {
                            fieldText = labelText.substring(2)
                        }
                        setOnChangeToLabel {
                            val alphaStr = fieldText.padStart(2, '0').uppercase()
                            lSliderAlpha.value = alphaStr.toInt(16).toDouble()

                            labelText = "0x$alphaStr"
                        }
                    }
                }
            }
            add(I18N["settings.other.title"]) {
                withContent(GridPane()) {
                    alignment = Pos.TOP_CENTER
                    padding = Insets(COMMON_GAP)
                    vgap = COMMON_GAP
                    hgap = COMMON_GAP

                    //   0        1
                    // 0 O InstantTranslate
                    // 1 O UseMeoFileAsDefault
                    // 2 O UseExportTemplate
                    // 3   |  template text  |

                    add(xInstCheckBox, 0, 0, 2, 1)
                    add(xUseMCheckBox, 0, 1, 2, 1)
                    add(xUseTCheckBox, 0, 2, 2, 1)
                    add(xTemplateField, 1, 3) {
                        disableProperty().bind(!xUseTCheckBox.selectedProperty())
                        textFormatter = TextFormatter<String> {
                            it.apply { text = text.replace(Regex("[:*?<>|/\"\\\\]"), "") }
                        }
                        tooltip = Tooltip(I18N["settings.other.template_hint"]).apply { showDelay = Duration(500.0) }
                    }
                }
            }
        }

        initProperties()
    }

    // ----- Group ----- //
    private fun initGroupTab() {
        gGridPane.children.clear()

        val nameList = Settings.defaultGroupNameList
        val colorList = Settings.defaultGroupColorHexList
        val createList = Settings.isGroupCreateOnNewTransList

        if (nameList.isEmpty()) {
            gGridPane.add(gLabelHint, 0, 0)
        } else {
            for (i in nameList.indices) createGroupRow(createList[i], nameList[i], colorList[i])
        }
    }
    private fun createGroupRow(createOnNew: Boolean = false, name: String = "", color: String = "") {
        val newRowIndex = if (gGridPane.rowCount == 0) 1 else gGridPane.rowCount

        if (gGridPane.children.size == 1 || gGridPane.rowCount == 0) { // Only hint or nothing
            gGridPane.children.clear()
            gGridPane.add(gLabelName, 0, 0)
            gGridPane.add(gLabelColor, 1, 0)
            gGridPane.add(gLabelIsCreate, 2, 0)
        }

        val groupId = newRowIndex - gRowShift
        val colorHex = if (color.isColorHex()) color else TransFile.Companion.LPTransFile.DEFAULT_COLOR_HEX_LIST[groupId % 9]

        val checkBox = CheckBox().apply { isSelected = createOnNew }
        val textField = TextField(name).apply { textFormatter = genGroupNameFormatter() }
        val colorPicker = CColorPicker(Color.web(colorHex))
        val button = Button(I18N["common.delete"]) does { removeGroupRow(GridPane.getRowIndex(this)) }

        checkBox.disableProperty().bind(textField.textProperty().isEmpty)
        textField.textProperty().addListener(onNew { if (it.isEmpty()) checkBox.isSelected = false })

        //   0       1             2        3
        // 0 Name    Color         isCreate
        // 1 _______ | 66CCFF  V |  X       Delete
        gGridPane.add(textField, 0, newRowIndex)
        gGridPane.add(colorPicker, 1, newRowIndex)
        gGridPane.add(checkBox, 2, newRowIndex)
        gGridPane.add(button, 3, newRowIndex)
    }
    private fun removeGroupRow(index: Int) {
        val toRemoveSet = HashSet<Node>()
        for (node in gGridPane.children) {
            val row = GridPane.getRowIndex(node) ?: 0
            if (row == index) toRemoveSet.add(node)
            if (row > index) GridPane.setRowIndex(node, row - 1)
        }
        gGridPane.children.removeAll(toRemoveSet)

        if (gGridPane.rowCount == gRowShift) {
            gGridPane.children.removeAll(gLabelIsCreate, gLabelName, gLabelColor)
            gGridPane.add(gLabelHint, 0, 0)
        }
    }

    // ----- Ligature ----- //
    private fun initLigatureTab() {
        rGridPane.children.clear()

        val ruleList = Settings.ligatureRules

        if (ruleList.isEmpty()) {
            rGridPane.add(rLabelHint, 0, 0)
        } else {
            for (rule in ruleList) createLigatureRow(rule.first, rule.second)
        }
    }
    private fun createLigatureRow(from: String = "", to: String = "") {
        val newRowIndex = if (rGridPane.rowCount == 0) 1 else rGridPane.rowCount

        if (rGridPane.children.size == 1 || rGridPane.rowCount == 0) { // Only hint || nothing
            rGridPane.children.clear()
            rGridPane.add(rLabelFrom, 0, 0)
            rGridPane.add(rLabelTo, 1, 0)
        }

        val fromField = TextField(from).apply {
            textFormatter = genPropertyFormatter()
            properties[rRuleIndex] = newRowIndex - rRowShift
            properties[rIsFrom] = true
        }
        val toField = TextField(to).apply {
            textFormatter = genPropertyFormatter()
            properties[rRuleIndex] = newRowIndex - rRowShift
            properties[rIsFrom] = false
        }
        val button = Button(I18N["common.delete"]) does { removeLigatureRow(GridPane.getRowIndex(this)) }

        //   0         1         2
        // 0 From      To
        // 1 ________  ________  Delete
        rGridPane.add(fromField, 0, newRowIndex)
        rGridPane.add(toField, 1, newRowIndex)
        rGridPane.add(button, 2, newRowIndex)
    }
    private fun removeLigatureRow(index: Int) {
        val toRemoveSet = HashSet<Node>()
        for (node in rGridPane.children) {
            val row = GridPane.getRowIndex(node) ?: 0
            if (row == index) toRemoveSet.add(node)
            if (row > index) {
                GridPane.setRowIndex(node, row - 1)
                node.properties[rRuleIndex] = row - 1 - rRowShift
            }
        }
        rGridPane.children.removeAll(toRemoveSet)

        if (rGridPane.rowCount == rRowShift) {
            rGridPane.children.removeAll(rLabelFrom, rLabelTo)
            rGridPane.add(rLabelHint, 0, 0)
        }
    }

    // ----- Initialize Properties ----- //
    override fun initProperties() {
        // Group
        initGroupTab()

        // Ligature Rule
        initLigatureTab()

        // Mode
        mComboInput.select(Settings.viewModes[0])
        mComboLabel.select(Settings.viewModes[1])
        mComboScale.select(Settings.newPictureScalePicture)

        // Label
        lCLabel.anchorPaneLeft = (lLabelPane.prefWidth - lCLabel.prefWidth) / 2
        lCLabel.anchorPaneTop = (lLabelPane.prefHeight - lCLabel.prefHeight) / 2

        lLabelRadius.isEditing = false
        lLabelRadius.text = String.format("%05.2f", Settings.labelRadius)
        lSliderRadius.value = Settings.labelRadius

        lLabelAlpha.isEditing = false
        lLabelAlpha.text = "0x${(Settings.labelOpacity * 255).roundToInt().toString(16).padStart(2, '0')}"
        lSliderAlpha.value = Settings.labelOpacity * 255

        // Other
        xInstCheckBox.isSelected = Settings.instantTranslate
        xUseMCheckBox.isSelected = Settings.useMeoFileAsDefault
        xUseTCheckBox.isSelected = Settings.useExportNameTemplate
        xTemplateField.text = Settings.exportNameTemplate
    }

    // ----- Result convert ---- //
    private fun convertGroup(): List<CProperty> {
        val list = ArrayList<CProperty>()

        val size = gGridPane.rowCount - gRowShift
        if (size < 0) {
            list.add(CProperty(Settings.DefaultGroupNameList, CProperty.EMPTY))
            list.add(CProperty(Settings.DefaultGroupColorHexList, CProperty.EMPTY))
            list.add(CProperty(Settings.IsGroupCreateOnNewTrans, CProperty.EMPTY))

            return list
        }

        val nameList = MutableList(size) { "" }
        val colorList = MutableList(size) { "" }
        val isCreateList = MutableList(size) { false }
        for (node in gGridPane.children) {
            val groupId = GridPane.getRowIndex(node) - gRowShift
            if (groupId < 0) continue
            when (node) {
                is CheckBox -> isCreateList[groupId] = node.isSelected
                is TextField -> nameList[groupId] = node.text
                is ColorPicker -> colorList[groupId] = node.value.toHexRGB()
            }
        }

        list.add(CProperty(Settings.DefaultGroupNameList, nameList))
        list.add(CProperty(Settings.DefaultGroupColorHexList, colorList))
        list.add(CProperty(Settings.IsGroupCreateOnNewTrans, isCreateList))

        return list
    }
    private fun convertLigatureRule(): List<CProperty> {
        val list = ArrayList<CProperty>()

        val size = rGridPane.rowCount - rRowShift
        if (size < 0) {
            list.add(CProperty(Settings.LigatureRules, CProperty.EMPTY))

            return list
        }

        val fromList = MutableList(size) { "" }
        val toList = MutableList(size) { "" }
        for (node in rGridPane.children) {
            if (node is TextField) {
                val ruleIndex = node.properties[rRuleIndex] as Int
                val isFrom = node.properties[rIsFrom] as Boolean

                if (isFrom) fromList[ruleIndex] = node.text
                else toList[ruleIndex] = node.text
            }
        }
        // Abandon repeated rule-from
        val rules = List(size) { fromList[it] to toList[it] }.toMap().toList()

        list.add(CProperty(Settings.LigatureRules, rules))

        return list
    }
    private fun convertMode(): List<CProperty> {
        val list = ArrayList<CProperty>()

        list.add(CProperty(Settings.ViewModeOrdinals, mComboInput.value.ordinal, mComboLabel.value.ordinal))
        list.add(CProperty(Settings.ScaleOnNewPictureOrdinal, mComboScale.value.ordinal))

        return list
    }
    private fun convertLabel(): List<CProperty> {
        val list = ArrayList<CProperty>()

        list.add(CProperty(Settings.LabelRadius, lSliderRadius.value))
        list.add(CProperty(Settings.LabelAlpha, lSliderAlpha.value.roundToInt().toString(16).padStart(2, '0')))

        return list
    }
    private fun convertOther(): List<CProperty> {
        val list = ArrayList<CProperty>()

        list.add(CProperty(Settings.InstantTranslate, xInstCheckBox.isSelected))
        list.add(CProperty(Settings.UseMeoFileAsDefault, xUseMCheckBox.isSelected))
        list.add(CProperty(Settings.UseExportNameTemplate, xUseTCheckBox.isSelected))
        list.add(CProperty(Settings.ExportNameTemplate, xTemplateField.text))

        return list
    }

    override fun convertResult(): List<CProperty> {
        return ArrayList<CProperty>().apply {
            addAll(convertGroup())
            addAll(convertLigatureRule())
            addAll(convertMode())
            addAll(convertLabel())
            addAll(convertOther())
        }
    }

}
