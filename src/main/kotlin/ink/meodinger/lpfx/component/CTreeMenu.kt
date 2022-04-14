package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.I18N
import ink.meodinger.lpfx.State
import ink.meodinger.lpfx.get
import ink.meodinger.lpfx.action.*
import ink.meodinger.lpfx.component.common.CColorPicker
import ink.meodinger.lpfx.genGeneralFormatter
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.util.color.toHexRGB
import ink.meodinger.lpfx.util.component.withContent
import ink.meodinger.lpfx.util.dialog.showChoice
import ink.meodinger.lpfx.util.dialog.showError
import ink.meodinger.lpfx.util.dialog.showInput
import ink.meodinger.lpfx.util.doNothing

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
        textFormatter = genGeneralFormatter()
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
            if (!state.transFile.groupList.any { g -> g.name == nameList[newGroupId] }) {
                newName = nameList[newGroupId]
            }
        }

        rAddGroupField.text = newName
        rAddGroupPicker.value = Color.web(colorHexList[newGroupId % colorHexList.size])
        rAddGroupDialog.result = null
        rAddGroupDialog.showAndWait().ifPresent { newGroup ->
            if (state.transFile.groupList.any { g -> g.name == newName }) {
                showError(state.stage, I18N["context.error.same_group_name"])
                return@ifPresent
            }

            state.doAction(GroupAction(ActionType.ADD, state, newGroup))
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
            genGeneralFormatter()
        ).ifPresent { newName ->
            if (newName.isBlank()) return@ifPresent
            if (state.transFile.groupList.any { g -> g.name == newName }) {
                showError(state.stage, I18N["context.error.same_group_name"])
                return@ifPresent
            }

            state.doAction(GroupAction(
                ActionType.CHANGE, state,
                state.transFile.getTransGroup(state.transFile.getGroupIdByName(groupName)),
                newName = newName
            ))
        }
    }
    private val gRenameItem         = MenuItem(I18N["context.rename_group"])

    private val gChangeColorPicker  = CColorPicker().apply {
        setPrefSize(40.0, 20.0)
    }
    private val gChangeColorHandler = EventHandler<ActionEvent> {
        val groupName = it.source as String
        val newColorHex = (it.target as ColorPicker).value.toHexRGB()

        state.doAction(GroupAction(
            ActionType.CHANGE, state,
            state.transFile.getTransGroup(state.transFile.getGroupIdByName(groupName)),
            newColorHex = newColorHex
        ))
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

        state.doAction(GroupAction(
            ActionType.REMOVE, state,
            state.transFile.getTransGroup(state.transFile.getGroupIdByName(groupName))
        ))
    }
    private val gDeleteItem         = MenuItem(I18N["context.delete_group"])

    private val lMoveToHandler      = EventHandler<ActionEvent> { event ->
        @Suppress("UNCHECKED_CAST") val items = event.source as List<CTreeLabelItem>

        val choice = showChoice(
            state.stage,
            I18N["context.move_to.dialog.title"],
            if (items.size == 1) I18N["context.move_to.dialog.header"] else I18N["context.move_to.dialog.header.pl"],
            state.transFile.groupList.map(TransGroup::name)
        )
        if (choice.isPresent) {
            val newGroupId = state.transFile.getGroupIdByName(choice.get())

            val labelActions = items.map {
                LabelAction(
                    ActionType.CHANGE, state,
                    state.currentPicName,
                    state.transFile.getTransLabel(state.currentPicName, it.index),
                    newGroupId = newGroupId
                )
            }
            val moveAction = FunctionAction(
                { labelActions.forEach(Action::commit); state.controller.requestUpdateTree() },
                { labelActions.forEach(Action::revert); state.controller.requestUpdateTree() }
            )
            state.doAction(moveAction)
        }
    }
    private val lMoveToItem         = MenuItem(I18N["context.move_to"])

    private val lDeleteHandler      = EventHandler<ActionEvent> { event ->
        // Reversed to delete big-index label first, make logger more literal
        @Suppress("UNCHECKED_CAST") val items = (event.source as List<CTreeLabelItem>).reversed()

        state.doAction(ComplexAction.of(items.map {
            LabelAction(
                ActionType.REMOVE, state,
                state.currentPicName,
                state.transFile.getTransLabel(state.currentPicName, it.index),
            )
        }))
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
            // single group
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
        } else if (rootCount == 0 && groupCount >= 2 && labelCount == 0) {
            // multi groups
            val groupNames = selectedItems.map { (it as CTreeGroupItem).name }

            gDeleteItem.isDisable = !groupNames.all { state.transFile.isGroupUnused(state.transFile.getGroupIdByName(it)) }
            gDeleteItem.setOnAction { groupNames.forEach { gDeleteHandler.handle(ActionEvent(it, gDeleteItem)) } }

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
