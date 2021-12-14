package ink.meodinger.lpfx.component.singleton

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.component.CLabel
import ink.meodinger.lpfx.component.common.CColorPicker
import ink.meodinger.lpfx.component.common.CComboBox
import ink.meodinger.lpfx.component.common.CInputLabel
import ink.meodinger.lpfx.options.CProperty
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.util.color.isColorHex
import ink.meodinger.lpfx.util.color.toHex
import ink.meodinger.lpfx.util.component.anchorPaneLeft
import ink.meodinger.lpfx.util.component.anchorPaneTop
import ink.meodinger.lpfx.util.component.invoke
import ink.meodinger.lpfx.util.component.does
import ink.meodinger.lpfx.util.property.minus
import ink.meodinger.lpfx.util.property.onChange
import ink.meodinger.lpfx.util.property.onNew
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.SAMPLE_IMAGE
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.string.isMathematicalDecimal

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment


/**
 * Author: Meodinger
 * Date: 2021/8/25
 * Have fun with my code!
 */

/**
 * A Dialog Singleton for settings
 */
object ASettingsDialog : AbstractPropertiesDialog() {

    private val tabPane = TabPane()

    private val groupTab = Tab(I18N["settings.group.title"])
    private val gBorderPane = BorderPane()
    private val gLabelHint = Label(I18N["settings.group.hint"])
    private val gLabelIsCreate = Label(I18N["settings.group.is_create_on_new"])
    private val gLabelName = Label(I18N["settings.group.name"])
    private val gLabelColor = Label(I18N["settings.group.color"])
    private val gGridPane = GridPane()
    private val gButtonAdd = Button(I18N["settings.group.add"])
    private const val gRowShift = 1

    private val ruleTab = Tab(I18N["settings.ligature.title"])
    private val rBorderPane = BorderPane()
    private val rLabelHint = Label(I18N["settings.ligature.hint"])
    private val rLabelFrom = Label(I18N["settings.ligature.from"])
    private val rLabelTo = Label(I18N["settings.ligature.to"])
    private val rGridPane = GridPane()
    private val rLabelSample = Label(I18N["settings.ligature.sample"])
    private val rButtonAdd = Button(I18N["settings.ligature.add"])
    private const val rRowShift = 1
    private const val rIsFrom = "C_Is_From"
    private const val rRuleIndex = "C_Rule_Index"

    private val modeTab = Tab(I18N["settings.mode.title"])
    private val mGridPane = GridPane()
    private val mComboInput = CComboBox<ViewMode>()
    private val mComboLabel = CComboBox<ViewMode>()
    private val mComboScale = CComboBox<String>()

    private val labelTab = Tab(I18N["settings.label.title"])
    private val lGridPane = GridPane()
    private val lCLabel = CLabel(index = 8)
    private val lLabelPane = AnchorPane()
    private val lSliderRadius = Slider()
    private val lSliderAlpha = Slider()
    private val lLabelRadius = CInputLabel()
    private val lLabelAlpha = CInputLabel()

    init {
        initOwner(State.stage)

        // ----- Group ----- //
        // initGroupTab()
        gGridPane.padding = Insets(COMMON_GAP)
        gGridPane.vgap = COMMON_GAP
        gGridPane.hgap = COMMON_GAP
        gGridPane.alignment = Pos.TOP_CENTER
        gButtonAdd.setOnAction { createGroupRow() }
        val gStackPane = StackPane(gGridPane)
        val gScrollPane = ScrollPane(gStackPane)
        gStackPane.prefWidthProperty().bind(gScrollPane.widthProperty() - COMMON_GAP)
        gScrollPane.style = "-fx-background-color:transparent;"
        gBorderPane.center = gScrollPane
        gBorderPane.bottom = HBox(gButtonAdd).also {
            it.alignment = Pos.CENTER_RIGHT
            it.padding = Insets(COMMON_GAP, COMMON_GAP / 2, COMMON_GAP / 2, COMMON_GAP)
        }
        groupTab.content = gBorderPane

        // ----- Ligature Rule ----- //
        // initLigatureTab()
        rGridPane.padding = Insets(COMMON_GAP)
        rGridPane.vgap = COMMON_GAP
        rGridPane.hgap = COMMON_GAP
        rGridPane.alignment = Pos.TOP_CENTER
        rButtonAdd.setOnAction { createLigatureRow() }
        val rStackPane = StackPane(rGridPane)
        val rScrollPane = ScrollPane(rStackPane)
        rStackPane.prefWidthProperty().bind(rScrollPane.widthProperty() - COMMON_GAP)
        rScrollPane.style = "-fx-background-color:transparent;"
        rBorderPane.center = rScrollPane
        rBorderPane.bottom = HBox(rLabelSample, HBox().also { HBox.setHgrow(it, Priority.ALWAYS) }, rButtonAdd).also {
            it.alignment = Pos.CENTER_RIGHT
            it.padding = Insets(COMMON_GAP, COMMON_GAP / 2, COMMON_GAP / 2, COMMON_GAP)
        }
        ruleTab.content = rBorderPane

        // ----- Mode ----- //
        initModeTab()
        mGridPane.padding = Insets(COMMON_GAP)
        mGridPane.vgap = COMMON_GAP
        mGridPane.hgap = COMMON_GAP
        mGridPane.alignment = Pos.TOP_CENTER
        modeTab.content = mGridPane

        // ----- Label ----- //
        initLabelTab()
        lGridPane.padding = Insets(COMMON_GAP, COMMON_GAP, 0.0, COMMON_GAP)
        lGridPane.vgap = COMMON_GAP
        lGridPane.hgap = COMMON_GAP
        lGridPane.alignment = Pos.CENTER
        labelTab.content = lGridPane

        // ----- Tab ----- //
        tabPane.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
        tabPane.prefWidth = DIALOG_WIDTH
        tabPane.prefHeight = DIALOG_HEIGHT
        tabPane.tabs.addAll(groupTab, ruleTab, modeTab, labelTab)

        initProperties()

        title = I18N["settings.title"]
        dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
        dialogPane.content = tabPane
    }

    // ----- Group ----- //
    private fun initGroupTab() {
        gGridPane.children.clear()

        val nameList = Settings[Settings.DefaultGroupNameList].asStringList()
        val colorList = Settings[Settings.DefaultGroupColorHexList].asStringList()
        val createList = Settings[Settings.IsGroupCreateOnNewTrans].asBooleanList()

        if (nameList.isEmpty()) {
            gGridPane.add(gLabelHint, 0, 0)
        } else {
            for (i in nameList.indices) createGroupRow(createList[i], nameList[i], colorList[i])
        }
    }
    private fun createGroupRow(createOnNew: Boolean = false, name: String = "", color: String = "") {
        val newRowIndex = if (gGridPane.rowCount == 0) 1 else gGridPane.rowCount

        if (gGridPane.children.size == 1 || gGridPane.rowCount == 0) { // Only hint
            gGridPane.children.clear()
            gGridPane.add(gLabelName, 0, 0)
            gGridPane.add(gLabelColor, 1, 0)
            gGridPane.add(gLabelIsCreate, 2, 0)
        }

        val groupId = newRowIndex - gRowShift
        val colorHex =
            if (color.isColorHex())
                color
            else {
                var defaultColorHexList = Settings[Settings.DefaultGroupColorHexList].asStringList()
                if (defaultColorHexList.isEmpty()) defaultColorHexList = TransFile.Companion.LPTransFile.DEFAULT_COLOR_HEX_LIST

                defaultColorHexList[groupId % defaultColorHexList.size]
            }

        val checkBox = CheckBox().also { it.isSelected = createOnNew }
        val textField = TextField(name).also { it.textFormatter = getGroupNameFormatter() }
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
        val toRemoveList = ArrayList<Node>()
        for (node in gGridPane.children) {
            val row = GridPane.getRowIndex(node) ?: 0
            if (row == index) toRemoveList.add(node)
            if (row > index) GridPane.setRowIndex(node, row - 1)
        }
        gGridPane.children.removeAll(toRemoveList)

        if (gGridPane.rowCount == gRowShift) {
            gGridPane.children.removeAll(gLabelIsCreate, gLabelName, gLabelColor)
            gGridPane.add(gLabelHint, 0, 0)
        }
    }

    // ----- Ligature ----- //
    private fun initLigatureTab() {
        rGridPane.children.clear()

        val ruleList = Settings[Settings.LigatureRules].asPairList()

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

        val fromField = TextField(from).also {
            it.textFormatter = getPropertyFormatter()
            it.properties[rRuleIndex] = newRowIndex - rRowShift
            it.properties[rIsFrom] = true
        }
        val toField = TextField(to).also {
            it.textFormatter = getPropertyFormatter()
            it.properties[rRuleIndex] = newRowIndex - rRowShift
            it.properties[rIsFrom] = false
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
        val toRemoveList = ArrayList<Node>()
        for (node in rGridPane.children) {
            val row = GridPane.getRowIndex(node) ?: 0
            if (row == index) toRemoveList.add(node)
            if (row > index) {
                GridPane.setRowIndex(node, row - 1)
                node.properties[rRuleIndex] = row - 1 - rRowShift
            }
        }
        rGridPane.children.removeAll(toRemoveList)

        if (rGridPane.rowCount == rRowShift) {
            rGridPane.children.removeAll(rLabelFrom, rLabelTo)
            rGridPane.add(rLabelHint, 0, 0)
        }
    }

    // ----- Mode ----- //
    private fun initModeTab() {
        val viewModeList = listOf(ViewMode.IndexMode, ViewMode.GroupMode)

        mComboInput.items.setAll(viewModeList)
        mComboInput.isWrapped = true
        mComboLabel.items.setAll(viewModeList)
        mComboLabel.isWrapped = true
        mComboScale.items.setAll(listOf(
            I18N["settings.mode.scale.100"],
            I18N["settings.mode.scale.fit"],
            I18N["settings.mode.scale.last"]
        ))
        mComboScale.isWrapped = true

        //   0         1
        // 0 WorkMode  ViewMode
        // 1 Input     | input | < > (ViewMode)
        // 2 Label     | label | < > (ViewMode)
        // 3
        // 4 Scale on new picture
        // 5 | selection | < >       (String)
        mGridPane.add(Label(I18N["mode.work"]), 0, 0)
        mGridPane.add(Label(I18N["mode.view"]), 1, 0)
        mGridPane.add(Label(I18N["mode.work.input"]), 0, 1)
        mGridPane.add(Label(I18N["mode.work.label"]), 0, 2)
        mGridPane.add(mComboInput, 1, 1)
        mGridPane.add(mComboLabel, 1, 2)
        mGridPane.add(HBox(), 0, 3)
        mGridPane.add(Label(I18N["settings.mode.scale.label"]), 0, 4, 2, 1)
        mGridPane.add(mComboScale, 0, 5, 2, 1)
    }

    // ----- Label ----- //
    private fun initLabelTab() {
        val lLabelPaneEdgeLength = 320.0
        val lLabelPaneBorderWidth = 2.0

        lLabelPane.setPrefSize(lLabelPaneEdgeLength, lLabelPaneEdgeLength)
        lLabelPane.border = Border(BorderStroke(
            Color.DARKGRAY,
            BorderStrokeStyle.SOLID,
            CornerRadii(0.0),
            BorderWidths(lLabelPaneBorderWidth)
        ))
        lLabelPane.background = Background(BackgroundImage(
            SAMPLE_IMAGE,
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            BackgroundSize.DEFAULT
        ))

        // Draggable & drag-limitation
        var shiftX = 0.0
        var shiftY = 0.0
        lCLabel.addEventHandler(MouseEvent.MOUSE_PRESSED) {
            it.consume()

            lCLabel.cursor = Cursor.MOVE

            shiftX = lCLabel.anchorPaneLeft - it.sceneX
            shiftY = lCLabel.anchorPaneTop - it.sceneY
        }
        lCLabel.addEventHandler(MouseEvent.MOUSE_DRAGGED) {
            it.consume()

            val newLayoutX = shiftX + it.sceneX
            val newLayoutY = shiftY + it.sceneY

            //  0--L-----    0 LR LR |
            //  |  R         LR      |
            //  |LR|-----    LR      |
            //  |  |         --------|
            val limitX = SAMPLE_IMAGE.width - lCLabel.prefWidth - 2 * lLabelPaneBorderWidth
            val limitY = SAMPLE_IMAGE.height - lCLabel.prefHeight - 2 * lLabelPaneBorderWidth
            if (newLayoutX < 0 || newLayoutX > limitX) return@addEventHandler
            if (newLayoutY < 0 || newLayoutY > limitY) return@addEventHandler

            lCLabel.anchorPaneLeft = newLayoutX
            lCLabel.anchorPaneTop = newLayoutY
        }
        lCLabel.addEventHandler(MouseEvent.MOUSE_RELEASED) {
            lCLabel.cursor = Cursor.HAND
        }
        lCLabel.radiusProperty().addListener(onChange {
            val limitX = SAMPLE_IMAGE.width - lCLabel.prefWidth - 2 * lLabelPaneBorderWidth
            val limitY = SAMPLE_IMAGE.height - lCLabel.prefHeight - 2 * lLabelPaneBorderWidth
            if (lCLabel.anchorPaneLeft > limitX) lCLabel.anchorPaneLeft = limitX
            if (lCLabel.anchorPaneTop > limitY) lCLabel.anchorPaneTop = limitY
        })

        lLabelPane.children.add(lCLabel)

        lLabelRadius.textFormatter = TextFormatter { change ->
            if (!change.isAdded) return@TextFormatter change

            if (!change.text.isMathematicalDecimal())
                return@TextFormatter change.also { it.text = "" }
            if (!change.controlNewText.isMathematicalDecimal())
                return@TextFormatter change.also { it.text = "" }

            change
        }
        lLabelRadius.setOnChangeFinish {
            lSliderRadius.value = it.toDouble()
        }
        lSliderRadius.valueProperty().addListener(onNew<Number, Double> {
            lLabelRadius.text = String.format("%05.2f",  it)
            lCLabel.radius = it
        })
        lSliderRadius.min = 8.0
        lSliderRadius.max = 48.0

        lLabelAlpha.textFormatter = TextFormatter { change ->
            if (!change.isAdded) return@TextFormatter change

            if (change.text.uppercase().contains(Regex("[^0-9A-F]")))
                return@TextFormatter change.also { it.text = "" }
            if (change.controlText.length + change.text.length > 2)
                return@TextFormatter change.also { it.text = "" }

            return@TextFormatter change
        }
        lLabelAlpha.setOnChangeStart {
            fieldText = it.replace("0x", "")
        }
        lLabelAlpha.setOnChangeFinish {
            lSliderAlpha.value = it.toInt(16) / 255.0
        }
        lSliderAlpha.valueProperty().addListener(onNew<Number, Double> {
            val str = (it * 255.0).toInt().toString(16).uppercase()
            lLabelAlpha.labelText = if (str.length == 1) "0x0$str" else "0x$str"
            lLabelAlpha.fieldText = str
            lCLabel.color = if (str.length == 1) Color.web("FF00000$str") else Color.web("FF0000$str")
        })
        lSliderAlpha.min = 0.0
        lSliderAlpha.max = 1.0

        // lGridPane.isGridLinesVisible = true
        //   0         1           2
        // 0 --------  Radius
        // 1 |      |  ----O------ 24.0
        // 2 |      |  Alpha
        // 3 |      |  ------O---- 0x80
        // 4 --------  *TEXT*
        lGridPane.add(lLabelPane, 0, 0, 1, 5)
        lGridPane.add(Label(I18N["settings.label.helpText"])(true, TextAlignment.CENTER), 1, 4, 2, 1)
        lGridPane.add(Label(I18N["settings.label.radius"]), 1, 0)
        lGridPane.add(lSliderRadius, 1, 1)
        lGridPane.add(lLabelRadius, 2, 1)
        lGridPane.add(Label(I18N["settings.label.alpha"]), 1, 2)
        lGridPane.add(lSliderAlpha, 1, 3)
        lGridPane.add(lLabelAlpha, 2, 3)
    }

    // ----- Initialize Properties ----- //
    override fun initProperties() {
        // Group
        initGroupTab()

        // Ligature Rule
        initLigatureTab()

        // Mode
        val preferenceStringList = Settings[Settings.ViewModePreference].asStringList()
        val preferenceList = List(preferenceStringList.size) { ViewMode.getViewMode(preferenceStringList[it]) }
        mComboInput.select(preferenceList[0])
        mComboLabel.select(preferenceList[1])
        mComboScale.select(Settings[Settings.ScaleOnNewPicture].asInteger())

        // Label
        lCLabel.anchorPaneLeft = (lLabelPane.prefWidth - lCLabel.prefWidth) / 2
        lCLabel.anchorPaneTop = (lLabelPane.prefHeight - lCLabel.prefHeight) / 2
        lLabelRadius.isEditing = false
        lLabelAlpha.isEditing = false
        lSliderRadius.value = Settings[Settings.LabelRadius].asDouble()
        lSliderAlpha.value = Settings[Settings.LabelAlpha].asInteger(16) / 255.0
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
                is ColorPicker -> colorList[groupId] = node.value.toHex()
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

        list.add(CProperty(Settings.ViewModePreference, mComboInput.value, mComboLabel.value))
        list.add(CProperty(Settings.ScaleOnNewPicture, mComboScale.index))

        return list
    }
    private fun convertLabel(): List<CProperty> {
        val list = ArrayList<CProperty>()

        list.add(CProperty(Settings.LabelRadius, lSliderRadius.value))

        var str = (lSliderAlpha.value * 255.0).toInt().toString(16)
        if (str.length == 1) str = "0$str"
        list.add(CProperty(Settings.LabelAlpha, str))

        return list
    }
    override fun convertResult(): List<CProperty> {
        val list = ArrayList<CProperty>()

        list.addAll(convertGroup())
        list.addAll(convertLigatureRule())
        list.addAll(convertMode())
        list.addAll(convertLabel())

        Logger.info("Generated settings", LOGSRC_DIALOGS)
        Logger.debug("got", list, LOGSRC_DIALOGS)

        return list
    }

}
