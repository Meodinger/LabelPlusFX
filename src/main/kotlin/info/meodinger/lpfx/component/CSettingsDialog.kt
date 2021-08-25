package info.meodinger.lpfx.component

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
    private val groupPane = BorderPane()
    private val groupLabelIsCreate = Label("is Create On New")
    private val groupLabelName = Label("Group Name")
    private val groupLabelColor = Label("Group Color")
    private val groupGridPane = GridPane()
    private val groupButtonAdd = Button("Add Group")

    private val modeTab = Tab("Mode")
    private val modeGridPane = GridPane()

    init {
        initOwner(owner)

        initGroupBox()
        groupGridPane.padding = Insets(Gap, Gap, Gap, 0.0)
        groupGridPane.vgap = Gap
        groupGridPane.hgap = Gap
        groupGridPane.alignment = Pos.TOP_CENTER
        groupButtonAdd.setOnAction { createGroupBox(remainGroup) }
        val scrollPane = ScrollPane().also {
            it.style = "-fx-background-color:transparent;"
        }
        val stackPane = StackPane(groupGridPane).also {
            it.prefWidthProperty().bind(Bindings.createDoubleBinding(
                { scrollPane.viewportBounds.width },
                scrollPane.viewportBoundsProperty())
            )
        }
        groupPane.center = scrollPane.also { it.content = stackPane }
        groupPane.bottom = HBox(groupButtonAdd).also { it.alignment = Pos.CENTER_RIGHT }
        groupTab.content = groupPane

        tabPane.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
        tabPane.prefHeight = 400.0
        tabPane.prefWidth = 600.0
        tabPane.tabs.addAll(groupTab, modeTab)

        this.title = "Settings"
        this.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL, ButtonType.APPLY)
        this.dialogPane.content = tabPane
    }

    private fun initGroupBox() {
        val nameList = Settings[Settings.DefaultGroupList].asStringList()
        val colorList = Settings[Settings.DefaultColorList].asStringList()
        val createList = Settings[Settings.CreateOnNewTrans].asBooleanList()

        for (i in nameList.indices) createGroupBox(i, createList[i], nameList[i], colorList[i])
    }
    private fun createGroupBox(groupId: Int, createOnNew: Boolean = false, name: String = "", color: String = "") {
        val newRowIndex = groupId + RowShift

        if (remainGroup == 0) {
            groupGridPane.add(groupLabelIsCreate, 0, 0)
            groupGridPane.add(groupLabelName, 1, 0)
            groupGridPane.add(groupLabelColor, 2, 0)
        }
        remainGroup++

        val defaultColorList = Settings[Settings.DefaultColorList].asStringList()
        val colorHex = if (isColorHex(color)) color else defaultColorList[groupId % defaultColorList.size]

        val checkBox = CheckBox().also { it.isSelected = createOnNew }
        val textField = TextField(name).also { it.textFormatter = getGroupNameFormatter() }
        val colorPicker = CColorPicker(Color.web(colorHex))
        val button = Button("Delete").also { it.setOnAction { _ -> removeGroupBox(GridPane.getRowIndex(it) - RowShift) } }

        groupGridPane.add(checkBox, 0, newRowIndex)
        groupGridPane.add(textField, 1, newRowIndex)
        groupGridPane.add(colorPicker, 2, newRowIndex)
        groupGridPane.add(button, 3, newRowIndex)
    }
    private fun removeGroupBox(groupId: Int) {
        val toRemoveRow = groupId + RowShift
        val toRemoveList = ArrayList<Node>()
        for (node in groupGridPane.children) {
            val row = GridPane.getRowIndex(node) ?: 0
            if (row == toRemoveRow) toRemoveList.add(node)
            if (row > toRemoveRow) GridPane.setRowIndex(node, row - 1)
        }
        groupGridPane.children.removeAll(toRemoveList)

        remainGroup--
        if (remainGroup == 0) {
            groupGridPane.children.removeAll(groupLabelIsCreate, groupLabelName, groupLabelColor)
        }
    }
}