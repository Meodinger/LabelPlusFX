package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.NOT_FOUND
import ink.meodinger.lpfx.State
import ink.meodinger.lpfx.action.ActionType
import ink.meodinger.lpfx.action.ComplexAction
import ink.meodinger.lpfx.action.GroupAction
import ink.meodinger.lpfx.action.LabelAction
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
class CTreeMenu(private val state: State) : ContextMenu() {

    private val rAddGroupField      = TextField().apply {
        textFormatter = genGroupNameFormatter()
    }
    private val rAddGroupPicker     = CColorPicker().apply {
        hide()
    }
    private val rAddGroupDialog     = Dialog<TransGroup>().apply {
        title = I18N["context.add_group.dialog.title"]
        headerText = I18N["context.add_group.dialog.header"]
        dialogPane.buttonTypes.addAll(ButtonType.FINISH, ButtonType.CANCEL)
        withContent(HBox(rAddGroupField, rAddGroupPicker)) { alignment = Pos.CENTER }

        setResultConverter converter@{
            return@converter when (it) {
                ButtonType.FINISH -> TransGroup(rAddGroupField.text, rAddGroupPicker.value.toHexRGB())
                else -> null
            }
        }
    }
    private val rAddGroupHandler    = EventHandler<ActionEvent> {
        if (rAddGroupDialog.owner == null) rAddGroupDialog.initOwner(state.stage)

        val nameList = Settings.defaultGroupNameList
        val colorHexList = TransFile.Companion.LPTransFile.DEFAULT_COLOR_HEX_LIST

        val newGroupId = state.transFile.groupCount
        var newName = String.format(I18N["context.add_group.new_group.i"], newGroupId + 1)
        if (newGroupId < nameList.size && nameList[newGroupId].isNotEmpty()) {
            if (!state.transFile.groupNames.contains(nameList[newGroupId])) {
                newName = nameList[newGroupId]
            }
        }

        rAddGroupField.text = newName
        rAddGroupPicker.value = Color.web(colorHexList[newGroupId % colorHexList.size])
        rAddGroupDialog.result = null
        rAddGroupDialog.showAndWait().ifPresent { newGroup ->
            if (state.transFile.groupNames.contains(newGroup.name)) {
                showError(state.stage, I18N["context.error.same_group_name"])
                return@ifPresent
            }

            // Edit data
            state.doAction(GroupAction(ActionType.ADD, state, newGroup))
            // Mark change
            state.isChanged = true
        }
    }
    private val rAddGroupItem       = MenuItem(I18N["context.add_group"]).apply {
        onAction = rAddGroupHandler
    }

    private val gRenameHandler      = EventHandler<ActionEvent> {
        val groupName: String = it.source as String

        showInput(
            state.stage,
            I18N["context.rename_group.dialog.title"],
            I18N["context.rename_group.dialog.header"],
            groupName,
            genGroupNameFormatter()
        ).ifPresent { newName ->
            if (newName.isBlank()) return@ifPresent
            if (state.transFile.groupNames.contains(newName)) {
                showError(state.stage, I18N["context.error.same_group_name"])
                return@ifPresent
            }

            // Edit data
            state.doAction(GroupAction(
                ActionType.CHANGE, state,
                state.transFile.getTransGroup(state.transFile.getGroupIdByName(groupName)),
                newName = newName
            ))
            // Mark change
            state.isChanged = true
        }
    }
    private val gRenameItem         = MenuItem(I18N["context.rename_group"])

    private val gChangeColorPicker  = CColorPicker().apply {
        setPrefSize(40.0, 20.0)
    }
    private val gChangeColorHandler = EventHandler<ActionEvent> {
        val groupName = it.source as String
        val newColor = (it.target as ColorPicker).value

        // Edit data
        state.doAction(GroupAction(
            ActionType.CHANGE, state,
            state.transFile.getTransGroup(state.transFile.getGroupIdByName(groupName)),
            newColorHex = newColor.toHexRGB()
        ))
        // Mark change
        state.isChanged = true
    }
    private val gChangeColorItem    = MenuItem().apply {
        graphic = gChangeColorPicker
        textProperty().bind(Bindings.createStringBinding(
            { gChangeColorPicker.value.toHexRGB() },
            gChangeColorPicker.valueProperty()
        ))
    }

    private val gDeleteHandler      = EventHandler<ActionEvent> {
        val groupName = it.source as String

        // Edit data
        state.doAction(GroupAction(
            ActionType.REMOVE, state,
            state.transFile.getTransGroup(state.transFile.getGroupIdByName(groupName))
        ))
        // Mark change
        state.isChanged = true
    }
    private val gDeleteItem         = MenuItem(I18N["context.delete_group"])

    private val lMoveToHandler      = EventHandler<ActionEvent> { event ->
        @Suppress("UNCHECKED_CAST") val items = event.source as List<CTreeLabelItem>

        val choice = showChoice(
            state.stage,
            I18N["context.move_to.dialog.title"],
            if (items.size == 1) I18N["context.move_to.dialog.header"] else I18N["context.move_to.dialog.header.pl"],
            state.transFile.groupNames
        )
        if (choice.isPresent) {
            val newGroupId = state.transFile.getGroupIdByName(choice.get())

            state.doAction(ComplexAction.of(items.map {
                LabelAction(
                    ActionType.CHANGE, state,
                    state.currentPicName,
                    state.transFile.getTransLabel(state.currentPicName, it.index),
                    newGroupId = newGroupId
                )
            }))
            for (item in items) state.controller.moveLabelTreeItem(item.index, newGroupId)
            // Mark change
            state.isChanged = true
        }
    }
    private val lMoveToItem         = MenuItem(I18N["context.move_to"])

    private val lDeleteHandler      = EventHandler<ActionEvent> { event ->
        @Suppress("UNCHECKED_CAST") val items = event.source as List<CTreeLabelItem>

        val confirm = showConfirm(
            state.stage,
            if (items.size == 1) I18N["context.delete_label.dialog.header"] else I18N["context.delete_label.dialog.header.pl"],
            StringBuilder().apply { for (item in items) appendLine((item as CTreeLabelItem).text) }.toString(),
            I18N["context.delete_label.dialog.title"]
        )
        if (confirm.isPresent && confirm.get() == ButtonType.YES) {
            state.doAction(ComplexAction.of(items.map {
                LabelAction(
                    ActionType.REMOVE, state,
                    state.currentPicName,
                    state.transFile.getTransLabel(state.currentPicName, it.index),
                )
            }))
            state.currentLabelIndex = NOT_FOUND
            // Mark change
            state.isChanged = true
        }
    }
    private val lDeleteItem         = MenuItem(I18N["context.delete_label"])

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
            items.add(rAddGroupItem)
        } else if (rootCount == 0 && groupCount == 1 && labelCount == 0) {
            // group
            val groupItem = selectedItems[0] as CTreeGroupItem
            val groupName = groupItem.name

            gChangeColorPicker.value = groupItem.color
            gDeleteItem.isDisable = !state.transFile.isGroupUnused(state.transFile.getGroupIdByName(groupItem.value))

            gRenameItem.setOnAction { gRenameHandler.handle(ActionEvent(groupName, gRenameItem)) }
            gChangeColorPicker.setOnAction { gChangeColorHandler.handle(ActionEvent(groupName, gChangeColorPicker)) }
            gDeleteItem.setOnAction { gDeleteHandler.handle(ActionEvent(groupName, gDeleteItem)) }

            items.add(gRenameItem)
            items.add(gChangeColorItem)
            items.add(SeparatorMenuItem())
            items.add(gDeleteItem)
        } else if (rootCount == 0 && groupCount == 0 && labelCount > 0) {
            // label(s)
            lMoveToItem.setOnAction { lMoveToHandler.handle(ActionEvent(selectedItems, lMoveToItem)) }
            lDeleteItem.setOnAction { lDeleteHandler.handle(ActionEvent(selectedItems, lDeleteItem)) }

            items.add(lMoveToItem)
            items.add(SeparatorMenuItem())
            items.add(lDeleteItem)
        } else {
            // other
            doNothing()
        }
    }

    fun toggleGroupCreate() {
        rAddGroupItem.fire()
    }
    fun toggleGroupRename(groupName: String) {
        gRenameItem.onAction.handle(ActionEvent(groupName, null))
    }
    fun toggleGroupDelete(groupName: String) {
        gDeleteItem.onAction.handle(ActionEvent(groupName, null))
    }

}
