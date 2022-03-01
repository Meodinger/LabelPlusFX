package ink.meodinger.lpfx.component.singleton

import ink.meodinger.lpfx.NOT_FOUND
import ink.meodinger.lpfx.State
import ink.meodinger.lpfx.component.CTreeGroupItem
import ink.meodinger.lpfx.component.CTreeLabelItem
import ink.meodinger.lpfx.component.common.CColorPicker
import ink.meodinger.lpfx.genGroupNameFormatter
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.util.color.toHexRGB
import ink.meodinger.lpfx.util.component.withContent
import ink.meodinger.lpfx.util.dialog.showChoice
import ink.meodinger.lpfx.util.dialog.showConfirm
import ink.meodinger.lpfx.util.dialog.showError
import ink.meodinger.lpfx.util.dialog.showInput
import ink.meodinger.lpfx.util.doNothing
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get

import javafx.beans.binding.Bindings
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.paint.Color


/**
 * Author: Meodinger
 * Date: 2021/8/30
 * Have fun with my code!
 */

/**
 * A ContextMenu Singleton for CTreeView
 */
object ATreeMenu : ContextMenu() {

    private val r_addGroupField      = TextField().apply {
        textFormatter = genGroupNameFormatter()
    }
    private val r_addGroupPicker     = CColorPicker().apply {
        hide()
    }
    private val r_addGroupDialog     = Dialog<TransGroup>().apply {
        title = I18N["context.add_group.dialog.title"]
        headerText = I18N["context.add_group.dialog.header"]
        dialogPane.buttonTypes.addAll(ButtonType.FINISH, ButtonType.CANCEL)
        withContent(HBox(r_addGroupField, r_addGroupPicker)) { alignment = Pos.CENTER }

        setResultConverter converter@{
            return@converter when (it) {
                ButtonType.FINISH -> TransGroup(r_addGroupField.text, r_addGroupPicker.value.toHexRGB())
                else -> null
            }
        }
    }
    private val r_addGroupHandler    = EventHandler<ActionEvent> {
        if (r_addGroupDialog.owner == null) r_addGroupDialog.initOwner(State.stage)

        val nameList = Settings.defaultGroupNameList
        val colorHexList = Settings.defaultGroupColorHexList.ifEmpty { TransFile.Companion.LPTransFile.DEFAULT_COLOR_HEX_LIST }

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
                showError(State.stage, I18N["context.error.same_group_name"])
                return@ifPresent
            }

            // Edit data
            State.addTransGroup(newGroup)
            // Mark change
            State.isChanged = true
        }
    }
    private val r_addGroupItem       = MenuItem(I18N["context.add_group"]).apply {
        onAction = r_addGroupHandler
    }

    private val g_renameHandler      = EventHandler<ActionEvent> {
        val groupName: String = it.source as String

        showInput(
            State.stage,
            I18N["context.rename_group.dialog.title"],
            I18N["context.rename_group.dialog.header"],
            groupName,
            genGroupNameFormatter()
        ).ifPresent { newName ->
            if (newName.isBlank()) return@ifPresent
            if (State.transFile.groupNames.contains(newName)) {
                showError(State.stage, I18N["context.error.same_group_name"])
                return@ifPresent
            }

            // Edit data
            State.setTransGroupName(State.transFile.getGroupIdByName(groupName), newName)
            // Mark change
            State.isChanged = true
        }
    }
    private val g_renameItem         = MenuItem(I18N["context.rename_group"])

    private val g_changeColorPicker  = CColorPicker().apply {
        setPrefSize(40.0, 20.0)
    }
    private val g_changeColorHandler = EventHandler<ActionEvent> {
        val groupName = it.source as String
        val newColor = (it.target as ColorPicker).value

        // Edit data
        State.setTransGroupColor(State.transFile.getGroupIdByName(groupName), newColor.toHexRGB())
        // Mark change
        State.isChanged = true
    }
    private val g_changeColorItem    = MenuItem().apply {
        graphic = g_changeColorPicker
        textProperty().bind(Bindings.createStringBinding(
            { g_changeColorPicker.value.toHexRGB() },
            g_changeColorPicker.valueProperty()
        ))
    }

    private val g_deleteHandler      = EventHandler<ActionEvent> {
        val groupName = it.source as String

        // Edit data
        State.removeTransGroup(groupName)
        // Mark change
        State.isChanged = true
    }
    private val g_deleteItem         = MenuItem(I18N["context.delete_group"])

    private val l_moveToHandler      = EventHandler<ActionEvent> {
        val items = it.source as List<*>

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
    private val l_moveToItem         = MenuItem(I18N["context.move_to"])

    private val l_deleteHandler      = EventHandler<ActionEvent> {
        val items = it.source as List<*>

        val confirm = showConfirm(
            State.stage,
            if (items.size == 1) I18N["context.delete_label.dialog.header"] else I18N["context.delete_label.dialog.header.pl"],
            StringBuilder().apply { for (item in items) appendLine((item as CTreeLabelItem).text) }.toString(),
            I18N["context.delete_label.dialog.title"]
        )

        if (confirm.isPresent && confirm.get() == ButtonType.YES) {
            for (item in items) {
                val labelIndex = (item as CTreeLabelItem).index

                // Edit data
                State.removeTransLabel(State.currentPicName, labelIndex)
                if (State.currentLabelIndex == labelIndex) State.currentLabelIndex = NOT_FOUND
            }
            // Mark change
            State.isChanged = true
        }
    }
    private val l_deleteItem         = MenuItem(I18N["context.delete_label"])

    fun update(selectedItems: List<TreeItem<String>>) {
        items.clear()

        if (selectedItems.isEmpty()) return

        var rootCount = 0
        var groupCount = 0
        var labelCount = 0

        for (item in selectedItems) {
            if (item.parent == null) rootCount += 1
            else if (item is CTreeLabelItem) labelCount += 1
            else if (item is CTreeGroupItem) groupCount += 1
            else doNothing()
        }

        if (rootCount == 1 && groupCount == 0 && labelCount == 0) {
            // root
            items.add(r_addGroupItem)
        } else if (rootCount == 0 && groupCount == 1 && labelCount == 0) {
            // group
            val groupItem = selectedItems[0] as CTreeGroupItem
            val groupName = groupItem.name

            g_changeColorPicker.value = groupItem.color
            g_deleteItem.isDisable = !State.transFile.isGroupUnused(State.transFile.getGroupIdByName(groupItem.value))

            g_renameItem.setOnAction { g_renameHandler.handle(ActionEvent(groupName, g_renameItem)) }
            g_changeColorPicker.setOnAction { g_changeColorHandler.handle(ActionEvent(groupName, g_changeColorPicker)) }
            g_deleteItem.setOnAction { g_deleteHandler.handle(ActionEvent(groupName, g_deleteItem)) }

            items.add(g_renameItem)
            items.add(g_changeColorItem)
            items.add(SeparatorMenuItem())
            items.add(g_deleteItem)
        } else if (rootCount == 0 && groupCount == 0 && labelCount > 0) {
            // label(s)
            l_moveToItem.setOnAction { l_moveToHandler.handle(ActionEvent(selectedItems, l_moveToItem)) }
            l_deleteItem.setOnAction { l_deleteHandler.handle(ActionEvent(selectedItems, l_deleteItem)) }

            items.add(l_moveToItem)
            items.add(SeparatorMenuItem())
            items.add(l_deleteItem)
        } else {
            // other
            doNothing()
        }
    }

    fun toggleGroupCreate() {
        r_addGroupItem.fire()
    }
    fun toggleGroupRename(groupName: String) {
        g_renameItem.onAction.handle(ActionEvent(groupName, null))
    }
    fun toggleGroupDelete(groupName: String) {
        g_deleteItem.onAction.handle(ActionEvent(groupName, null))
    }

}
