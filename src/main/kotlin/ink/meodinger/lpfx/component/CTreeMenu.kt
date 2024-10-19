package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.action.*
import ink.meodinger.lpfx.component.common.CColorPicker
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.util.color.toHexRGB
import ink.meodinger.lpfx.util.component.withContent
import ink.meodinger.lpfx.component.dialog.showError
import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.doNothing
import ink.meodinger.lpfx.util.property.transform

import javafx.collections.ListChangeListener
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
class CTreeMenu(
    private val state: State,
    private val view: CTreeView,
) : ContextMenu() {

    // region Controls & Handlers

    private val rAddGroupField = TextField().apply {
        textFormatter = genGeneralFormatter()
    }
    private val rAddGroupPicker = CColorPicker().apply {
        hide()
    }
    private val rAddGroupDialog = Dialog<TransGroup>().apply {
        title = I18N["context.add_group.dialog.title"]
        headerText = I18N["context.add_group.dialog.header"]
        dialogPane.buttonTypes.addAll(ButtonType.FINISH, ButtonType.CANCEL)
        dialogPane.withContent(HBox(rAddGroupField, rAddGroupPicker)) { alignment = Pos.CENTER }

        setResultConverter converter@{
            return@converter when (it) {
                ButtonType.FINISH -> TransGroup(rAddGroupField.text, rAddGroupPicker.value.toHexRGB())
                else -> null
            }
        }
    }
    private val rAddGroupHandler = EventHandler<ActionEvent> {
        // State::stage is not set when initializing TreeMenu
        if (rAddGroupDialog.owner == null) rAddGroupDialog.initOwner(state.stage)

        val nameList = Settings.defaultGroupNameList
        val colorList = Settings.defaultGroupColorHexList.ifEmpty { TransFile.DEFAULT_COLOR_HEX_LIST }

        val newGroupId = state.transFile.groupCount
        var newName: String? = nameList.getOrNull(newGroupId)?.takeIf(String::isNotEmpty)

        if (newName == null || state.transFile.groupList.any { g -> g.name == newName }) {
            var tempNum = newGroupId + 1
            var tempName = String.format(I18N["context.add_group.new_group.i"], tempNum)
            while (state.transFile.groupList.any { g -> g.name == tempName }) {
                tempName = String.format(I18N["context.add_group.new_group.i"], ++tempNum)
            }
            newName = tempName
        }

        rAddGroupField.text = newName
        rAddGroupPicker.value = Color.web(colorList[newGroupId % colorList.size])
        rAddGroupDialog.result = null

        val result = rAddGroupDialog.showAndWait()
        if (!result.isPresent) return@EventHandler
        val newGroup = result.get()
        if (newGroup.name.isBlank()) return@EventHandler

        // Check repeat
        if (state.transFile.groupList.any { g -> g.name == newGroup.name }) {
            showError(state.stage, I18N["context.error.same_group_name"])
            return@EventHandler
        }
        // do Action
        state.doAction(GroupAction(ActionType.ADD, state, newGroup))
    }
    private val rAddGroupItem = MenuItem(I18N["context.add_group"]).apply {
        onAction = rAddGroupHandler
    }

    private val gRenameHandler = EventHandler<ActionEvent> {
        val dialog = TextInputDialog(it.source as String).apply {
            initOwner(state.stage)
            title = I18N["context.rename_group.dialog.title"]
            headerText = I18N["context.rename_group.dialog.header"]
            editor.textFormatter = genGeneralFormatter()
        }

        val result = dialog.showAndWait()
        if (!result.isPresent) return@EventHandler
        val newName = result.get()
        if (newName.isBlank()) return@EventHandler

        // Check repeat
        if (state.transFile.groupList.any { g -> g.name == newName }) {
            showError(state.stage, I18N["context.error.same_group_name"])
            return@EventHandler
        }
        // do Action
        state.doAction(GroupAction(
            ActionType.CHANGE, state,
            state.transFile.getTransGroup(it.source as String),
            newName = newName
        ))
    }
    private val gRenameItem = MenuItem(I18N["context.rename_group"])
    private val gChangeColorPicker = CColorPicker().apply {
        setPrefSize(40.0, 20.0)
    }
    private val gChangeColorHandler = EventHandler<ActionEvent> {
        state.doAction(GroupAction(
            ActionType.CHANGE, state,
            state.transFile.getTransGroup(it.source as String),
            newColorHex = (it.target as ColorPicker).value.toHexRGB()
        ))
    }
    private val gChangeColorItem = MenuItem().apply {
        graphic = gChangeColorPicker
        textProperty().bind(gChangeColorPicker.valueProperty().transform(Color::toHexRGB))
    }
    private val gDeleteHandler = EventHandler<ActionEvent> {
        // Clear selected to-remove items
        view.clearSelection()

        state.doAction(GroupAction(
            ActionType.REMOVE, state,
            state.transFile.getTransGroup(it.source as String)
        ))

        // Select the first group if TransFile has
        if (state.transFile.groupCount > 0) view.selectGroup(state.transFile.groupList[0].name, clear = true, scrollTo = false)
    }
    private val gDeleteItem = MenuItem(I18N["context.delete_group"])

    private val lMoveToHandler = EventHandler<ActionEvent> { event ->
        @Suppress("UNCHECKED_CAST") val items = event.source as List<CTreeLabelItem>

        val groups = state.transFile.groupList.map(TransGroup::name)
        val dialog = ChoiceDialog(groups[0], groups).apply {
            initOwner(state.stage)
            title = I18N["context.move_to.dialog.title"]
            contentText =
                if (items.size == 1) I18N["context.move_to.dialog.header"]
                else I18N["context.move_to.dialog.header.pl"]
        }
        val choice = dialog.showAndWait()
        if (!choice.isPresent) return@EventHandler
        val transGroup = state.transFile.getTransGroup(choice.get())

        val labelActions = items.map {
            LabelAction(
                ActionType.CHANGE, state,
                state.currentPicName,
                state.transFile.getTransLabel(state.currentPicName, it.transLabel.index),
                newGroupId = transGroup.index
            )
        }
        val moveAction = FunctionAction(
            { labelActions.forEach(Action::commit); state.controller.requestUpdateTree() },
            { labelActions.forEach(Action::revert); state.controller.requestUpdateTree() }
        )
        state.doAction(moveAction)
    }
    private val lMoveToItem = MenuItem(I18N["context.move_to"])
    private val lDeleteHandler = EventHandler<ActionEvent> { event ->
        // Reversed to delete big-index label first, make logger more literal
        @Suppress("UNCHECKED_CAST") val items = (event.source as List<CTreeLabelItem>).reversed()

        state.doAction(ComplexAction(items.map {
            LabelAction(
                ActionType.REMOVE, state,
                state.currentPicName,
                state.transFile.getTransLabel(state.currentPicName, it.transLabel.index),
            )
        }))
    }
    private val lDeleteItem = MenuItem(I18N["context.delete_label"])

    private val lMoveToIndexHandler = EventHandler<ActionEvent> { event ->
        @Suppress("UNCHECKED_CAST") val items = event.source as List<CTreeLabelItem>
        // choose the first transLabel
        val item = items[0]
        val labels = state.transFile.getTransList(state.currentPicName).map(TransLabel::index)

        val dialog = ChoiceDialog(labels[0], labels).apply {
            initOwner(state.stage)
            title = I18N["context.move_to_index.dialog.title"]
            contentText = I18N["context.move_to_index.dialog.header"]
        }
        val choice = dialog.showAndWait()
        if (!choice.isPresent) return@EventHandler
//        val transGroup = state.transFile.getTransGroup(choice.get())
        val labelAction= LabelAction(
                ActionType.CHANGE, state,
                state.currentPicName,
                state.transFile.getTransLabel(state.currentPicName, item.transLabel.index),
                newLabelIndex = choice.get()
        )

        val moveAction = FunctionAction(
            { labelAction.commit(); state.controller.requestUpdateTree() },
            { labelAction.revert(); state.controller.requestUpdateTree() }
        )
        state.doAction(moveAction)
    }

    private val lMoveToIndexItem = MenuItem(I18N["context.move_to_index"])

    private val lCopyLabelTextHandler = EventHandler<ActionEvent> { event ->
        @Suppress("UNCHECKED_CAST") val items = event.source as List<CTreeLabelItem>
        // choose the first transLabel
        val item = items[0]
        view.copyLabelText(item.transLabel.index)
    }

    private val lCopyLabelTextItem = MenuItem(I18N["context.copy_label_text"])

    private val lPasteLabelTextHandler = EventHandler<ActionEvent> { event ->
        @Suppress("UNCHECKED_CAST") val items = event.source as List<CTreeLabelItem>
        view.pasteLabelsText(items.map { it.transLabel.index },state)
    }

    private val lPasteLabelTextItem = MenuItem(I18N["context.paste_label_text"])



    // endregion

    init {
        view.selectionModel.selectedItems.addListener(ListChangeListener { change ->
            items.clear()
            val selectedItems = change.list

            if (selectedItems.isEmpty()) return@ListChangeListener

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
                // NOTE: we could not store name here because the name may change
                val groupItem = selectedItems[0] as CTreeGroupItem

                gChangeColorPicker.value = groupItem.transGroup.color
                gDeleteItem.isDisable = state.transFile.isGroupStillInUse(groupItem.value)

                gRenameItem.setOnAction { gRenameHandler.handle(ActionEvent(groupItem.transGroup.name, gRenameItem)) }
                gChangeColorPicker.setOnAction { gChangeColorHandler.handle(ActionEvent(groupItem.transGroup.name, gChangeColorPicker)) }
                gDeleteItem.setOnAction { gDeleteHandler.handle(ActionEvent(groupItem.transGroup.name, gDeleteItem)) }

                items.add(gRenameItem)
                items.add(gChangeColorItem)
                items.add(SeparatorMenuItem())
                items.add(gDeleteItem)
            } else if (rootCount == 0 && groupCount > 1 && labelCount == 0) {
                // multi groups
                // NOTE: we cannot change names here, so it is safe to store names
                val groupNames = selectedItems.map { (it as CTreeGroupItem).transGroup.name }

                gDeleteItem.isDisable = groupNames.any { state.transFile.isGroupStillInUse(it) }
                gDeleteItem.setOnAction { groupNames.forEach { gDeleteHandler.handle(ActionEvent(it, gDeleteItem)) } }

                items.add(gDeleteItem)
            } else if (rootCount == 0 && groupCount == 0 && labelCount > 0) {
                // label(s)
                lMoveToIndexItem.setOnAction { lMoveToIndexHandler.handle(ActionEvent(selectedItems, lMoveToIndexItem)) }
                lMoveToItem.setOnAction { lMoveToHandler.handle(ActionEvent(selectedItems, lMoveToItem)) }
                lCopyLabelTextItem.setOnAction { lCopyLabelTextHandler.handle(ActionEvent(selectedItems, lCopyLabelTextItem)) }
                lPasteLabelTextItem.setOnAction { lPasteLabelTextHandler.handle(ActionEvent(selectedItems, lPasteLabelTextItem)) }
                lDeleteItem.setOnAction { lDeleteHandler.handle(ActionEvent(selectedItems, lDeleteItem)) }

                items.add(lMoveToIndexItem)
                items.add(lMoveToItem)
                items.add(SeparatorMenuItem())
                items.add(lCopyLabelTextItem)
                items.add(lPasteLabelTextItem)
                items.add(SeparatorMenuItem())
                items.add(lDeleteItem)
            } else {
                // other
                doNothing()
            }

        })
    }

    /**
     * Trigger group-create action
     */
    fun triggerGroupCreate() {
        rAddGroupItem.fire()
    }

    /**
     * Trigger group-rename action
     * @param groupName Target group name
     */
    fun triggerGroupRename(groupName: String) {
        gRenameItem.onAction.handle(ActionEvent(groupName, null))
    }

    /**
     * Trigger group-delete action
     * @param groupName Target group name
     */
    fun triggerGroupDelete(groupName: String) {
        gDeleteItem.onAction.handle(ActionEvent(groupName, null))
    }

}
