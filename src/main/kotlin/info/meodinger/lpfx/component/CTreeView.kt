package info.meodinger.lpfx.component

import info.meodinger.lpfx.*
import info.meodinger.lpfx.component.singleton.CTreeMenu
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

    val groupNamesProperty = SimpleListProperty<String>(FXCollections.emptyObservableList())
    val groupColorsProperty = SimpleListProperty<Color>(FXCollections.emptyObservableList())
    var groupNames: ObservableList<String> by groupNamesProperty
    var groupColors: ObservableList<Color> by groupColorsProperty

    val picNameProperty = SimpleStringProperty("")
    var picName: String by picNameProperty

    val viewModeProperty = SimpleObjectProperty(DEFAULT_VIEW_MODE)
    var viewMode: ViewMode by viewModeProperty

    private val groupItems = ArrayList<TreeItem<String>>()
    private val labelItems = ArrayList<ArrayList<CTreeItem>>()

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
    }
    fun render(viewMode: ViewMode, picName: String, groupNames: List<String>, groupColors: List<String>, transLabels: List<TransLabel>) {
        this.viewMode = viewMode
        this.picName = picName
        this.groupNames = FXCollections.observableList(groupNames)
        this.groupColors = FXCollections.observableList(List(groupColors.size) { Color.web(groupColors[it]) })

        this.root = TreeItem(picName)

        when (viewMode) {
            ViewMode.GroupMode -> renderByGroup(transLabels)
            ViewMode.IndexMode -> renderByIndex(transLabels)
        }

        this.root.expandAll()
    }
    private fun renderByGroup(transLabels: List<TransLabel>) {
        val groupItems = ArrayList<TreeItem<String>>()

        for (i in groupNames.indices) {
            val circle = Circle(GRAPHICS_CIRCLE_RADIUS, groupColors[i])
            val groupItem = TreeItem(groupNames[i], circle)
            groupItems.add(groupItem)
            root.children.add(groupItem)
        }
        for (transLabel in transLabels) {
            groupItems[transLabel.groupId].children.add(CTreeItem(transLabel.index, transLabel.text))
        }
    }
    private fun renderByIndex(transLabels: List<TransLabel>) {
        for (transLabel in transLabels) {
            val circle = Circle(GRAPHICS_CIRCLE_RADIUS, groupColors[transLabel.groupId])
            root.children.add(CTreeItem(transLabel.index, transLabel.text, circle))
        }
    }
    private fun getLabelGroup(labelIndex: Int): Int {
        for (i in labelItems.indices) for (label in labelItems[i]) if (label.index == labelIndex) return i
        return NOT_FOUND
    }

    fun addGroupItem(name: String, color: Color) {
        groupNames.add(name)
        groupColors.add(color)

        when (viewMode) {
            ViewMode.IndexMode -> return
            ViewMode.GroupMode -> {
                val groupItem = TreeItem(name, Circle(GRAPHICS_CIRCLE_RADIUS, color))
                labelItems.add(ArrayList())
                groupItems.add(groupItem)
                root.children.add(groupItem)
            }
        }
    }
    fun removeGroupItem(groupName: String) {
        val groupId = groupNames.indexOf(groupName)

        groupNames.removeAt(groupId)
        groupColors.removeAt(groupId)

        when (viewMode) {
            ViewMode.IndexMode -> return
            ViewMode.GroupMode -> {
                val groupItem = groupItems[groupId]
                groupItems.remove(groupItem)
                root.children.remove(groupItem)
            }
        }
    }
    fun updateGroupItem(groupName: String, name: String? = null, color: Color? = null) {
        when (viewMode) {
            ViewMode.IndexMode -> return
            ViewMode.GroupMode -> {
                val groupItem = groupItems[groupNames.indexOf(groupName)]

                if (name != null) groupItem.value = name
                if (color != null) (groupItem.graphic as Circle).fill = color
            }
        }
    }

    fun addLabelItem(index: Int, text: String, groupId: Int) {
        val labelItem = CTreeItem(index, text)

        when (viewMode) {
            ViewMode.IndexMode -> {
                labelItem.graphic = Circle(GRAPHICS_CIRCLE_RADIUS, groupColors[groupId])
                root.children.add(labelItem)
            }
            ViewMode.GroupMode -> {
                groupItems[groupId].children.add(labelItem)
            }
        }

        labelItems[groupId].add(labelItem)
    }
    fun removeLabelItem(labelIndex: Int) {
        val groupId = getLabelGroup(labelIndex)
        val labelItem = labelItems[groupId][labelIndex]

        labelItems.removeAt(labelIndex)
        when (viewMode) {
            ViewMode.IndexMode -> root.children.remove(labelItem)
            ViewMode.GroupMode -> groupItems[groupId].children.remove(labelItem)
        }
    }
    fun updateLabelItem(labelIndex: Int, index: Int? = null, text: String? = null, groupId: Int? = null) {
        val labelItem = labelItems[getLabelGroup(labelIndex)][labelIndex]

        if (index != null) labelItem.index = index
        if (text != null) labelItem.text = text
        if (groupId != null) {
            groupItems[getLabelGroup(labelIndex)].children.remove(labelItem)
            groupItems[groupId].children.add(labelItem)
        }
    }

}