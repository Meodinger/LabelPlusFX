package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.component.singleton.ATreeMenu
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.component.expandAll
import ink.meodinger.lpfx.util.property.onChange
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.property.getValue

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.*
import javafx.scene.input.ContextMenuEvent
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import kotlin.collections.ArrayList


/**
 * Author: Meodinger
 * Date: 2021/8/16
 * Have fun with my code!
 */

/**
 * A TreeView for tree-style label display
 *
 * Bind Status: Semi-bind (PicName, ViewMode)
 */
class CTreeView: TreeView<String>() {

    val picNameProperty = SimpleStringProperty("")
    var picName: String by picNameProperty

    val viewModeProperty = SimpleObjectProperty(State.DEFAULT_VIEW_MODE)
    var viewMode: ViewMode by viewModeProperty

    private val transGroups: MutableList<TransGroup> = ArrayList()
    private val transLabels: MutableList<TransLabel> = ArrayList()

    private val groupItems: MutableList<CTreeGroupItem> = ArrayList()
    private val labelItems: MutableList<MutableList<CTreeLabelItem>> = ArrayList()

    init {
        // Init
        ATreeMenu.initView(this)

        this.selectionModel.selectionMode = SelectionMode.MULTIPLE
        this.contextMenu = ATreeMenu

        // Update tree menu when requested
        addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED) { ATreeMenu.update(this.selectionModel.selectedItems) }

        // ViewMode -> update
        viewModeProperty.addListener(onChange { update() })
    }

    fun reset() {
        this.root = null

        if (!this.picNameProperty.isBound) this.picName = ""
        if (!this.viewModeProperty.isBound) this.viewMode = State.DEFAULT_VIEW_MODE

        this.transGroups.clear()
        this.transLabels.clear()

        this.groupItems.clear()
        this.labelItems.clear()
    }
    fun render(picName: String = this.picName, transGroups: List<TransGroup>, transLabels: List<TransLabel>, viewMode: ViewMode = this.viewMode) {
        reset()

        if (!this.picNameProperty.isBound) this.picName = picName
        if (!this.viewModeProperty.isBound) this.viewMode = viewMode

        update(transGroups, transLabels)
    }
    fun update(transGroups: List<TransGroup> = this.transGroups.toList(), transLabels: List<TransLabel> = this.transLabels.toList()) {
        this.root = TreeItem(picName)

        this.transGroups.clear()
        this.transLabels.clear()

        this.groupItems.clear()
        this.labelItems.clear()

        for (transGroup in transGroups) createGroupItem(transGroup)
        for (transLabel in transLabels) createLabelItem(transLabel)

        this.root.expandAll()
    }

    private fun getGroupItem(groupName: String): CTreeGroupItem {
        for (item in groupItems) if (item.name == groupName) return item
        throw IllegalArgumentException()
    }
    private fun getLabelItem(labelIndex: Int): CTreeLabelItem {
        for (labelItems in labelItems) for (labelItem in labelItems) if (labelItem.index == labelIndex) return labelItem
        throw IllegalArgumentException()
    }

    fun createGroupItem(transGroup: TransGroup) {
        this.transGroups.add(transGroup)
        this.labelItems.add(ArrayList())

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
                groupItems.add(groupItem)
            }
        }
    }
    fun createLabelItem(transLabel: TransLabel) {
        this.transLabels.add(transLabel)

        val labelItem = CTreeLabelItem(transLabel.index, transLabel.text)

        labelItem.indexProperty.bind(transLabel.indexProperty)
        labelItem.textProperty.bind(transLabel.textProperty)

        when (viewMode) {
            ViewMode.IndexMode -> {
                labelItem.graphic = Circle(
                    GRAPHICS_CIRCLE_RADIUS,
                    Color.web(transGroups[transLabel.groupId].colorHex)
                )
                labelItem.graphicProperty().bind(Bindings.createObjectBinding(
                    { Circle(
                        GRAPHICS_CIRCLE_RADIUS,
                        Color.web(transGroups[transLabel.groupId].colorHex)
                    ) },
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
        val groupItem = getGroupItem(groupName)
        val groupId = groupItems.indexOf(groupItem)

        transGroups.removeAt(groupId)

        when (viewMode) {
            ViewMode.IndexMode -> return
            ViewMode.GroupMode -> {
                root.children.remove(groupItem)
                groupItems.remove(groupItem)
                labelItems.removeAt(groupId)
            }
        }
    }
    fun removeLabelItem(labelIndex: Int) {
        var transLabel: TransLabel? = null
        var labelItem: CTreeLabelItem? = null
        var groupId = NOT_FOUND

        for (label in transLabels) if (label.index == labelIndex) transLabel = label
        for (i in labelItems.indices) for (item in labelItems[i]) if (item.index == labelIndex) {
            labelItem = item
            groupId = i
        }

        if (groupId == NOT_FOUND) return

        when (viewMode) {
            ViewMode.IndexMode -> root.children.remove(labelItem)
            ViewMode.GroupMode -> groupItems[groupId].children.remove(labelItem)
        }
        labelItems[groupId].remove(labelItem)
        transLabels.remove(transLabel)
    }

    fun moveLabelItem(labelIndex: Int, from: Int, to: Int) {
        if (viewMode != ViewMode.GroupMode) return
        val labelItem = getLabelItem(labelIndex)

        groupItems[from].children.remove(labelItem)
        labelItems[from].remove(labelItem)
        groupItems[to].children.add(labelItem)
        labelItems[to].add(labelItem)
    }

    fun select(labelIndex: Int) {
        select(getLabelItem(labelIndex))
    }
    fun select(groupName: String) {
        select(getGroupItem(groupName))
    }
    private fun select(item: TreeItem<String>) {
        this.selectionModel.clearSelection()
        this.selectionModel.select(item)
        this.scrollTo(getRow(item))
    }
}