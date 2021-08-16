package info.meodinger.lpfx.component

import info.meodinger.lpfx.State
import info.meodinger.lpfx.options.Settings
import info.meodinger.lpfx.type.TransGroup
import info.meodinger.lpfx.util.color.toHex
import info.meodinger.lpfx.util.dialog.*
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get

import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Circle

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.component
 */
object CTreeMenu {

    private val GroupNameTextFormatter = TextFormatter<String> { change ->
        change.text = change.text
            .trim()
            .replace(" ", "_")
            .replace("|", "_")
        change
    }

    abstract class TargetMenu : ContextMenu() {
        abstract fun redirectTo(target: TreeItem<String>?)
    }
    val rootMenu = object : TargetMenu() {

        private val addGroupField = TextField()
        private val addGroupPicker = CColorPicker()
        private val addGroupItem = MenuItem(I18N["context.add_group"])
        private val addGroupDialog = Dialog<TransGroup>()
        private val addGroupAction = { rootItem: TreeItem<String> ->
            val newGroupId = State.transFile.groupList.size
            val newColor = if (newGroupId >= 9) Color.WHITE
            else Color.web(Settings[Settings.DefaultColorList].asList()[newGroupId])

            addGroupField.text = String.format(I18N["context.add_group.dialog.format"], newGroupId + 1)
            addGroupPicker.value = newColor
            addGroupDialog.result = null
            addGroupDialog.showAndWait().ifPresent { newGroup ->
                // Edit data
                State.transFile.groupList.add(newGroup)
                // Update view
                State.accessor.updateGroupList()
                State.accessor.addLabelLayer()
                rootItem.children.add(TreeItem(newGroup.name, Circle(8.0, Color.web(newGroup.color))))
                // Mark change
                State.isChanged = true
            }
        }

        init {
            addGroupField.textFormatter = GroupNameTextFormatter
            addGroupPicker.hide()
            addGroupDialog.title = I18N["context.add_group.dialog.title"]
            addGroupDialog.headerText = I18N["context.add_group.dialog.header"]
            addGroupDialog.dialogPane.content = HBox(addGroupField, addGroupPicker).also { it.alignment = Pos.CENTER }
            addGroupDialog.dialogPane.buttonTypes.addAll(ButtonType.APPLY, ButtonType.CANCEL)
            addGroupDialog.setResultConverter {
                if (it == ButtonType.APPLY)
                    TransGroup(addGroupField.text, addGroupPicker.value.toHex())
                else
                    null
            }

            items.add(addGroupItem)
        }

        override fun redirectTo(target: TreeItem<String>?) {
            if (target == null) {
                addGroupItem.onAction = null
            } else {
                addGroupItem.setOnAction { addGroupAction(target) }
            }
        }

    }
    val groupMenu = object : TargetMenu() {

        private val renameItem = MenuItem(I18N["context.rename_group"])
        private val renameAction = { groupItem: TreeItem<String> ->
            showInput(
                State.stage,
                I18N["context.rename_group.dialog.title"],
                I18N["context.rename_group.dialog.header"],
                groupItem.value,
                TextFormatter<String> { change ->
                    change.text = change.text
                        .trim()
                        .replace(" ", "_")
                        .replace("|", "_")
                    change
                }
            ).ifPresent { newName ->
                if (newName.isBlank()) return@ifPresent
                for (group in State.transFile.groupList) {
                    if (group.name == newName) showError(I18N["context.rename_group.error.same_name"])
                    return@ifPresent
                }

                val groupId = State.getGroupIdByName(groupItem.value)

                // Edit data
                State.transFile.getTransGroupAt(groupId).name = newName
                // Update view
                State.accessor.updateGroupList()
                groupItem.value = newName
                // Mark change
                State.isChanged = true
            }
        }

        private val changeColorItem = MenuItem()
        private val changeColorPicker = CColorPicker()
        private val changeColorAction = { groupItem: TreeItem<String> ->
            val newColor = changeColorPicker.value
            val groupId = State.getGroupIdByName(groupItem.value)

            // Edit data
            State.transFile.getTransGroupAt(groupId).color = newColor.toHex()
            // Update view
            (groupItem.graphic as Circle).fill = newColor
            // Mark change
            State.isChanged = true
        }

        private val deleteItem = MenuItem(I18N["context.delete_group"])
        private val deleteAction = { groupItem: TreeItem<String> ->
            val groupId = State.getGroupIdByName(groupItem.value)

            // Edit data
            for (labels in State.transFile.transMap.values) for (label in labels)
                if (label.groupId >= groupId) label.groupId = label.groupId - 1
            State.transFile.groupList.removeIf { it.name == groupItem.value }
            // Update view
            groupItem.parent.children.remove(groupItem)
            State.accessor.updateGroupList()
            State.accessor.removeLabelLayer(groupId)
            // Mark change
            State.isChanged = true
        }

        init {
            changeColorPicker.valueProperty().addListener { _, _, newValue ->
                changeColorItem.text = newValue.toHex()
            }
            changeColorPicker.setPrefSize(40.0, 20.0)
            changeColorItem.graphic = changeColorPicker

            items.add(renameItem)
            items.add(changeColorItem)
            items.add(SeparatorMenuItem())
            items.add(deleteItem)
        }

        override fun redirectTo(target: TreeItem<String>?) {
            if (target == null) {
                renameItem.onAction = null
                changeColorItem.onAction = null
                deleteItem.onAction = null
            } else {
                renameItem.setOnAction { renameAction(target) }

                changeColorPicker.value = (target.graphic as Circle).fill as Color
                changeColorItem.text = changeColorPicker.value.toHex()
                changeColorPicker.setOnAction { changeColorAction(target) }

                deleteItem.isDisable = false
                val thisGroupId = State.getGroupIdByName(target.value)
                outer@ for (labels in State.transFile.transMap.values) {
                    for (label in labels) {
                        if (label.groupId == thisGroupId) {
                            deleteItem.isDisable = true
                            break@outer
                        }
                    }
                }
                deleteItem.setOnAction { deleteAction(target) }
            }
        }

    }
    val labelMenu = object : TargetMenu() {

        private val moveToItem = MenuItem(I18N["context.move_to"])
        private val moveToAction = { item: TreeItem<String> ->
            val labelItem = item as CTreeItem
            val groupNameList = ArrayList<String>()
            for (group in State.transFile.groupList) groupNameList.add(group.name)

            showChoice(
                State.stage,
                I18N["context.move_to.title"],
                I18N["context.move_to.header"],
                groupNameList
            ).ifPresent { newGroupName ->
                // Edit data
                labelItem.groupId = State.getGroupIdByName(newGroupName)
                // Update view
                State.accessor.updateTree()
                // Mark change
                State.isChanged = true
            }
        }

        private val deleteItem = MenuItem(I18N["context.delete_label"])
        private val deleteAction = { item: TreeItem<String> ->
            val labelItem = item as CTreeItem
            val result = showConfirm(
                I18N["context.delete_label.dialog.title"],
                I18N["context.delete_label.dialog.header"],
                labelItem.value
            )

            if (result.isPresent && result.get() == ButtonType.YES) {
                // Edit data
                for (label in State.transFile.getTransLabelListOf(State.currentPicName)) {
                    if (label.index > labelItem.index) {
                        label.index = label.index - 1
                    }
                }
                State.transFile.getTransLabelListOf(State.currentPicName).remove(labelItem.meta)
                // Update view
                State.accessor.updateTree()
                // Mark change
                State.isChanged = true
            }
        }

        init {
            items.add(moveToItem)
            items.add(SeparatorMenuItem())
            items.add(deleteItem)
        }

        override fun redirectTo(target: TreeItem<String>?) {
            if (target == null) {
                moveToItem.onAction = null
                deleteItem.onAction = null
            } else {
                moveToItem.setOnAction { moveToAction(target) }
                deleteItem.setOnAction { deleteAction(target) }
            }
        }

    }

    abstract class TreeMenu : ContextMenu() {
        abstract fun init(view: TreeView<String>?)
        abstract fun update()
    }
    val treeMenu = object : TreeMenu() {

        private var view: TreeView<String>? = null

        private val moveToItem = MenuItem(I18N["context.move_to"])
        private val moveToAction = { view: TreeView<String> ->
            val selectedItems = view.selectionModel.selectedItems
            val groupNameList = ArrayList<String>()
            for (group in State.transFile.groupList) groupNameList.add(group.name)

            showChoice(
                State.stage,
                I18N["context.move_to.title"],
                I18N["context.move_to.header"],
                groupNameList
            ).ifPresent { newGroupName ->
                val newGroupId = State.getGroupIdByName(newGroupName)

                // Edit data
                for (item in selectedItems) {
                    (item as CTreeItem).groupId = newGroupId
                }
                // Update view
                State.accessor.updateTree()
                // Mark change
                State.isChanged = true
            }
        }

        private val deleteItem = MenuItem(I18N["context.delete_label"])
        private val deleteAction = { view: TreeView<String> ->
            val selectedItems = view.selectionModel.selectedItems
            val result = showConfirm(
                I18N["context.delete_label.dialog.title"],
                null,
                I18N["context.delete_label.dialog.content.pl"],
            )

            if (result.isPresent && result.get() == ButtonType.YES) {
                // Edit data
                for (item in selectedItems) {
                    val label = (item as CTreeItem).meta
                    for (l in State.transFile.getTransLabelListOf(State.currentPicName)) {
                        if (l.index > label.index)
                            l.index = l.index - 1
                    }
                    State.transFile.getTransLabelListOf(State.currentPicName).remove(label)
                }
                // Update view
                State.accessor.updateTree()
                // Mark change
                State.isChanged = true
            }
        }

        init {
            items.add(moveToItem)
            items.add(SeparatorMenuItem())
            items.add(deleteItem)
        }

        override fun init(view: TreeView<String>?) {
            this.view = view

            if (view == null) {
                moveToItem.onAction = null
                deleteItem.onAction = null
            } else {
                moveToItem.setOnAction { moveToAction(view) }
                deleteItem.setOnAction { deleteAction(view) }
            }
        }

        override fun update() {
            moveToItem.isDisable = true
            deleteItem.isDisable = true

            if (view != null) {
                val selectedItems = view!!.selectionModel.selectedItems

                if (selectedItems.size == 0) return
                for (item in selectedItems) {
                    if (item.javaClass != CTreeItem::class.java) {
                        return
                    }
                }
            }

            moveToItem.isDisable = false
            deleteItem.isDisable = false
        }

    }

}