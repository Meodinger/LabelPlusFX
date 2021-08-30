package info.meodinger.lpfx.component

import info.meodinger.lpfx.*
import info.meodinger.lpfx.component.singleton.CTreeMenu
import info.meodinger.lpfx.type.TransGroup
import info.meodinger.lpfx.type.TransLabel
import info.meodinger.lpfx.util.tree.*

import javafx.scene.control.*
import javafx.scene.input.ContextMenuEvent
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

    private var viewMode: ViewMode = State.viewMode
    private var picName: String = ""
    private var transGroups: MutableList<TransGroup> = ArrayList()
    private var transLabels: MutableList<TransLabel> = ArrayList()

    init {
        // Init
        this.selectionModel.selectionMode = SelectionMode.MULTIPLE
        this.contextMenu = CTreeMenu

        // Update tree menu when requested
        addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED) {
            CTreeMenu.update(this.selectionModel.selectedItems)
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