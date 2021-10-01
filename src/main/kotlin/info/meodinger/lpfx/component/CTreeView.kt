package info.meodinger.lpfx.component

import info.meodinger.lpfx.*
import info.meodinger.lpfx.component.singleton.CTreeMenu
import info.meodinger.lpfx.type.TransGroup
import info.meodinger.lpfx.type.TransLabel
import info.meodinger.lpfx.util.tree.*
import info.meodinger.lpfx.util.property.setValue
import info.meodinger.lpfx.util.property.getValue

import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
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

/**
 * A TreeView for tree-style label display
 */
class CTreeView: TreeView<String>() {

    companion object {
        const val GRAPHICS_CIRCLE_RADIUS = 8.0
    }

    val viewModeProperty = SimpleObjectProperty(DEFAULT_VIEW_MODE)
    var viewMode: ViewMode by viewModeProperty

    val picNameProperty = SimpleStringProperty("")
    var picName: String by picNameProperty

    val groupNamesProperty = SimpleListProperty<String>(FXCollections.emptyObservableList())
    var groupNames: ObservableList<String> by groupNamesProperty

    val groupColorsProperty = SimpleListProperty<Color>(FXCollections.emptyObservableList())
    var groupColors: ObservableList<Color> by groupColorsProperty

    val transLabelsProperty = SimpleListProperty<TransLabel>(FXCollections.emptyObservableList())
    var transLabels: ObservableList<TransLabel> by transLabelsProperty

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
        this.groupNames = FXCollections.emptyObservableList()
        this.groupColors = FXCollections.emptyObservableList()
        this.transLabels = FXCollections.emptyObservableList()
    }
    fun render(viewMode: ViewMode, picName: String, groupNames: List<String>, groupColors: List<String>, transLabels: List<TransLabel>) {
        this.viewMode = viewMode
        this.picName = picName
        this.groupNames = FXCollections.observableList(groupNames)
        this.groupColors = FXCollections.observableList(List(groupColors.size) { Color.web(groupColors[it]) })
        this.transLabels = FXCollections.observableList(transLabels)

        this.root = TreeItem(picName)

        when (viewMode) {
            ViewMode.GroupMode -> renderByGroup()
            ViewMode.IndexMode -> renderByIndex()
        }

        this.root.expandAll()
    }
    private fun renderByGroup() {
        val groupItems = ArrayList<TreeItem<String>>()

        for (i in groupNames.indices) {
            val circle = Circle(GRAPHICS_CIRCLE_RADIUS, groupColors[i])
            val groupItem = TreeItem(groupNames[i], circle)
            groupItems.add(groupItem)
            root.children.add(groupItem)
        }
        for (transLabel in transLabels) {
            groupItems[transLabel.groupId].children.add(CTreeItem(transLabel))
        }
    }
    private fun renderByIndex() {
        for (transLabel in transLabels) {
            val circle = Circle(GRAPHICS_CIRCLE_RADIUS, groupColors[transLabel.groupId])
            root.children.add(CTreeItem(transLabel, circle))
        }
    }

    fun addGroupItem(transGroup: TransGroup) {
        groupNames.add(transGroup.name)
        groupColors.add(Color.web(transGroup.color))

        when (viewMode) {
            ViewMode.IndexMode -> return
            ViewMode.GroupMode -> {
                val newItem = TreeItem(transGroup.name, Circle(GRAPHICS_CIRCLE_RADIUS, Color.web(transGroup.color)))
                root.children.add(newItem)
            }
        }
    }
    fun removeGroupItem(transGroup: TransGroup) {
        groupNames.remove(transGroup.name)
        groupColors.remove(Color.web(transGroup.color))

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
                val newItem = CTreeItem(transLabel, Circle(GRAPHICS_CIRCLE_RADIUS, groupColors[transLabel.groupId]))
                root.children.add(newItem)
            }
            ViewMode.GroupMode -> {
                val newItem = CTreeItem(transLabel)
                val groupItem = root.children.find { it.value == groupNames[transLabel.groupId] }!!
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
                val groupItem = root.children.find { it.value == groupNames[transLabel.groupId] }!!
                val labelItem = groupItem.children.find { (it as CTreeItem).meta == transLabel }
                groupItem.children.remove(labelItem)
            }
        }
    }

}