package info.meodinger.lpfx.component

import info.meodinger.lpfx.*
import info.meodinger.lpfx.component.singleton.CTreeMenu
import info.meodinger.lpfx.type.TransGroup
import info.meodinger.lpfx.type.TransLabel
import info.meodinger.lpfx.util.tree.*
import info.meodinger.lpfx.util.property.setValue
import info.meodinger.lpfx.util.property.getValue
import javafx.beans.binding.Bindings

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

    val groupColorsProperty = SimpleListProperty<Color>(FXCollections.observableArrayList())
    var groupColors: ObservableList<Color> by groupColorsProperty

    val picNameProperty = SimpleStringProperty("")
    var picName: String by picNameProperty

    val viewModeProperty = SimpleObjectProperty(DEFAULT_VIEW_MODE)
    var viewMode: ViewMode by viewModeProperty

    private val groupItems = ArrayList<CTreeGroupItem>()
    private val labelItems = ArrayList<ArrayList<CTreeLabelItem>>()

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
        this.groupColors = FXCollections.observableArrayList()
    }
    fun render(viewMode: ViewMode, picName: String, transGroups: List<TransGroup>, transLabels: List<TransLabel>) {
        this.viewMode = viewMode
        this.picName = picName

        this.root = TreeItem(picName)

        when (viewMode) {
            ViewMode.GroupMode -> {
                for (transGroup in transGroups) addGroupItem(transGroup)
                for (transLabel in transLabels) addLabelItem(transLabel)
            }
            ViewMode.IndexMode -> {
                for (i in transGroups.indices) labelItems.add(ArrayList())
                for (transLabel in transLabels) addLabelItem(transLabel)
            }
        }

        this.root.expandAll()
    }

    private fun getLabelItem(labelIndex: Int): CTreeLabelItem {
        for (labelItems in labelItems) for (labelItem in labelItems) if (labelItem.index == labelIndex) return labelItem
        throw IllegalArgumentException()
    }
    private fun getLabelItem(labelIndex: Int, groupId: Int): CTreeLabelItem {
        for (labelItem in labelItems[groupId]) if (labelItem.index == labelIndex) return labelItem
        throw IllegalArgumentException()
    }

    fun addGroupItem(transGroup: TransGroup) {
        groupColors.add(Color.web(transGroup.colorHex))

        when (viewMode) {
            ViewMode.IndexMode -> return
            ViewMode.GroupMode -> {
                val groupItem = CTreeGroupItem(transGroup.name, Color.web(transGroup.colorHex))

                groupItem.nameProperty.bind(transGroup.nameProperty)
                groupItem.colorProperty.bind(Bindings.createObjectBinding(
                    { Color.web(transGroup.colorHex) },
                    transGroup.colorHexProperty
                ))

                root.children.add(groupItem)
                labelItems.add(ArrayList())
                groupItems.add(groupItem)
            }
        }
    }
    fun addLabelItem(transLabel: TransLabel) {
        val labelItem = CTreeLabelItem(transLabel.index, transLabel.text)

        labelItem.indexProperty.bind(transLabel.indexProperty)

        when (viewMode) {
            ViewMode.IndexMode -> {
                labelItem.graphic = Circle(GRAPHICS_CIRCLE_RADIUS, groupColors[transLabel.groupId])
                labelItem.graphicProperty().bind(Bindings.createObjectBinding(
                    { Circle(GRAPHICS_CIRCLE_RADIUS, groupColors[transLabel.groupId]) },
                    transLabel.groupIdProperty
                ))
                root.children.add(labelItem)
            }
            ViewMode.GroupMode -> {
                groupItems[transLabel.groupId].children.add(labelItem)
            }
        }

        labelItems[transLabel.groupId].add(labelItem)
    }

    fun removeGroupItem(groupName: String) {
        var groupId = NOT_FOUND
        for (i in groupItems.indices) if (groupItems[i].name == groupName) groupId = i
        if (groupId == NOT_FOUND) return

        groupColors.removeAt(groupId)

        when (viewMode) {
            ViewMode.IndexMode -> return
            ViewMode.GroupMode -> {
                val groupItem = groupItems[groupId]
                root.children.remove(groupItem)
                groupItems.remove(groupItem)
            }
        }
    }
    fun removeLabelItem(labelIndex: Int) {
        var groupId = NOT_FOUND
        for (i in labelItems.indices) for (label in labelItems[i]) if (label.index == labelIndex) groupId = i
        if (groupId == NOT_FOUND) return

        val labelItem = getLabelItem(labelIndex, groupId)

        when (viewMode) {
            ViewMode.IndexMode -> root.children.remove(labelItem)
            ViewMode.GroupMode -> groupItems[groupId].children.remove(labelItem)
        }
        labelItems[groupId].remove(labelItem)
    }

    fun moveLabelItem(labelIndex: Int, from: Int, to: Int) {
        val labelItem = getLabelItem(labelIndex)

        groupItems[from].children.remove(labelItem)
        labelItems[from].remove(labelItem)

        groupItems[to].children.add(labelItem)
        labelItems[to].add(labelItem)
    }
}