package info.meodinger.lpfx.component

import info.meodinger.lpfx.options.Settings
import info.meodinger.lpfx.util.getGroupNameFormatter
import javafx.geometry.Insets

import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.stage.Window

/**
 * Author: Meodinger
 * Date: 2021/8/25
 * Location: info.meodinger.lpfx.component
 */
class CSettings(owner: Window?) : Dialog<ButtonType>() {

    private val tabPane = TabPane()

    private val groupTab = Tab("Groups")
    private val groupGridPane = GridPane()

    private val modeTab = Tab("Mode")
    private val modeGridPane = GridPane()

    init {
        initOwner(owner)

        groupGridPane.add(Label("is Create On New"), 0, 0)
        groupGridPane.add(Label("Group Name"), 1, 0)
        groupGridPane.add(Label("Group Color"), 2, 0)
        initGroupBox()
        groupGridPane.padding = Insets(16.0, 16.0, 16.0, 0.0)
        groupGridPane.vgap = 16.0
        groupGridPane.hgap = 16.0
        groupGridPane.alignment = Pos.TOP_CENTER
        groupTab.content = groupGridPane

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

        for (i in nameList.indices) createGroupBox(createList[i], nameList[i], colorList[i])
    }
    private fun createGroupBox(createOnNew: Boolean = false, name: String = "", color: String = "") {
        val newRowIndex = groupGridPane.rowCount
        groupGridPane.add(CheckBox().also { it.isSelected = createOnNew }, 0, newRowIndex)
        groupGridPane.add(TextField(name).also { it.textFormatter = getGroupNameFormatter() }, 1, newRowIndex)
        groupGridPane.add(CColorPicker(color), 2, newRowIndex)
    }
    private fun removeGroupBox(groupInt: Int) {
       groupGridPane.rowConstraints.removeAt(groupInt + 2)
    }
}