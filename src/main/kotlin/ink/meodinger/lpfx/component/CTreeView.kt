package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.component.singleton.ATreeMenu
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.component.expandAll
import ink.meodinger.lpfx.util.property.onChange
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.isNotBound
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get

import javafx.beans.binding.Bindings
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.scene.control.*
import javafx.scene.input.ContextMenuEvent
import javafx.scene.paint.Color
import javafx.scene.shape.Circle


/**
 * Author: Meodinger
 * Date: 2021/8/16
 * Have fun with my code!
 */

/**
 * A TreeView for tree-style label display
 */
class CTreeView: TreeView<String>() {

    private val picNameProperty: StringProperty = SimpleStringProperty("")
    fun picNameProperty(): StringProperty = picNameProperty
    var picName: String by picNameProperty

    private val viewModeProperty: ObjectProperty<ViewMode> = SimpleObjectProperty(State.DEFAULT_VIEW_MODE)
    fun viewModeProperty(): ObjectProperty<ViewMode> = viewModeProperty
    var viewMode: ViewMode by viewModeProperty

    private val transGroups: MutableList<TransGroup> = ArrayList()
    private val transLabels: MutableList<TransLabel> = ArrayList()

    private val groupItems: MutableList<CTreeGroupItem> = ArrayList()
    private val labelItems: MutableList<MutableList<CTreeLabelItem>> = ArrayList()

    init {
        // Init
        ATreeMenu.initView(this)

        selectionModel.selectionMode = SelectionMode.MULTIPLE
        contextMenu = ATreeMenu

        // Update tree menu when requested
        addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED) { ATreeMenu.update(selectionModel.selectedItems) }

        // ViewMode -> update
        viewModeProperty.addListener(onChange { update() })
    }

    fun reset() {
        root = null

        if (picNameProperty.isNotBound) picName = ""
        if (viewModeProperty.isNotBound) viewMode = State.DEFAULT_VIEW_MODE

        transGroups.clear()
        transLabels.clear()

        groupItems.clear()
        labelItems.clear()
    }
    fun render(picName: String = this.picName, transGroups: List<TransGroup>, transLabels: List<TransLabel>, viewMode: ViewMode = this.viewMode) {
        reset()

        if (picNameProperty.isNotBound) this.picName = picName
        if (viewModeProperty.isNotBound) this.viewMode = viewMode

        update(transGroups, transLabels)
    }
    fun update(transGroups: List<TransGroup> = this.transGroups.toList(), transLabels: List<TransLabel> = this.transLabels.toList()) {
        // NOTE: toList() to make a copy
        root = TreeItem(picName)

        this.transGroups.clear()
        this.transLabels.clear()

        groupItems.clear()
        labelItems.clear()

        for (transGroup in transGroups) when (viewMode) {
            ViewMode.IndexMode -> registerGroup(transGroup)
            ViewMode.GroupMode -> createGroupItem(transGroup)
        }
        for (transLabel in transLabels) {
            createLabelItem(transLabel)
        }

        root.expandAll()
    }

    private fun select(item: TreeItem<String>, scrollTo: Boolean) {
        selectionModel.clearSelection()
        selectionModel.select(item)
        if (scrollTo) scrollTo(getRow(item))
    }
    private fun getGroupItem(groupName: String): CTreeGroupItem {
        for (item in groupItems) if (item.name == groupName) return item
        throw IllegalArgumentException(String.format(I18N["exception.tree_view.no_such_group_item.s"], groupName))
    }
    private fun getLabelItem(labelIndex: Int): CTreeLabelItem {
        for (labelItems in labelItems) for (labelItem in labelItems) if (labelItem.index == labelIndex) return labelItem
        throw IllegalArgumentException(String.format(I18N["exception.tree_view.no_such_label_item.i"], labelIndex))
    }

    /**
     * Register a TransGroup.
     * Add it to data but not display it, use in IndexMode
     */
    fun registerGroup(transGroup: TransGroup) {
        labelItems.add(ArrayList())
        transGroups.add(transGroup)
    }
    fun unregisterGroup(groupName: String) {
        var groupId = NOT_FOUND
        transGroups.forEachIndexed { index, transGroup ->
            if (transGroup.name == groupName) {
                groupId = index
                return@forEachIndexed
            }
        }

        if (groupId == NOT_FOUND)
            throw IllegalArgumentException(String.format(I18N["exception.tree_view.no_such_group_item.s"], groupName))

        labelItems.removeAt(groupId)
        transGroups.removeAt(groupId)
    }
    fun createGroupItem(transGroup: TransGroup) {
        // In IndexMode this will not available
        if (viewMode == ViewMode.IndexMode)
            throw IllegalStateException(I18N["exception.tree_view.group_operation_in_index_mode"])

        val groupItem = CTreeGroupItem().also {
            it.nameProperty().bind(transGroup.nameProperty)
            it.colorProperty().bind(Bindings.createObjectBinding(
                { Color.web(transGroup.colorHex) },
                transGroup.colorHexProperty
            ))
        }

        // Add view
        root.children.add(groupItem)
        // Add data
        groupItems.add(groupItem)
        registerGroup(transGroup)
    }
    fun removeGroupItem(groupName: String) {
        // In IndexMode this will not available
        if (viewMode == ViewMode.IndexMode)
            throw IllegalStateException(I18N["exception.tree_view.group_operation_in_index_mode"])

        val groupItem = getGroupItem(groupName)

        // Unbind
        groupItem.nameProperty().unbind()
        groupItem.colorProperty().unbind()

        // Remove view
        root.children.remove(groupItem)
        // Remove data
        groupItems.remove(groupItem)
        unregisterGroup(groupName)
    }
    fun selectGroup(groupName: String, scrollTo: Boolean) {
        // In IndexMode this will not available
        if (viewMode == ViewMode.IndexMode)
            throw IllegalStateException(I18N["exception.tree_view.group_operation_in_index_mode"])

        select(getGroupItem(groupName), scrollTo)
    }

    fun createLabelItem(transLabel: TransLabel) {
        val labelItem = CTreeLabelItem().also {
            it.indexProperty().bind(transLabel.indexProperty)
            it.textProperty().bind(transLabel.textProperty)
        }

        // Add view
        when (viewMode) {
            ViewMode.IndexMode -> {
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
        // Add data
        labelItems[transLabel.groupId].add(labelItem)
        transLabels.add(transLabel)
    }
    fun removeLabelItem(labelIndex: Int) {
        val labelItem = getLabelItem(labelIndex)
        val transLabel = transLabels.first { it.index == labelIndex }

        // Unbind
        labelItem.indexProperty().unbind()
        labelItem.textProperty().unbind()
        if (viewMode == ViewMode.IndexMode) labelItem.graphicProperty().unbind()

        // Remove view
        when (viewMode) {
            ViewMode.IndexMode -> root.children.remove(labelItem)
            ViewMode.GroupMode -> groupItems[transLabel.groupId].children.remove(labelItem)
        }
        // Remove data
        labelItems[transLabel.groupId].remove(labelItem)
        transLabels.remove(transLabel)
    }
    fun selectLabel(labelIndex: Int, scrollTo: Boolean) {
        select(getLabelItem(labelIndex), scrollTo)
    }

    fun moveLabelItem(labelIndex: Int, from: Int, to: Int) {
        when (viewMode) {
            ViewMode.IndexMode -> return
            ViewMode.GroupMode -> {
                val labelItem = getLabelItem(labelIndex)

                groupItems[from].children.remove(labelItem)
                labelItems[from].remove(labelItem)
                groupItems[to].children.add(labelItem)
                labelItems[to].add(labelItem)
            }
        }
    }

}
