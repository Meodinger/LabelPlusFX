package ink.meodinger.lpfx.component.singleton

import ink.meodinger.lpfx.State
import ink.meodinger.lpfx.ViewMode
import ink.meodinger.lpfx.component.common.CColorPicker
import ink.meodinger.lpfx.component.CTreeLabelItem
import ink.meodinger.lpfx.component.CTreeView
import ink.meodinger.lpfx.genGroupNameFormatter
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.util.color.toHexRGB
import ink.meodinger.lpfx.util.dialog.showChoice
import ink.meodinger.lpfx.util.dialog.showConfirm
import ink.meodinger.lpfx.util.dialog.showError
import ink.meodinger.lpfx.util.dialog.showInput
import ink.meodinger.lpfx.util.doNothing
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get

import javafx.beans.binding.Bindings
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

    private val r_addGroupItem    = MenuItem(I18N["context.add_group"])
    private val g_renameItem      = MenuItem(I18N["context.rename_group"])
    private val g_changeColorItem = MenuItem()
    private val g_deleteItem      = MenuItem(I18N["context.delete_group"])

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
            StringBuilder().apply { for (item in items) appendLine(item.value) }.toString(),
            State.stage
        )

        if (confirm.isPresent && confirm.get() == ButtonType.YES) {
            view.selectionModel.clearSelection()

            for (item in items) {
                val labelIndex = (item as CTreeLabelItem).index

                // Update view
                State.controller.removeLabelTreeItem(labelIndex)
                State.controller.removeLabel(labelIndex)
                // Edit data
                State.removeTransLabel(State.currentPicName, labelIndex)
                for (label in State.transFile.getTransList(State.currentPicName))
                    if (label.index > labelIndex)
                        State.setTransLabelIndex(State.currentPicName, label.index, label.index - 1)
            }
            // Mark change
            State.isChanged = true
        }
    }
    private val l_deleteItem = MenuItem(I18N["context.delete_label"])

    init {
        val rAddGroupField = TextField().apply { textFormatter = genGroupNameFormatter() }
        val rAddGroupPicker = CColorPicker().apply { hide() }
        val rAddGroupDialog = Dialog<TransGroup>().apply {
            title = I18N["context.add_group.dialog.title"]
            headerText = I18N["context.add_group.dialog.header"]
            dialogPane.content = HBox(rAddGroupField, rAddGroupPicker).also { box -> box.alignment = Pos.CENTER }
            dialogPane.buttonTypes.addAll(ButtonType.FINISH, ButtonType.CANCEL)
            setResultConverter {
                when (it) {
                    ButtonType.FINISH -> TransGroup(rAddGroupField.text, rAddGroupPicker.value.toHexRGB())
                    else -> null
                }
            }
        }

        val gChangeColorPicker = CColorPicker().apply {
            setPrefSize(40.0, 20.0)
            setOnAction {
                val groupItem = view.selectionModel.selectedItem

                // Edit data
                State.setTransGroupColor(
                    State.transFile.getGroupIdByName(groupItem.value),
                    this@apply.value.toHexRGB()
                )
                // Mark change
                State.isChanged = true
            }
        }
        g_changeColorItem.graphic = gChangeColorPicker
        g_changeColorItem.textProperty().bind(Bindings.createStringBinding(
            { gChangeColorPicker.value.toHexRGB() },
            gChangeColorPicker.valueProperty()
        ))

        // Action
        r_addGroupItem.setOnAction {
            val nameList = Settings[Settings.DefaultGroupNameList].asStringList()
            val colorHexList = Settings[Settings.DefaultGroupColorHexList].asStringList().ifEmpty {
                TransFile.Companion.LPTransFile.DEFAULT_COLOR_HEX_LIST
            }

            val newGroupId = State.transFile.groupCount
            var newName = String.format(I18N["context.add_group.new_group.i"], newGroupId + 1)
            if (newGroupId < nameList.size && nameList[newGroupId].isNotEmpty()) {
                if (!State.transFile.groupNames.contains(nameList[newGroupId])) {
                    newName = nameList[newGroupId]
                }
            }

            rAddGroupField.text = newName
            rAddGroupPicker.value = Color.web(colorHexList[newGroupId % colorHexList.size])
            rAddGroupDialog.result = null
            rAddGroupDialog.showAndWait().ifPresent { newGroup ->
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
        g_renameItem.setOnAction {
            val groupItem = view.selectionModel.selectedItem

            showInput(
                State.stage,
                I18N["context.rename_group.dialog.title"],
                I18N["context.rename_group.dialog.header"],
                groupItem.value,
                genGroupNameFormatter()
            ).ifPresent { newName ->
                if (newName.isBlank()) return@ifPresent
                if (State.transFile.groupNames.contains(newName)) {
                    showError(I18N["context.error.same_group_name"], State.stage)
                    return@ifPresent
                }

                // Edit data
                State.setTransGroupName(State.transFile.getGroupIdByName(groupItem.value), newName)
                // Mark change
                State.isChanged = true
            }
        }
        g_deleteItem.setOnAction {
            val groupItem = view.selectionModel.selectedItem
            val groupName = groupItem.value
            val groupId = State.transFile.getGroupIdByName(groupName)

            view.selectionModel.clearSelection()

            // Update view
            State.controller.removeLabelLayer(groupId)
            State.controller.removeGroupBarItem(groupName)
            State.controller.removeGroupTreeItem(groupName)
            // Edit data
            for (key in State.transFile.picNames) for (label in State.transFile.getTransList(key))
                if (label.groupId >= groupId) State.setTransLabelGroup(key, label.index, label.groupId - 1)
            State.removeTransGroup(groupName)
            // Mark change
            State.isChanged = true
        }
    }

    fun initView(view: CTreeView) {
        this.view = view
    }

    fun update(selectedItems: ObservableList<TreeItem<String>>) {
        items.clear()

        if (selectedItems.isEmpty()) return

        // NOTE: toList() to make a not observable copy
        val selected = selectedItems.toList()
        var rootCount = 0
        var groupCount = 0
        var labelCount = 0

        for (item in selected) {
            if (item.parent == null) rootCount += 1
            else if (item is CTreeLabelItem) labelCount += 1
            else groupCount += 1
        }

        if (rootCount == 1 && groupCount == 0 && labelCount == 0) {
            // root
            items.add(r_addGroupItem)
        } else if (rootCount == 0 && groupCount == 1 && labelCount == 0) {
            // group
            val groupItem = selected[0]

            (g_changeColorItem.graphic as CColorPicker).value = (groupItem.graphic as Circle).fill as Color
            g_deleteItem.isDisable = !State.transFile.isGroupUnused(groupItem.value)

            items.add(g_renameItem)
            items.add(g_changeColorItem)
            items.add(SeparatorMenuItem())
            items.add(g_deleteItem)
        } else if (rootCount == 0 && groupCount == 0 && labelCount > 0) {
            // label(s)
            l_moveToItem.setOnAction { l_moveToAction(selected) }
            l_deleteItem.setOnAction { l_deleteAction(selected) }

            items.add(l_moveToItem)
            items.add(SeparatorMenuItem())
            items.add(l_deleteItem)
        } else {
            // other
            doNothing()
        }
    }

}
