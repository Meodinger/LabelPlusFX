package ink.meodinger.lpfx.component.singleton

import ink.meodinger.lpfx.State
import ink.meodinger.lpfx.ViewMode
import ink.meodinger.lpfx.component.common.CColorPicker
import ink.meodinger.lpfx.component.CTreeLabelItem
import ink.meodinger.lpfx.component.CTreeView
import ink.meodinger.lpfx.getGroupNameFormatter
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.util.color.toHex
import ink.meodinger.lpfx.util.dialog.showChoice
import ink.meodinger.lpfx.util.dialog.showConfirm
import ink.meodinger.lpfx.util.dialog.showError
import ink.meodinger.lpfx.util.dialog.showInput
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get

import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Circle


/**
 * Author: Meodinger
 * Date: 2021/8/30
 * Have fun with my code!
 */

/**
 * A ContextMenu Singleton for CTreeView
 */
object ATreeMenu : ContextMenu() {

    private lateinit var view: CTreeView

    private val r_addGroupField = TextField()
    private val r_addGroupPicker = CColorPicker()
    private val r_addGroupDialog = Dialog<TransGroup>()
    private val r_addGroupAction = {
        val nameList = Settings[Settings.DefaultGroupNameList].asStringList()
        var colorHexList = Settings[Settings.DefaultGroupColorHexList].asStringList()
        if (colorHexList.isEmpty()) colorHexList = TransFile.Companion.LPTransFile.DEFAULT_COLOR_HEX_LIST

        val newGroupId = State.transFile.groupCount
        var newName = String.format(I18N["context.add_group.new_group.i"], newGroupId + 1)
        if (newGroupId < nameList.size && nameList[newGroupId].isNotEmpty()) {
            if (!State.transFile.groupNames.contains(nameList[newGroupId])) {
                newName = nameList[newGroupId]
            }
        }

        r_addGroupField.text = newName
        r_addGroupPicker.value = Color.web(colorHexList[newGroupId % colorHexList.size])
        r_addGroupDialog.result = null
        r_addGroupDialog.showAndWait().ifPresent { newGroup ->
            if (State.transFile.groupNames.contains(newGroup.name)) {
                showError(I18N["context.error.same_group_name"], State.stage)
                return@ifPresent
            }

            // Edit data
            State.addTransGroup(newGroup)
            // Update view
            State.controller.createLabelLayer()
            State.controller.createGroupBarItem(newGroup)
            when (State.viewMode) {
                ViewMode.IndexMode -> State.controller.registerGroup(newGroup)
                ViewMode.GroupMode -> State.controller.createGroupTreeItem(newGroup)
            }
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
            if (State.transFile.groupNames.contains(newName)) {
                showError(I18N["context.error.same_group_name"], State.stage)
                return@ifPresent
            }

            val groupId = State.transFile.getGroupIdByName(groupItem.value)

            // Edit data
            State.setTransGroupName(groupId, newName)
            // Mark change
            State.isChanged = true
        }
    }
    private val g_renameItem = MenuItem(I18N["context.rename_group"])
    private val g_changeColorPicker = CColorPicker()
    private val g_changeColorAction = { groupItem: TreeItem<String> ->
        val newColor = g_changeColorPicker.value

        val groupId = State.transFile.getGroupIdByName(groupItem.value)

        // Edit data
        State.setTransGroupColor(groupId, newColor.toHex())
        // Mark change
        State.isChanged = true
    }
    private val g_changeColorItem = MenuItem()
    private val g_deleteAction = { groupItem: TreeItem<String> ->
        val groupName = groupItem.value
        val groupId = State.transFile.getGroupIdByName(groupName)

        view.selectionModel.clearSelection()

        // Edit data
        for (key in State.transFile.picNames) for (label in State.transFile.getTransList(key)) {
            if (label.groupId >= groupId) State.setTransLabelGroup(key, label.index, label.groupId - 1)
        }
        State.removeTransGroup(groupName)
        // Update view
        State.controller.removeLabelLayer(groupId)
        State.controller.removeGroupBarItem(groupName)
        State.controller.removeGroupTreeItem(groupName)
        // Mark change
        State.isChanged = true
    }
    private val g_deleteItem = MenuItem(I18N["context.delete_group"])

    private val l_moveToAction = { items: List<TreeItem<String>> ->
        showChoice(
            State.stage,
            I18N["context.move_to.dialog.title"],
            if (items.size == 1) I18N["context.move_to.dialog.header"] else I18N["context.move_to.dialog.header.pl"],
            State.transFile.groupNames
        ).ifPresent { newGroupName ->
            val newGroupId = State.transFile.getGroupIdByName(newGroupName)

            for (item in items) {
                val labelIndex = (item as CTreeLabelItem).index
                val groupId = State.transFile.getTransLabel(State.currentPicName, labelIndex).groupId

                // Edit data
                State.setTransLabelGroup(State.currentPicName, labelIndex, newGroupId)

                // Update view
                State.controller.moveLabelTreeItem(labelIndex, groupId, newGroupId)
            }
            // Mark change
            State.isChanged = true
        }
    }
    private val l_moveToItem = MenuItem(I18N["context.move_to"])
    private val l_deleteAction = { items: List<TreeItem<String>> ->
        val confirm = showConfirm(
            I18N["context.delete_label.dialog.title"],
            if (items.size == 1) I18N["context.delete_label.dialog.header"] else I18N["context.delete_label.dialog.header.pl"],
            StringBuilder().also { for (item in items) it.appendLine(item.value) }.toString(),
            State.stage
        )

        if (confirm.isPresent && confirm.get() == ButtonType.YES) {
            view.selectionModel.clearSelection()

            for (item in items) {
                val labelIndex = (item as CTreeLabelItem).index

                // Update view
                State.controller.removeLabelTreeItem(labelIndex) // need old data
                State.controller.removeLabel(labelIndex) // need old data

                // Edit data
                State.removeTransLabel(State.currentPicName, labelIndex)
                for (label in State.transFile.getTransList(State.currentPicName)) {
                    if (label.index > labelIndex) {
                        State.setTransLabelIndex(State.currentPicName, label.index, label.index - 1)
                    }
                }
            }
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

    fun initView(view: CTreeView) {
        this.view = view
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
            else if (item is CTreeLabelItem) labelCount += 1
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
            g_deleteItem.isDisable = !State.transFile.isGroupUnused(groupItem.value)
            g_deleteItem.setOnAction { g_deleteAction(groupItem) }

            items.add(g_renameItem)
            items.add(g_changeColorItem)
            items.add(SeparatorMenuItem())
            items.add(g_deleteItem)
        } else if (rootCount == 0 && groupCount == 0 && labelCount > 0) {
            // label(s)
            // NOTE: toList() to make a not observable copy
            l_moveToItem.setOnAction { l_moveToAction(selectedItems.toList()) }
            l_deleteItem.setOnAction { l_deleteAction(selectedItems.toList()) }

            items.add(l_moveToItem)
            items.add(SeparatorMenuItem())
            items.add(l_deleteItem)
        } else {
            // other
        }
    }

}
