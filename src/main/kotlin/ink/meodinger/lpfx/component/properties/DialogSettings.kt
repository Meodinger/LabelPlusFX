package ink.meodinger.lpfx.component.properties

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.component.CLabel
import ink.meodinger.lpfx.component.CLabelPane
import ink.meodinger.lpfx.component.common.CColorPicker
import ink.meodinger.lpfx.component.common.CComboBox
import ink.meodinger.lpfx.component.common.CInputLabel
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.util.color.isColorHex
import ink.meodinger.lpfx.util.color.toHexRGB
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.property.minus
import ink.meodinger.lpfx.util.property.onChange
import ink.meodinger.lpfx.util.property.onNew
import ink.meodinger.lpfx.util.string.emptyString
import ink.meodinger.lpfx.util.string.isMathematicalDecimal

import javafx.collections.FXCollections
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
import kotlin.math.ceil


/**
 * Author: Meodinger
 * Date: 2021/8/25
 * Have fun with my code!
 */

/**
 * A Dialog Singleton for settings
 */
class DialogSettings : AbstractPropertiesDialog() {

    companion object {
        private const val gRowShift = 1
        private const val rRowShift = 1
        private const val qRowShift = 1
        private const val rIsFrom = "C_Is_From"
        private const val rRuleIndex = "C_Rule_Index"
    }

    private val gGridPane = GridPane().apply {
        alignment = Pos.TOP_CENTER
        padding = Insets(16.0)
        vgap = 16.0
        hgap = 16.0
    }
    private val gLabelHint = Label(I18N["settings.group.hint"])
    private val gLabelName = Label(I18N["settings.group.name"])
    private val gLabelColor = Label(I18N["settings.group.color"])
    private val gLabelCreate = Label(I18N["settings.group.is_create_on_new"])

    private val rGridPane = GridPane().apply {
        alignment = Pos.TOP_CENTER
        padding = Insets(16.0)
        vgap = 16.0
        hgap = 16.0
    }
    private val rLabelHint = Label(I18N["settings.ligature.hint"])
    private val rLabelFrom = Label(I18N["settings.ligature.from"])
    private val rLabelTo = Label(I18N["settings.ligature.to"])

    private val qGridPane = GridPane().apply {
        alignment = Pos.TOP_CENTER
        padding = Insets(16.0)
        vgap = 16.0
        hgap = 16.0
    }
    private val qLabelHint = Label(I18N["settings.quick_input.hint"])

    private val mComboInput = CComboBox<ViewMode>()
    private val mComboLabel = CComboBox<ViewMode>()
    private val mComboScale = CComboBox<CLabelPane.NewPictureScale>()
    private val mCheckWheel = CheckBox(I18N["settings.mode.use_wheel"])

    private val lCLabel = CLabel(labelIndex = 8, labelColor = Color.RED)
    private val lLabelPane = AnchorPane()
    private val lSliderRadius = Slider()
    private val lSliderAlpha = Slider()
    private val lLabelRadius = CInputLabel()
    private val lLabelAlpha = CInputLabel()
    private val lCheckTextOpaque = CheckBox(I18N["settings.label.text_opaque"])

    private val xCheckUpdate = CheckBox(I18N["settings.other.auto_check_upd"])
    private val xCheckAutoOp = CheckBox(I18N["settings.other.auto_open_last"])
    private val xCheckInstTr = CheckBox(I18N["settings.other.inst_trans"])
    private val xCheckFormat = CheckBox(I18N["settings.other.check_format"])
    private val xCheckUseMeo = CheckBox(I18N["settings.other.meo_default"])
    private val xCheckUseTmp = CheckBox(I18N["settings.other.template.enable"])
    private val xFieldTemplate = TextField()
    private val xCheckUseCustomBaiduKey = CheckBox(I18N["settings.other.Translate_keys.enable"])
    private val xFieldBaiduTranslateKey = TextField()
    private val xFieldBaiduTranslateAppId = TextField()

    init {
        title = I18N["settings.title"]
        dialogPane.prefWidth = 600.0
        dialogPane.prefHeight = 480.0
        dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
        dialogPane.withContent(TabPane()) {
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

            add(I18N["settings.group.title"]) {
                withContent(BorderPane()) {
                    val stackPane = StackPane(gGridPane)
                    val scrollPane = ScrollPane(stackPane)
                    stackPane.prefWidthProperty().bind(scrollPane.widthProperty() - 16.0)

                    center(scrollPane) { style = "-fx-background-color:transparent;" }
                    bottom(HBox()) {
                        alignment = Pos.CENTER_RIGHT
                        padding = Insets(16.0, 8.0, 8.0, 16.0)
                        add(Button(I18N["settings.group.add"])) { does { createGroupRow() } }
                    }
                }
            }
            add(I18N["settings.ligature.title"]) {
                withContent(BorderPane()) {
                    val stackPane = StackPane(rGridPane)
                    val scrollPane = ScrollPane(stackPane)
                    stackPane.prefWidthProperty().bind(scrollPane.widthProperty() - 16.0)

                    center(scrollPane) { style = "-fx-background-color:transparent;" }
                    bottom(HBox()) {
                        alignment = Pos.CENTER_RIGHT
                        padding = Insets(16.0, 8.0, 8.0, 16.0)
                        add(Label(I18N["settings.ligature.sample"]))
                        add(HBox()) { hgrow = Priority.ALWAYS }
                        add(Button(I18N["settings.ligature.add"])) { does { createLigatureRow() } }
                    }
                }
            }
            add(I18N["settings.quick_input.title"]) {
                withContent(BorderPane()) {
                    val stackPane = StackPane(qGridPane)
                    val scrollPane = ScrollPane(stackPane)
                    stackPane.prefWidthProperty().bind(scrollPane.widthProperty() - 16.0)

                    center(scrollPane) { style = "-fx-background-color:transparent;" }
                    bottom(HBox()) {
                        alignment = Pos.CENTER_RIGHT
                        padding = Insets(16.0, 8.0, 8.0, 16.0)
                        add(Label(I18N["settings.quick_input.sample"]))
                        add(HBox()) { hgrow = Priority.ALWAYS }
                        add(Button(I18N["settings.quick_input.add"])) { does { createQuickInputRow() } }
                    }
                }
            }
            add(I18N["settings.mode.title"]) {
                withContent(GridPane()) {
                    alignment = Pos.TOP_CENTER
                    padding = Insets(16.0)
                    vgap = 16.0
                    hgap = 16.0

                    //   0         1
                    // 0 Input     | input | < > (ViewMode)
                    // 1 Label     | label | < > (ViewMode)
                    // 2
                    // 3 Scale on new picture
                    // 4 | selection | < >       (NewPicScale)
                    // 5
                    // 6 Use wheel to scale      (UseWheelToScale)

                    add(Label(I18N["mode.work.input"]), 0, 0)
                    add(mComboInput, 1, 0) {
                        prefWidth = 160.0
                        items = FXCollections.observableList(ViewMode.values().toList())
                        isWrapped = true
                    }
                    add(Label(I18N["mode.work.label"]), 0, 1)
                    add(mComboLabel, 1, 1) {
                        prefWidth = 160.0
                        items = FXCollections.observableList(ViewMode.values().toList())
                        isWrapped = true
                    }
                    add(HBox(), 0, 2)
                    add(Label(I18N["settings.mode.scale.label"]), 0, 3, 2, 1)
                    add(mComboScale, 0, 4, 2, 1) {
                        prefWidth = 224.0
                        items = FXCollections.observableList(CLabelPane.NewPictureScale.values().toList())
                        isWrapped = true
                    }
                    add(HBox(), 0, 5)
                    add(mCheckWheel, 0, 6, 2, 1)
                }
            }
            add(I18N["settings.label.title"]) {
                withContent(GridPane()) {
                    alignment = Pos.CENTER
                    padding = Insets(16.0, 16.0, 0.0, 16.0)
                    vgap = 16.0
                    hgap = 16.0

                    // lGridPane.isGridLinesVisible = true
                    //   0           1           2
                    // 0 ----------  Radius
                    // 1 |        |  ----O------ 24.0
                    // 2 |        |  Alpha
                    // 3 |        |  ------O---- 0x80
                    // 4 |        |  O TextOpaque
                    // 5 ----------    *HTLP TEXT*
                    add(lLabelPane, 0, 0, 1, 6) {
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
                            colorOpacityProperty().bind(lSliderAlpha.valueProperty())
                            textOpaqueProperty().bind(lCheckTextOpaque.selectedProperty())

                            // Draggable & drag-limitation
                            var shiftX = 0.0
                            var shiftY = 0.0
                            addEventHandler(MouseEvent.MOUSE_PRESSED) {
                                // Mark immediately when this event will be consumed
                                it.consume() // disable further propagation

                                cursor = Cursor.MOVE

                                shiftX = anchorX - it.sceneX
                                shiftY = anchorY - it.sceneY
                            }
                            addEventHandler(MouseEvent.MOUSE_DRAGGED) {
                                // Mark immediately when this event will be consumed
                                it.consume() // disable further propagation

                                val newAnchorX = shiftX + it.sceneX
                                val newAnchorY = shiftY + it.sceneY

                                //  0--L-----    0 LR LR |
                                //  |  R         LR      |
                                //  |LR|-----    LR      |
                                //  |  |         --------|
                                val limitX = SAMPLE_IMAGE.width - prefWidth - 2 * lLabelPaneBorderWidth
                                val limitY = SAMPLE_IMAGE.height - prefHeight - 2 * lLabelPaneBorderWidth
                                if (newAnchorX < 0 || newAnchorX > limitX) return@addEventHandler
                                if (newAnchorY < 0 || newAnchorY > limitY) return@addEventHandler

                                anchorX = newAnchorX
                                anchorY = newAnchorY
                            }
                            addEventHandler(MouseEvent.MOUSE_RELEASED) {
                                cursor = Cursor.HAND
                            }
                            radiusProperty().addListener(onChange {
                                val limitX = SAMPLE_IMAGE.width - prefWidth - 2 * lLabelPaneBorderWidth
                                val limitY = SAMPLE_IMAGE.height - prefHeight - 2 * lLabelPaneBorderWidth
                                if (anchorX > limitX) anchorX = limitX
                                if (anchorY > limitY) anchorY = limitY
                            })
                        }
                    }
                    add(Label(I18N["settings.label.radius"]), 1, 0)
                    add(lSliderRadius, 1, 1) {
                        prefWidth = 160.0

                        min = 8.0
                        max = 48.0
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
                        textFormatter = genTextFormatter<String> {
                            if (!it.text.isMathematicalDecimal() ||
                                !it.controlNewText.isMathematicalDecimal()
                            ) emptyString() else it.text
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
                        max = 1.0
                        majorTickUnit = max / 4
                        minorTickCount = 3
                        blockIncrement = majorTickUnit / (minorTickCount + 1)
                        isShowTickMarks = true
                        isShowTickLabels = true
                        labelFormatter = object : StringConverter<Double>() {
                            override fun toString(double: Double): String = ceil(double * 255.0).toInt().toString(16).padStart(2, '0')
                            override fun fromString(string: String): Double = string.toInt(16).toDouble() / 255.0
                        }
                        valueProperty().addListener(onNew<Number, Double> {
                            val alphaPart = ceil(it * 255.0).toInt().toString(16).padStart(2, '0')
                            lLabelAlpha.text = (if (lLabelAlpha.isEditing) "" else "0x") + alphaPart
                        })
                    }
                    add(lLabelAlpha, 2, 3) {
                        textFormatter = genTextFormatter<String> {
                            if (it.text.uppercase().contains(Regex("[^\\dA-F]")) ||
                                it.controlNewText.length > 2
                            ) emptyString() else it.text
                        }
                        setOnChangeToField {
                            fieldText = labelText.substring(2)
                        }
                        setOnChangeToLabel {
                            val alphaStr = fieldText.padStart(2, '0').uppercase()
                            lSliderAlpha.value = alphaStr.toInt(16).toDouble() / 255.0

                            labelText = "0x$alphaStr"
                        }
                    }
                    add(lCheckTextOpaque, 1, 4, 2, 1)
                    add(Label(I18N["settings.label.helpText"]), 1, 5, 2, 1) {
                        isWrapText = true
                        textAlignment = TextAlignment.CENTER
                    }
                }
            }
            add(I18N["settings.other.title"]) {
                withContent(GridPane()) {
                    alignment = Pos.TOP_CENTER
                    padding = Insets(16.0)
                    vgap = 16.0
                    hgap = 16.0

                    //   0        1
                    // 0 O UpdateCheck
                    // 1 O OpenLastFile
                    // 2 O InstantTranslate
                    // 3 O CheckFormatWhenSave
                    // 4 O UseMeoFileAsDefault
                    // 5 O UseExportTemplate
                    // 6   |  template text  |

                    add(xCheckUpdate, 0, 0, 2, 1)
                    add(xCheckAutoOp, 0, 1, 2, 1)
                    add(xCheckInstTr, 0, 2, 2, 1)
                    add(xCheckFormat, 0, 3, 2, 1)
                    add(xCheckUseMeo, 0, 4, 2, 1)
                    add(xCheckUseTmp, 0, 5, 2, 1)
                    add(xFieldTemplate, 1, 6) {
                        disableProperty().bind(!xCheckUseTmp.selectedProperty())
                        textFormatter = genTextFormatter<String> { it.text.replace(Regex("[:*?<>|/\"\\\\]"), "") }
                        tooltip = Tooltip(I18N["settings.other.template.hint"]).apply {
                            showDelay = Duration(500.0)
                        }
                    }
                    add(xCheckUseCustomBaiduKey, 0, 7, 2, 1)
                    add(Label(I18N["settings.other.Translate_keys.key"]), 0, 8)
                    add(xFieldBaiduTranslateKey, 1, 8) {
                        disableProperty().bind(!xCheckUseCustomBaiduKey.selectedProperty())

                    }
                    add(Label(I18N["settings.other.Translate_keys.app_id"]), 0, 9)
                    add(xFieldBaiduTranslateAppId, 1, 9) {
                        disableProperty().bind(!xCheckUseCustomBaiduKey.selectedProperty())
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
            gGridPane.add(gLabelCreate, 2, 0)
        }

        val groupId = newRowIndex - gRowShift
        val colorHex = if (color.isColorHex()) color else TransFile.DEFAULT_COLOR_HEX_LIST[groupId % 9]

        val checkBox = CheckBox().apply { isSelected = createOnNew }
        val textField = TextField(name).apply { textFormatter = genGeneralFormatter() }
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
            gGridPane.children.removeAll(gLabelCreate, gLabelName, gLabelColor)
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
            for ((from, to) in ruleList) createLigatureRow(from, to)
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
            textFormatter = genGeneralFormatter()
            properties[rRuleIndex] = newRowIndex - rRowShift
            properties[rIsFrom] = true
        }
        val toField = TextField(to).apply {
            textFormatter = genGeneralFormatter()
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

    // ----- Quick Input  ----- //
    private fun initQuickInputTab() {
        qGridPane.children.clear()

        val quickInputTextsList = Settings.quickInputTexts

        if (quickInputTextsList.isEmpty()) {
            qGridPane.add(qLabelHint, 0, 0)
        } else {
            for ( text in quickInputTextsList) createQuickInputRow(text)
        }
    }
    private fun createQuickInputRow(text: String = "") {
        val newRowIndex = if (qGridPane.rowCount == 0) 1 else qGridPane.rowCount
        if (qGridPane.children.size == 1 || qGridPane.rowCount == 0) { // Only hint || nothing
            qGridPane.children.clear()
        }
        val textField = TextField(text).apply {
            textFormatter = genGeneralFormatter()
        }
        val button = Button(I18N["common.delete"]) does { removeQuickInputRow(GridPane.getRowIndex(this)) }
        //   0         1         2
        // 1 textField  ________  Delete
        qGridPane.add(textField, 0, newRowIndex)
        qGridPane.add(button, 1, newRowIndex)
    }
    private fun removeQuickInputRow(index: Int) {
        val toRemoveSet = HashSet<Node>()
        for (node in qGridPane.children) {
            val row = GridPane.getRowIndex(node) ?: 0
            if (row == index) toRemoveSet.add(node)
            if (row > index) {
                GridPane.setRowIndex(node, row - 1)
            }
        }
        qGridPane.children.removeAll(toRemoveSet)

        if (qGridPane.rowCount == qRowShift) {
            qGridPane.children.removeAll()
            qGridPane.add(qLabelHint, 0, 0)
        }
    }


    // ----- Initialize Properties ----- //
    override fun initProperties() {
        // Group
        initGroupTab()

        // Ligature Rule
        initLigatureTab()

        // quick Input
        initQuickInputTab()

        // Mode
        mComboInput.select(Settings.viewModes[0])
        mComboLabel.select(Settings.viewModes[1])
        mComboScale.select(Settings.newPictureScalePicture)
        mCheckWheel.isSelected = Settings.useWheelToScale

        // Label
        lCLabel.anchorX = (lLabelPane.prefWidth - lCLabel.prefWidth) / 2
        lCLabel.anchorY = (lLabelPane.prefHeight - lCLabel.prefHeight) / 2

        lLabelRadius.isEditing = false
        lSliderRadius.value = Settings.labelRadius

        lLabelAlpha.isEditing = false
        lSliderAlpha.value = Settings.labelColorOpacity

        lCheckTextOpaque.isSelected = Settings.labelTextOpaque

        // Other
        xCheckUpdate.isSelected = Settings.autoCheckUpdate
        xCheckAutoOp.isSelected = Settings.autoOpenLastFile
        xCheckInstTr.isSelected = Settings.instantTranslate
        xCheckFormat.isSelected = Settings.checkFormatWhenSave
        xCheckUseMeo.isSelected = Settings.useMeoFileAsDefault
        xCheckUseTmp.isSelected = Settings.useExportNameTemplate
        xFieldTemplate.text = Settings.exportNameTemplate
        xCheckUseCustomBaiduKey.isSelected = Settings.useCustomBaiduKey
        xFieldBaiduTranslateKey.text = Settings.baiduTransLateKey
        xFieldBaiduTranslateAppId.text = Settings.baiduTransLateAppId
    }

    // ----- Result convert ---- //
    private fun convertGroup(): Map<String, Any> {
        val size = gGridPane.rowCount - gRowShift
        if (size < 0) return emptyMap()

        val map = HashMap<String, Any>()

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

        map[Settings.DefaultGroupNameList] = nameList
        map[Settings.DefaultGroupColorHexList] = colorList
        map[Settings.IsGroupCreateOnNewTrans] = isCreateList

        return map
    }
    private fun convertLigatureRule(): Map<String, Any> {
        val size = rGridPane.rowCount - rRowShift
        if (size < 0) return emptyMap()

        val map = HashMap<String, List<Pair<String, String>>>()

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
        // Abandon repeated rule-from by toMap
        val rules = List(size) { fromList[it] to toList[it] }.toMap().toList()

        map[Settings.LigatureRules] = rules

        return map
    }
    private fun convertQuickInput(): Map<String, Any> {
        val map = HashMap<String, Any>()
        val size = qGridPane.rowCount - qRowShift
        if (size < 0) return emptyMap()

        val textList = MutableList(size) { "" }
        for (node in qGridPane.children) {
            val groupId = GridPane.getRowIndex(node) - qRowShift
            if (groupId < 0) continue
            when (node) {
                is TextField -> textList[groupId] = node.text
            }
        }
        map[Settings.QuickInputTexts] = textList
        return map
    }
    private fun convertMode(): Map<String, Any> {
        val map = HashMap<String, Any>()

        map[Settings.ViewModes] = listOf(mComboInput.index, mComboLabel.index).map(ViewMode.values()::get)
        map[Settings.NewPictureScale] = mComboScale.index.let(CLabelPane.NewPictureScale.values()::get)
        map[Settings.UseWheelToScale] = mCheckWheel.isSelected

        return map
    }
    private fun convertLabel(): Map<String, Any> {
        val map = HashMap<String, Any>()

        map[Settings.LabelRadius] = lSliderRadius.value
        map[Settings.LabelColorOpacity] = lSliderAlpha.value
        map[Settings.LabelTextOpaque] = lCheckTextOpaque.isSelected

        return map
    }
    private fun convertOther(): Map<String, Any> {
        val map = HashMap<String, Any>()

        map[Settings.AutoCheckUpdate] = xCheckUpdate.isSelected
        map[Settings.AutoOpenLastFile] = xCheckAutoOp.isSelected
        map[Settings.InstantTranslate] = xCheckInstTr.isSelected
        map[Settings.CheckFormatWhenSave] = xCheckFormat.isSelected
        map[Settings.UseMeoFileAsDefault] = xCheckUseMeo.isSelected
        map[Settings.UseExportNameTemplate] = xCheckUseTmp.isSelected
        map[Settings.ExportNameTemplate] = xFieldTemplate.text
        map[Settings.UseCustomBaiduKey] = xCheckUseCustomBaiduKey.isSelected
        map[Settings.BaiduTransLateKey] = xFieldBaiduTranslateKey.text
        map[Settings.BaiduTransLateAppId] = xFieldBaiduTranslateAppId.text

        return map
    }

    override fun convertResult(): Map<String, Any> {
        return HashMap<String, Any>().apply {
            putAll(convertGroup())
            putAll(convertLigatureRule())
            putAll(convertQuickInput())
            putAll(convertMode())
            putAll(convertLabel())
            putAll(convertOther())
        }
    }

}
