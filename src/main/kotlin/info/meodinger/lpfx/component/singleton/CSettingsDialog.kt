package info.meodinger.lpfx.component.singleton

import info.meodinger.lpfx.State
import info.meodinger.lpfx.ViewMode
import info.meodinger.lpfx.component.common.CColorPicker
import info.meodinger.lpfx.component.common.CComboBox
import info.meodinger.lpfx.component.common.CInputLabel
import info.meodinger.lpfx.component.CLabel
import info.meodinger.lpfx.getGroupNameFormatter
import info.meodinger.lpfx.options.CProperty
import info.meodinger.lpfx.options.Logger
import info.meodinger.lpfx.options.Settings
import info.meodinger.lpfx.util.color.isColorHex
import info.meodinger.lpfx.util.color.toHex
import info.meodinger.lpfx.util.component.anchorPaneLeft
import info.meodinger.lpfx.util.component.anchorPaneTop
import info.meodinger.lpfx.util.component.invoke
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.SAMPLE_IMAGE
import info.meodinger.lpfx.util.resource.get

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import kotlin.collections.ArrayList


/**
 * Author: Meodinger
 * Date: 2021/8/25
 * Location: info.meodinger.lpfx.component
 */

/**
 * A Dialog Singleton for settings
 */
object CSettingsDialog : AbstractPropertiesDialog() {

    private const val Gap = 16.0

    private val tabPane = TabPane()

    private val groupTab = Tab(I18N["settings.group.title"])
    private val gBorderPane = BorderPane()
    private val gLabelIsCreate = Label(I18N["settings.group.is_create_on_new"])
    private val gLabelName = Label(I18N["settings.group.name"])
    private val gLabelColor = Label(I18N["settings.group.color"])
    private val gGridPane = GridPane()
    private val gButtonAdd = Button(I18N["settings.group.add"])
    private var gRemainGroup = 0
    private const val gRowShift = 1

    private val modeTab = Tab(I18N["settings.mode.title"])
    private val mGridPane = GridPane()
    private val mComboInput = CComboBox<ViewMode>()
    private val mComboLabel = CComboBox<ViewMode>()
    private val mComboScale = CComboBox<String>()

    private val labelTab = Tab(I18N["settings.label.title"])
    private val lGridPane = GridPane()
    private val lCLabel = CLabel(8)
    private val lLabelPane = AnchorPane()
    private val lSliderRadius = Slider()
    private val lSliderAlpha = Slider()
    private val lLabelRadius = CInputLabel()
    private val lLabelAlpha = CInputLabel()

    init {
        initOwner(State.stage)

        // ----- Group ----- //
        // initGroupTab()
        gGridPane.padding = Insets(Gap)
        gGridPane.vgap = Gap
        gGridPane.hgap = Gap
        gGridPane.alignment = Pos.TOP_CENTER
        gButtonAdd.setOnAction { createGroupRow(gRemainGroup) }
        gBorderPane.center = ScrollPane(AnchorPane(gGridPane)).also {
            it.style = "-fx-background-color:transparent;"
            it.widthProperty().addListener { _, _, newValue ->
                gGridPane.layoutX = (newValue as Double - gGridPane.width) / 2
            }
        }
        gBorderPane.bottom = HBox(gButtonAdd).also {
            it.alignment = Pos.CENTER_RIGHT
            it.padding = Insets(Gap, Gap / 2, Gap / 2, 0.0)
        }
        groupTab.content = gBorderPane

        // ----- Mode ----- //
        initModeTab()
        mGridPane.padding = Insets(Gap)
        mGridPane.vgap = Gap
        mGridPane.hgap = Gap
        mGridPane.alignment = Pos.TOP_CENTER
        modeTab.content = mGridPane

        // ----- Label ----- //
        initLabelTab()
        lGridPane.padding = Insets(Gap, Gap, 0.0, Gap)
        lGridPane.vgap = Gap
        lGridPane.hgap = Gap
        lGridPane.alignment = Pos.CENTER
        labelTab.content = lGridPane

        // ----- Tab ----- //
        tabPane.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
        tabPane.prefHeight = 400.0
        tabPane.prefWidth = 600.0
        tabPane.tabs.addAll(groupTab, modeTab, labelTab)

        initProperties()

        this.title = I18N["settings.title"]
        this.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
        this.dialogPane.content = tabPane
    }

    // ----- Group ----- //
    private fun initGroupTab() {
        val nameList = Settings[Settings.DefaultGroupNameList].asStringList()
        val colorList = Settings[Settings.DefaultGroupColorList].asStringList()
        val createList = Settings[Settings.IsGroupCreateOnNewTrans].asBooleanList()

        for (i in nameList.indices) createGroupRow(i, createList[i], nameList[i], colorList[i])
    }
    private fun createGroupRow(groupId: Int, createOnNew: Boolean = false, name: String = "", color: String = "") {
        val newRowIndex = groupId + gRowShift

        if (gRemainGroup == 0) {
            gGridPane.add(gLabelName, 0, 0)
            gGridPane.add(gLabelColor, 1, 0)
            gGridPane.add(gLabelIsCreate, 2, 0)
        }
        gRemainGroup++

        val defaultColorList = Settings[Settings.DefaultGroupColorList].asStringList()
        val colorHex = if (isColorHex(color)) color else defaultColorList[groupId % defaultColorList.size]

        val checkBox = CheckBox().also { it.isSelected = createOnNew }
        val textField = TextField(name).also { it.textFormatter = getGroupNameFormatter() }
        val colorPicker = CColorPicker(Color.web(colorHex))
        val button = Button(I18N["common.delete"]).also {
            it.setOnAction { _ ->
                removeGroupRow(GridPane.getRowIndex(it) - gRowShift)
            }
        }

        checkBox.disableProperty().bind(textField.textProperty().isEmpty)
        textField.textProperty().addListener { _ ,_ ,newValue -> if (newValue.isEmpty()) checkBox.isSelected = false }

        //   0       1             2        3
        // 0 Name    Color         isCreate
        // 1 _______ | 66CCFF  V |  X       Delete
        gGridPane.add(textField, 0, newRowIndex)
        gGridPane.add(colorPicker, 1, newRowIndex)
        gGridPane.add(checkBox, 2, newRowIndex)
        gGridPane.add(button, 3, newRowIndex)
    }
    private fun removeGroupRow(groupId: Int) {
        val toRemoveRow = groupId + gRowShift
        val toRemoveList = ArrayList<Node>()
        for (node in gGridPane.children) {
            val row = GridPane.getRowIndex(node) ?: 0
            if (row == toRemoveRow) toRemoveList.add(node)
            if (row > toRemoveRow) GridPane.setRowIndex(node, row - 1)
        }
        gGridPane.children.removeAll(toRemoveList)

        gRemainGroup--
        if (gRemainGroup == 0) {
            gGridPane.children.removeAll(gLabelIsCreate, gLabelName, gLabelColor)
        }
    }

    // ----- Mode ----- //
    private fun initModeTab() {
        val viewModeList = listOf(ViewMode.IndexMode, ViewMode.GroupMode)

        mComboInput.setList(viewModeList)
        mComboInput.isWrapped = true
        mComboLabel.setList(viewModeList)
        mComboLabel.isWrapped = true
        mComboScale.setList(listOf(
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
        lCLabel.radiusProperty.addListener { _, _, _ ->

            val limitX = SAMPLE_IMAGE.width - lCLabel.prefWidth - 2 * lLabelPaneBorderWidth
            val limitY = SAMPLE_IMAGE.height - lCLabel.prefHeight - 2 * lLabelPaneBorderWidth
            if (lCLabel.anchorPaneLeft > limitX) lCLabel.anchorPaneLeft = limitX
            if (lCLabel.anchorPaneTop > limitY) lCLabel.anchorPaneTop = limitY
        }

        lLabelPane.children.add(lCLabel)

        lLabelRadius.textFormatter = TextFormatter { change ->
            if (change.isAdded) {
                val builder = StringBuilder()
                for (c in change.text.toCharArray()) {
                    if ((c in '0'..'9') || (c == '.' && !lLabelRadius.fieldText.contains(c))) {
                        builder.append(c)
                    }
                }
                change.text = builder.toString()
            }
            change
        }
        lLabelRadius.setOnChangeFinish {
            lSliderRadius.value = it.toDouble()
        }
        lSliderRadius.valueProperty().addListener { _, _, newValue ->
            lLabelRadius.text = String.format("%05.2f", newValue as Double)
            lCLabel.radius = newValue
        }
        lSliderRadius.min = 8.0
        lSliderRadius.max = 48.0

        lLabelAlpha.textFormatter = TextFormatter { change ->
            if (change.isAdded) {
                if (lLabelAlpha.fieldText.length == 2) {
                    change.text = ""
                    return@TextFormatter change
                }

                val builder = StringBuilder()
                for (c in change.text.uppercase().toCharArray()) {
                    if (c in '0'..'9' || c in 'A'..'F') {
                        builder.append(c)
                    }
                }
                change.text = builder.toString()

                if (lLabelAlpha.fieldText.length + change.text.length > 2) {
                    change.text = ""
                }
            }
            change
        }
        lLabelAlpha.setOnChangeStart {
            lLabelAlpha.fieldText = it.replace("0x", "")
        }
        lLabelAlpha.setOnChangeFinish {
            lSliderAlpha.value = it.toInt(16) / 255.0
        }
        lSliderAlpha.valueProperty().addListener { _, _, newValue ->
            val str = (newValue as Double * 255.0).toInt().toString(16).uppercase()
            lLabelAlpha.labelText = if (str.length == 1) "0x0$str" else "0x$str"
            lLabelAlpha.fieldText = str
            lCLabel.color = if (str.length == 1) Color.web("FF00000$str") else Color.web("FF0000$str")
        }
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
        gRemainGroup = 0
        gGridPane.children.clear()
        initGroupTab()

        // Mode
        val preferenceStringList = Settings[Settings.ViewModePreference].asStringList()
        val preferenceList = List(preferenceStringList.size) { ViewMode.getMode(preferenceStringList[it]) }
        mComboInput.moveTo(preferenceList[0])
        mComboLabel.moveTo(preferenceList[1])
        mComboScale.moveTo(Settings[Settings.ScaleOnNewPicture].asInteger())

        // Label
        lCLabel.anchorPaneLeft = (lLabelPane.prefWidth - lCLabel.prefWidth) / 2
        lCLabel.anchorPaneTop = (lLabelPane.prefHeight - lCLabel.prefHeight) / 2
        lLabelRadius.isEditing = false
        lLabelAlpha.isEditing = false
        lSliderRadius.value = Settings[Settings.LabelRadius].asDouble()
        lSliderAlpha.value = Settings[Settings.LabelAlpha].asInteger(16) / 255.0
    }

    // ----- Result convert ---- //
    override fun convertResult(): List<CProperty> {
        val list = ArrayList<CProperty>()

        list.addAll(convertGroup())
        list.addAll(convertMode())
        list.addAll(convertLabel())

        Logger.info("Generated settings", "SettingsDialog")
        Logger.debug("got", list, "SettingsDialog")

        return list
    }
    private fun convertGroup(): List<CProperty> {
        val list = ArrayList<CProperty>()

        val nameList = MutableList(gRemainGroup) { "" }
        val colorList = MutableList(gRemainGroup) { "" }
        val isCreateList = MutableList(gRemainGroup) { false }
        for (node in gGridPane.children) {
            val groupId = GridPane.getRowIndex(node) - gRowShift
            when (node) {
                is CheckBox -> isCreateList[groupId] = node.isSelected
                is TextField -> nameList[groupId] = node.text
                is ColorPicker -> colorList[groupId] = node.value.toHex()
            }
        }

        list.add(CProperty(Settings.DefaultGroupNameList, nameList))
        list.add(CProperty(Settings.DefaultGroupColorList, colorList))
        list.add(CProperty(Settings.IsGroupCreateOnNewTrans, isCreateList))

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
}