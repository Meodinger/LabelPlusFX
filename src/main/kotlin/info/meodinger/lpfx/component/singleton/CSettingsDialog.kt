package info.meodinger.lpfx.component.singleton

import info.meodinger.lpfx.State
import info.meodinger.lpfx.ViewMode
import info.meodinger.lpfx.component.CColorPicker
import info.meodinger.lpfx.component.CComboBox
import info.meodinger.lpfx.options.CProperty
import info.meodinger.lpfx.options.Logger
import info.meodinger.lpfx.options.Settings
import info.meodinger.lpfx.util.color.isColorHex
import info.meodinger.lpfx.util.color.toHex
import info.meodinger.lpfx.util.getGroupNameFormatter
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get

import javafx.beans.binding.Bindings
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
object CSettingsDialog : Dialog<List<CProperty>>() {

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

    init {
        initOwner(State.stage)

        // ----- Group ----- //
        initGroupTab()
        gGridPane.padding = Insets(Gap)
        gGridPane.vgap = Gap
        gGridPane.hgap = Gap
        gGridPane.alignment = Pos.TOP_CENTER
        gButtonAdd.setOnAction { createGroupRow(gRemainGroup) }
        val scrollPane = ScrollPane().also {
            it.style = "-fx-background-color:transparent;"
        }
        val stackPane = StackPane(gGridPane).also {
            it.prefWidthProperty().bind(Bindings.createDoubleBinding(
                { scrollPane.viewportBounds.width },
                scrollPane.viewportBoundsProperty())
            )
        }
        gBorderPane.center = scrollPane.also { it.content = stackPane }
        gBorderPane.bottom = HBox(gButtonAdd).also { it.alignment = Pos.CENTER_RIGHT }
        groupTab.content = gBorderPane

        // ----- Mode ----- //
        initModeTab()
        mGridPane.padding = Insets(Gap)
        mGridPane.vgap = Gap
        mGridPane.hgap = Gap
        mGridPane.alignment = Pos.TOP_CENTER
        modeTab.content = mGridPane

        // ----- Tab ----- //
        tabPane.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
        tabPane.prefHeight = 400.0
        tabPane.prefWidth = 600.0
        tabPane.tabs.addAll(groupTab, modeTab)

        this.title = I18N["settings.title"]
        this.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
        this.dialogPane.content = tabPane
        this.setResultConverter {
            when (it) {
                ButtonType.OK -> convertResult()
                else -> emptyList()
            }
        }
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
            gGridPane.add(gLabelIsCreate, 0, 0)
            gGridPane.add(gLabelName, 1, 0)
            gGridPane.add(gLabelColor, 2, 0)
        }
        gRemainGroup++

        val defaultColorList = Settings[Settings.DefaultGroupColorList].asStringList()
        val colorHex = if (isColorHex(color)) color else defaultColorList[groupId % defaultColorList.size]

        val checkBox = CheckBox().also { it.isSelected = createOnNew }
        val textField = TextField(name).also { it.textFormatter = getGroupNameFormatter() }
        val colorPicker = CColorPicker(Color.web(colorHex))
        val button = Button("Delete").also { it.setOnAction { _ -> removeGroupRow(GridPane.getRowIndex(it) - gRowShift) } }

        checkBox.disableProperty().bind(textField.textProperty().isEmpty)
        textField.textProperty().addListener { _ ,_ ,newValue -> if (newValue.isEmpty()) checkBox.isSelected = false }

        gGridPane.add(checkBox, 0, newRowIndex)
        gGridPane.add(textField, 1, newRowIndex)
        gGridPane.add(colorPicker, 2, newRowIndex)
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
        val preferenceStringList = Settings[Settings.ViewModePreference].asStringList()
        val preferenceList = List(preferenceStringList.size) { ViewMode.getMode(preferenceStringList[it]) }
        val viewModeList = listOf(ViewMode.IndexMode, ViewMode.GroupMode)

        mComboInput.setList(viewModeList)
        mComboInput.moveTo(preferenceList[0])
        mComboInput.isWrapped = true
        mComboLabel.setList(viewModeList)
        mComboLabel.moveTo(preferenceList[1])
        mComboLabel.isWrapped = true

        mGridPane.add(Label(I18N["mode.work"]), 0, 0)
        mGridPane.add(Label(I18N["mode.view"]), 1, 0)
        mGridPane.add(Label(I18N["mode.work.input"]), 0, 1)
        mGridPane.add(Label(I18N["mode.work.label"]), 0, 2)
        mGridPane.add(mComboInput, 1, 1)
        mGridPane.add(mComboLabel, 1, 2)
    }

    // ----- Result convert ---- //
    private fun convertResult(): List<CProperty> {
        val list = ArrayList<CProperty>()

        list.addAll(convertGroup())
        list.addAll(convertMode())

        Logger.info("Generated settings", "SettingsDialog")
        Logger.debug("got", list)

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
                is CColorPicker -> colorList[groupId] = node.value.toHex()
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
}