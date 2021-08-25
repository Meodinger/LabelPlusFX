package info.meodinger.lpfx.component

import info.meodinger.lpfx.ViewMode
import info.meodinger.lpfx.getViewMode
import info.meodinger.lpfx.options.Settings
import info.meodinger.lpfx.util.color.isColorHex
import info.meodinger.lpfx.util.getGroupNameFormatter
import javafx.beans.binding.Bindings

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Window

/**
 * Author: Meodinger
 * Date: 2021/8/25
 * Location: info.meodinger.lpfx.component
 */
class CSettingsDialog(owner: Window?) : Dialog<ButtonType>() {

    companion object {
        const val Gap = 16.0
        const val RowShift = 1
    }

    private val tabPane = TabPane()

    private var remainGroup = 0
    private val groupTab = Tab("Groups")
    private val gBorderPane = BorderPane()
    private val gLabelIsCreate = Label("is Create On New")
    private val gLabelName = Label("Group Name")
    private val gLabelColor = Label("Group Color")
    private val gGridPane = GridPane()
    private val gButtonAdd = Button("Add Group")

    private val modeTab = Tab("Mode")
    private val mLabelWork = Label("Work Mode")
    private val mLabelView = Label("View Mode")
    private val mLabelInput = Label("Input Mode")
    private val mLabelLabel = Label("Label Mode")
    private val mComboInput = CComboBox<ViewMode>()
    private val mComboLabel = CComboBox<ViewMode>()
    private val mGridPane = GridPane()

    init {
        initOwner(owner)

        // ----- Group ----- //
        initGroupBox()
        gGridPane.padding = Insets(Gap, Gap, Gap, 0.0)
        gGridPane.vgap = Gap
        gGridPane.hgap = Gap
        gGridPane.alignment = Pos.TOP_CENTER
        gButtonAdd.setOnAction { createGroupBox(remainGroup) }
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
        initModeBox()
        mGridPane.padding = Insets(Gap, Gap, Gap, 0.0)
        mGridPane.vgap = Gap
        mGridPane.hgap = Gap
        mGridPane.alignment = Pos.TOP_CENTER
        modeTab.content = mGridPane

        // ----- Tab ----- //
        tabPane.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
        tabPane.prefHeight = 400.0
        tabPane.prefWidth = 600.0
        tabPane.tabs.addAll(groupTab, modeTab)

        this.title = "Settings"
        this.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL, ButtonType.APPLY)
        this.dialogPane.content = tabPane
    }

    // ----- Group ----- //
    private fun initGroupBox() {
        val nameList = Settings[Settings.DefaultGroupList].asStringList()
        val colorList = Settings[Settings.DefaultColorList].asStringList()
        val createList = Settings[Settings.IsCreateOnNewTrans].asBooleanList()

        for (i in nameList.indices) createGroupBox(i, createList[i], nameList[i], colorList[i])
    }
    private fun createGroupBox(groupId: Int, createOnNew: Boolean = false, name: String = "", color: String = "") {
        val newRowIndex = groupId + RowShift

        if (remainGroup == 0) {
            gGridPane.add(gLabelIsCreate, 0, 0)
            gGridPane.add(gLabelName, 1, 0)
            gGridPane.add(gLabelColor, 2, 0)
        }
        remainGroup++

        val defaultColorList = Settings[Settings.DefaultColorList].asStringList()
        val colorHex = if (isColorHex(color)) color else defaultColorList[groupId % defaultColorList.size]

        val checkBox = CheckBox().also { it.isSelected = createOnNew }
        val textField = TextField(name).also { it.textFormatter = getGroupNameFormatter() }
        val colorPicker = CColorPicker(Color.web(colorHex))
        val button = Button("Delete").also { it.setOnAction { _ -> removeGroupBox(GridPane.getRowIndex(it) - RowShift) } }

        gGridPane.add(checkBox, 0, newRowIndex)
        gGridPane.add(textField, 1, newRowIndex)
        gGridPane.add(colorPicker, 2, newRowIndex)
        gGridPane.add(button, 3, newRowIndex)
    }
    private fun removeGroupBox(groupId: Int) {
        val toRemoveRow = groupId + RowShift
        val toRemoveList = ArrayList<Node>()
        for (node in gGridPane.children) {
            val row = GridPane.getRowIndex(node) ?: 0
            if (row == toRemoveRow) toRemoveList.add(node)
            if (row > toRemoveRow) GridPane.setRowIndex(node, row - 1)
        }
        gGridPane.children.removeAll(toRemoveList)

        remainGroup--
        if (remainGroup == 0) {
            gGridPane.children.removeAll(gLabelIsCreate, gLabelName, gLabelColor)
        }
    }

    // ----- Mode ----- //
    private fun initModeBox() {
        val a = Settings
        val preferenceStringList = Settings[Settings.ViewModePreference].asStringList()
        val preferenceList = List(preferenceStringList.size) { getViewMode(preferenceStringList[it]) }
        val viewModeList = listOf(ViewMode.IndexMode, ViewMode.GroupMode)

        mComboInput.setList(viewModeList)
        mComboInput.moveTo(preferenceList[0])
        mComboLabel.setList(viewModeList)
        mComboLabel.moveTo(preferenceList[1])

        mGridPane.add(mLabelWork, 0, 0)
        mGridPane.add(mLabelView, 1, 0)
        mGridPane.add(mLabelInput, 0, 1)
        mGridPane.add(mComboInput, 1, 1)
        mGridPane.add(mComboLabel, 1, 2)
        mGridPane.add(mLabelLabel, 0, 2)
    }
}