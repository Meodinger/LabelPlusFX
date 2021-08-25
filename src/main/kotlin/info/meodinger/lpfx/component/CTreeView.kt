package info.meodinger.lpfx.component

import info.meodinger.lpfx.*
import info.meodinger.lpfx.options.Settings
import info.meodinger.lpfx.type.TransGroup
import info.meodinger.lpfx.type.TransLabel
import info.meodinger.lpfx.util.color.toHex
import info.meodinger.lpfx.util.dialog.*
import info.meodinger.lpfx.util.getGroupNameFormatter
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get
import info.meodinger.lpfx.util.tree.*

import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.input.ContextMenuEvent
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import kotlin.collections.ArrayList

/**
 * Author: Meodinger
 * Date: 2021/8/16
 * Location: info.meodinger.lpfx.component
 */
class CTreeView: TreeView<String>() {

    companion object {
        const val GRAPHICS_CIRCLE_RADIUS = 8.0
    }

    private abstract class TreeMenu : ContextMenu() {
        abstract fun update(selectedItems: ObservableList<TreeItem<String>>)
    }
    private val treeMenu = object : TreeMenu() {

        private val r_addGroupField = TextField()
        private val r_addGroupPicker = CColorPicker()
        private val r_addGroupDialog = Dialog<TransGroup>()
        private val r_addGroupAction = {
            val colorList = Settings[Settings.DefaultColorList].asStringList()
            val nameList = Settings[Settings.DefaultGroupList].asStringList()

            val newGroupId = State.transFile.groupList.size
            val newColor =
                if (newGroupId < 9) Color.web(colorList[newGroupId])
                else Color.BLACK
            val newName =
                if (newGroupId < nameList.size) nameList[newGroupId]
                else String.format(I18N["context.add_group.new_group.format"], newGroupId + 1)

            r_addGroupField.text = newName
            r_addGroupPicker.value = newColor
            r_addGroupDialog.result = null
            r_addGroupDialog.showAndWait().ifPresent { newGroup ->
                // Edit data
                State.transFile.groupList.add(newGroup)
                // Update view
                State.controller.updateLabelColorList()
                State.controller.addLabelLayer()
                State.controller.updateGroupList()
                addGroupItem(newGroup)
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

                val transGroup = State.transFile.groupList[State.getGroupIdByName(groupItem.value)]
                val oldName = transGroup.name

                // Edit data
                transGroup.name = newName
                // Update view
                State.controller.updateGroupList()
                updateGroupItem(oldName, transGroup)
                // Mark change
                State.isChanged = true
            }
        }
        private val g_renameItem = MenuItem(I18N["context.rename_group"])
        private val g_changeColorPicker = CColorPicker()
        private val g_changeColorAction = { groupItem: TreeItem<String> ->
            val newColor = g_changeColorPicker.value

            val transGroup = State.transFile.groupList[State.getGroupIdByName(groupItem.value)]

            // Edit data
            transGroup.color = newColor.toHex()
            // Update view
            State.controller.updateLabelColorList()
            updateGroupItem(transGroup.name, transGroup)
            // Mark change
            State.isChanged = true
        }
        private val g_changeColorItem = MenuItem()
        private val g_deleteAction = { groupItem: TreeItem<String> ->
            val groupId = State.getGroupIdByName(groupItem.value)
            val transGroup = State.transFile.groupList[groupId]

            // Edit data
            for (labels in State.transFile.transMap.values) for (label in labels) {
                if (label.groupId >= groupId) {
                    label.groupId = label.groupId - 1
                }
            }
            State.transFile.groupList.remove(transGroup)
            // Update view
            State.controller.updateLabelColorList()
            State.controller.delLabelLayer(groupId)
            State.controller.updateGroupList()
            removeGroupItem(transGroup)
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
                val newGroupId = State.getGroupIdByName(newGroupName)

                // Edit data
                for (item in items) {
                    val transLabel = (item as CTreeItem).meta
                    transLabel.groupId = newGroupId
                }
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
                    val transLabel = (item as CTreeItem).meta
                    for (l in State.transFile.getTransLabelListOf(State.currentPicName)) {
                        if (l.index > transLabel.index) {
                            l.index = l.index - 1
                        }
                    }
                    State.transFile.getTransLabelListOf(State.currentPicName).remove(transLabel)
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

        override fun update(selectedItems: ObservableList<TreeItem<String>>) {
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
                    val thisGroupId = State.getGroupIdByName(groupItem.value)

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

    private var viewMode: ViewMode = DefaultViewMode
    private var picName: String = ""
    private var transGroups: MutableList<TransGroup> = ArrayList()
    private var transLabels: MutableList<TransLabel> = ArrayList()

    init {
        // Init
        this.selectionModel.selectionMode = SelectionMode.MULTIPLE
        this.contextMenu = treeMenu

        // Update tree menu when requested
        addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED) {
            treeMenu.update(this.selectionModel.selectedItems)
        }
    }

    fun reset() {
        this.root = null
        this.picName = ""
        this.transGroups = ArrayList()
        this.transLabels = ArrayList()
    }
    fun update(viewMode: ViewMode, picName: String, transGroups: List<TransGroup>, transLabels: List<TransLabel>) {
        this.viewMode = viewMode
        this.picName = picName
        this.transGroups = ArrayList(transGroups)
        this.transLabels = ArrayList(transLabels)

        this.root = TreeItem(picName)

        when (viewMode) {
            ViewMode.GroupMode -> updateByGroup()
            ViewMode.IndexMode -> updateByIndex()
        }

        this.root.expandAll()
    }
    private fun updateByGroup() {
        val groupItems = ArrayList<TreeItem<String>>()

        for (transGroup in transGroups) {
            val circle = Circle(GRAPHICS_CIRCLE_RADIUS, Color.web(transGroup.color))
            val groupItem = TreeItem(transGroup.name, circle)
            groupItems.add(groupItem)
            root.children.add(groupItem)
        }
        for (transLabel in transLabels) {
            groupItems[transLabel.groupId].children.add(CTreeItem(transLabel))
        }
    }
    private fun updateByIndex() {
        for (transLabel in transLabels) {
            val transGroup = transGroups[transLabel.groupId]
            val circle = Circle(GRAPHICS_CIRCLE_RADIUS, Color.web(transGroup.color))
            root.children.add(CTreeItem(transLabel, circle))
        }
    }

    fun addGroupItem(transGroup: TransGroup) {
        transGroups.add(transGroup)

        when (viewMode) {
            ViewMode.IndexMode -> return
            ViewMode.GroupMode -> {
                val newItem = TreeItem(transGroup.name, Circle(GRAPHICS_CIRCLE_RADIUS, Color.web(transGroup.color)))
                root.children.add(newItem)
            }
        }
    }
    fun addLabelItem(transLabel: TransLabel) {
        transLabels.add(transLabel)

        when (viewMode) {
            ViewMode.IndexMode -> {
                val newItem = CTreeItem(transLabel, Circle(GRAPHICS_CIRCLE_RADIUS, Color.web(transGroups[transLabel.groupId].color)))
                root.children.add(newItem)
            }
            ViewMode.GroupMode -> {
                val newItem = CTreeItem(transLabel)
                val groupItem = root.children.find { it.value == transGroups[transLabel.groupId].name }!!
                groupItem.children.add(newItem)
            }
        }
    }

    fun updateGroupItem(name: String, transGroup: TransGroup) {
        when (viewMode) {
            ViewMode.IndexMode -> return
            ViewMode.GroupMode -> {
                val groupItem = root.children.find { it.value == name }!!
                groupItem.value = transGroup.name
                (groupItem.graphic as Circle).fill = Color.web(transGroup.color)
            }
        }
    }

    fun removeGroupItem(transGroup: TransGroup) {
        transGroups.remove(transGroup)

        when (viewMode) {
            ViewMode.IndexMode -> return
            ViewMode.GroupMode -> {
                val groupItem = root.children.find { it.value == transGroup.name }!!
                root.children.remove(groupItem)
            }
        }
    }
    fun removeLabelItem(transLabel: TransLabel) {
        transLabels.remove(transLabel)

        when (viewMode) {
            ViewMode.IndexMode -> {
                val labelItem = root.children.find { (it as CTreeItem).meta == transLabel }
                root.children.remove(labelItem)
            }
            ViewMode.GroupMode -> {
                val groupItem = root.children.find { it.value == transGroups[transLabel.groupId].name }!!
                val labelItem = groupItem.children.find { (it as CTreeItem).meta == transLabel }
                groupItem.children.remove(labelItem)
            }
        }
    }

}