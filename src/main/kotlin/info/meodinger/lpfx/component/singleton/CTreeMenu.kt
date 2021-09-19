package info.meodinger.lpfx.component.singleton

import info.meodinger.lpfx.State
import info.meodinger.lpfx.component.CColorPicker
import info.meodinger.lpfx.component.CTreeItem
import info.meodinger.lpfx.options.Settings
import info.meodinger.lpfx.type.TransGroup
import info.meodinger.lpfx.util.color.toHex
import info.meodinger.lpfx.util.dialog.showChoice
import info.meodinger.lpfx.util.dialog.showConfirm
import info.meodinger.lpfx.util.dialog.showError
import info.meodinger.lpfx.util.dialog.showInput
import info.meodinger.lpfx.util.getGroupNameFormatter
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get

import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Circle


/**
 * Author: Meodinger
 * Date: 2021/8/30
 * Location: info.meodinger.lpfx.component.singleton
 */

/**
 * A ContextMenu Singleton for CTreeView
 */
object CTreeMenu : ContextMenu() {

    private val r_addGroupField = TextField()
    private val r_addGroupPicker = CColorPicker()
    private val r_addGroupDialog = Dialog<TransGroup>()
    private val r_addGroupAction = {
        val nameList = Settings[Settings.DefaultGroupNameList].asStringList()
        val colorList = Settings[Settings.DefaultGroupColorList].asStringList()

        val newGroupId = State.transFile.groupList.size
        val newName =
            if (newGroupId < nameList.size) nameList[newGroupId]
            else String.format(I18N["context.add_group.new_group.format.i"], newGroupId + 1)
        val newColor =
            if (newGroupId < 9) Color.web(colorList[newGroupId])
            else Color.BLACK

        r_addGroupField.text = newName
        r_addGroupPicker.value = newColor
        r_addGroupDialog.result = null
        r_addGroupDialog.showAndWait().ifPresent { newGroup ->
            // Edit data
            State.addTransGroup(newGroup)
            // Update view
            State.controller.updateLabelColorList()
            State.controller.addLabelLayer()
            State.controller.updateGroupList()
            State.controller.addGroupItem(newGroup)
            // Mark change
            State.isChanged = true
        }
    }
    private val r_addGroupItem = MenuItem(I18N["context.add_group"])

    private val g_renameAction = { groupItem: TreeItem<String> ->
        showInput(
            State.stage,
            I18N["context.rename_group.dialog.title"],
            I18N["context.rename_group.dialog.header"],
            groupItem.value,
            getGroupNameFormatter()
        ).ifPresent { newName ->
            if (newName.isBlank()) return@ifPresent
            for (group in State.transFile.groupList) {
                if (group.name == newName) {
                    showError(I18N["error.same_group_name"])
                    return@ifPresent
                }
            }

            val groupId = State.transFile.getGroupIdByName(groupItem.value)
            val transGroup = State.transFile.getTransGroupAt(groupId)
            val oldName = transGroup.name

            // Edit data
            State.setTransGroupName(groupId, newName)
            // Update view
            State.controller.updateGroupList()
            State.controller.updateGroupItem(oldName, transGroup)
            // Mark change
            State.isChanged = true
        }
    }
    private val g_renameItem = MenuItem(I18N["context.rename_group"])
    private val g_changeColorPicker = CColorPicker()
    private val g_changeColorAction = { groupItem: TreeItem<String> ->
        val newColor = g_changeColorPicker.value

        val groupId = State.transFile.getGroupIdByName(groupItem.value)
        val transGroup = State.transFile.getTransGroupAt(groupId)

        // Edit data
        State.setTransGroupColor(groupId, newColor.toHex())
        // Update view
        State.controller.updateLabelColorList()
        State.controller.updateGroupItem(transGroup.name, transGroup)
        // Mark change
        State.isChanged = true
    }
    private val g_changeColorItem = MenuItem()
    private val g_deleteAction = { groupItem: TreeItem<String> ->
        val groupId = State.transFile.getGroupIdByName(groupItem.value)
        val transGroup = State.transFile.getTransGroupAt(groupId)

        // Edit data
        for (key in State.transFile.transMap.keys) for (label in State.transFile.getTransLabelListOf(key)) {
            if (label.groupId >= groupId) State.setTransLabelGroup(key, label.index, label.groupId - 1)
        }
        State.removeTransGroup(transGroup)
        // Update view
        State.controller.updateLabelColorList()
        State.controller.removeLabelLayer(groupId)
        State.controller.updateGroupList()
        State.controller.removeGroupItem(transGroup)
        // Mark change
        State.isChanged = true
    }
    private val g_deleteItem = MenuItem(I18N["context.delete_group"])

    private val l_moveToAction = { items: ObservableList<TreeItem<String>> ->
        val groupNameList = ArrayList<String>()
        for (group in State.transFile.groupList) groupNameList.add(group.name)

        showChoice(
            State.stage,
            I18N["context.move_to.dialog.title"],
            if (items.size == 1) I18N["context.move_to.dialog.header"] else I18N["context.move_to.dialog.header.pl"],
            groupNameList
        ).ifPresent { newGroupName ->
            val newGroupId = State.transFile.getGroupIdByName(newGroupName)

            // Edit data
            for (item in items) State.setTransLabelGroup(State.currentPicName, (item as CTreeItem).index, newGroupId)
            // Update view
            State.controller.updateTreeView()
            State.controller.updateLabelPane()
            // Mark change
            State.isChanged = true
        }
    }
    private val l_moveToItem = MenuItem(I18N["context.move_to"])
    private val l_deleteAction = { items: ObservableList<TreeItem<String>> ->
        val result = showConfirm(
            I18N["context.delete_label.dialog.title"],
            if (items.size == 1) I18N["context.delete_label.dialog.header"] else I18N["context.delete_label.dialog.header.pl"],
            StringBuilder().also { for (item in items) it.appendLine(item.value) }.toString(),
        )

        if (result.isPresent && result.get() == ButtonType.YES) {
            // Edit data
            for (item in items) {
                val transLabels = State.transFile.getTransLabelListOf(State.currentPicName)
                val transLabel = (item as CTreeItem).meta
                for (label in transLabels) if (label.index > transLabel.index) {
                    State.setTransLabelIndex(State.currentPicName, label.index, label.index - 1)
                }
                State.removeTransLabel(State.currentPicName, transLabel)
            }
            // Update view
            State.controller.updateTreeView()
            State.controller.updateLabelPane()
            // Mark change
            State.isChanged = true
        }
    }
    private val l_deleteItem = MenuItem(I18N["context.delete_label"])

    init {
        r_addGroupField.textFormatter = getGroupNameFormatter()
        r_addGroupPicker.hide()
        r_addGroupDialog.title = I18N["context.add_group.dialog.title"]
        r_addGroupDialog.headerText = I18N["context.add_group.dialog.header"]
        r_addGroupDialog.dialogPane.content = HBox(r_addGroupField, r_addGroupPicker).also { box -> box.alignment = Pos.CENTER }
        r_addGroupDialog.dialogPane.buttonTypes.addAll(ButtonType.FINISH, ButtonType.CANCEL)
        r_addGroupDialog.setResultConverter {
            if (it == ButtonType.FINISH)
                TransGroup(r_addGroupField.text, r_addGroupPicker.value.toHex())
            else
                null
        }

        g_changeColorPicker.valueProperty().addListener { _, _, newValue -> g_changeColorItem.text = newValue.toHex() }
        g_changeColorPicker.setPrefSize(40.0, 20.0)
        g_changeColorItem.graphic = g_changeColorPicker
    }

    fun update(selectedItems: ObservableList<TreeItem<String>>) {
        items.clear()

        r_addGroupItem.onAction = null
        g_renameItem.onAction = null
        g_changeColorPicker.onAction = null
        g_deleteItem.onAction = null
        l_moveToItem.onAction = null
        l_deleteItem.onAction = null

        var rootCount = 0
        var groupCount = 0
        var labelCount = 0

        if (selectedItems.size == 0) return
        for (item in selectedItems) {
            if (item.parent == null) rootCount += 1
            else if (item is CTreeItem) labelCount += 1
            else groupCount += 1
        }

        if (rootCount == 1 && groupCount == 0 && labelCount == 0) {
            // root
            items.add(r_addGroupItem)

            r_addGroupItem.setOnAction { r_addGroupAction() }
        } else if (rootCount == 0 && groupCount == 1 && labelCount == 0) {
            // group
            val groupItem = selectedItems[0]

            g_renameItem.setOnAction { g_renameAction(groupItem) }
            g_changeColorItem.text = g_changeColorPicker.value.toHex()
            g_changeColorPicker.value = (groupItem.graphic as Circle).fill as Color
            g_changeColorPicker.setOnAction { g_changeColorAction(groupItem) }
            g_deleteItem.isDisable = run {
                val thisGroupId = State.transFile.getGroupIdByName(groupItem.value)

                for (labels in State.transFile.transMap.values) for (label in labels)
                    if (label.groupId == thisGroupId) return@run true
                false
            }
            g_deleteItem.setOnAction { g_deleteAction(groupItem) }

            items.add(g_renameItem)
            items.add(g_changeColorItem)
            items.add(SeparatorMenuItem())
            items.add(g_deleteItem)
        } else if (rootCount == 0 && groupCount == 0 && labelCount > 0) {
            // label(s)
            l_moveToItem.setOnAction { l_moveToAction(selectedItems) }
            l_deleteItem.setOnAction { l_deleteAction(selectedItems) }

            items.add(l_moveToItem)
            items.add(SeparatorMenuItem())
            items.add(l_deleteItem)
        } else {
            // other
        }
    }

}