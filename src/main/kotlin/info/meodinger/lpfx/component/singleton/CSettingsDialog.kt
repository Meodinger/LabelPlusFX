package info.meodinger.lpfx.component.singleton

import info.meodinger.lpfx.State
import info.meodinger.lpfx.ViewMode
import info.meodinger.lpfx.component.CComboBox
import info.meodinger.lpfx.component.CInputLabel
import info.meodinger.lpfx.component.CLabel
import info.meodinger.lpfx.options.CProperty
import info.meodinger.lpfx.options.Logger
import info.meodinger.lpfx.options.Settings
import info.meodinger.lpfx.util.color.isColorHex
import info.meodinger.lpfx.util.color.toHex
import info.meodinger.lpfx.util.getGroupNameFormatter
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
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

    private val labelTab = Tab(I18N["settings.label.title"])
    private val lGridPane = GridPane()
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
        gBorderPane.center = ScrollPane(gGridPane).also {
            it.style = "-fx-background-color:transparent;"
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
        lGridPane.padding = Insets(Gap)
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
        val colorPicker = ColorPicker(Color.web(colorHex)).also {
            /*
            it.setOnShown { _ ->
                val clazz = ComboBoxPopupControl::class.java
                val methods = clazz.declaredMethods
                for (method in methods) {
                    if (method.name == "getPopup") {
                        method.isAccessible = true
                        val popupControl = method.invoke(it.skin) as PopupControl
                        popupControl.isAutoHide = false
                        print("success")
                    }
                }
            }
             */
        }
        val button = Button(I18N["common.delete"]).also { it.setOnAction { _ -> removeGroupRow(GridPane.getRowIndex(it) - gRowShift) } }

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

        //   0         1
        // 0 WorkMode  ViewMode
        // 1 Input     | input | < >
        // 2 Label     | label | < >
        mGridPane.add(Label(I18N["mode.work"]), 0, 0)
        mGridPane.add(Label(I18N["mode.view"]), 1, 0)
        mGridPane.add(Label(I18N["mode.work.input"]), 0, 1)
        mGridPane.add(Label(I18N["mode.work.label"]), 0, 2)
        mGridPane.add(mComboInput, 1, 1)
        mGridPane.add(mComboLabel, 1, 2)
    }

    // ----- Label ----- //
    private fun initLabelTab() {
        val radius = Settings[Settings.LabelRadius].asDouble()
        val alpha = Settings[Settings.LabelAlpha].asString()
        val label = CLabel(8, radius, Color.RED.toHex() + alpha)

        lLabelPane.setPrefSize(200.0, 200.0)
        lLabelPane.children.add(label)
        lLabelPane.border = Border(BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, CornerRadii(0.0), BorderWidths(2.0)))

        val layout = {
            label.layoutX = lLabelPane.prefWidth / 2 - label.prefWidth / 2
            label.layoutY = lLabelPane.prefHeight / 2 - label.prefHeight / 2
        }.also { it.invoke() }
        label.radiusProperty.addListener { _, _, _ -> layout.invoke()}

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
            label.radius = newValue
        }
        lSliderRadius.min = 8.0
        lSliderRadius.max = 48.0

        lLabelAlpha.textFormatter = TextFormatter { change ->
            val fieldText = lLabelAlpha.fieldText
            val isHead0x = lLabelAlpha.fieldText.startsWith("0x")
            if (fieldText.length >= (if (isHead0x) 4 else 2)) {
                change.text = ""
                return@TextFormatter change
            }

            change.text = change.text.uppercase().replace(Regex("[^0-9A-F]"), "")
            if (change.isAdded) {
                val left = (if (isHead0x) 4 else 2) - fieldText.length
                change.text = change.text.substring(0, left.coerceAtMost(change.text.length))
            }

            change
        }
        lLabelAlpha.setOnChangeStart {
            lLabelAlpha.fieldText = it.replace("0x", "")
        }
        lLabelAlpha.setOnChangeFinish {
            lSliderAlpha.value = it.replace("0x", "").toInt(16) / 255.0
        }
        lSliderAlpha.valueProperty().addListener { _, _, newValue ->
            val str = (newValue as Double * 255.0).toInt().toString(16).uppercase()
            lLabelAlpha.text = if (str.length == 1) "0x0$str" else "0x$str"
            label.color = if (str.length == 1) "FF00000$str" else "FF0000$str"
        }
        lSliderAlpha.min = 0.0
        lSliderAlpha.max = 1.0

        // lGridPane.isGridLinesVisible = true
        //   0         1           2
        // 0 --------  Radius
        // 1 |      |  ----O------ 24.0
        // 2 |      |  Alpha
        // 3 |      |  ------O---- 0x80
        // 4 --------
        lGridPane.add(lLabelPane, 0, 0, 1, 5)
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

        // Label
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