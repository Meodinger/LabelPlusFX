package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.component.singleton.ATreeMenu
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.component.expandAll
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.onNew
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.string.emptyString

import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
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

    private val rootNameProperty: StringProperty = SimpleStringProperty(emptyString())
    fun rootNameProperty(): StringProperty = rootNameProperty
    var rootName: String by rootNameProperty

    private val viewModeProperty: ObjectProperty<ViewMode> = SimpleObjectProperty(ViewMode.IndexMode)
    fun viewModeProperty(): ObjectProperty<ViewMode> = viewModeProperty
    var viewMode: ViewMode by viewModeProperty

    private val groupsProperty: ListProperty<TransGroup> = SimpleListProperty(FXCollections.emptyObservableList())
    fun groupsProperty(): ListProperty<TransGroup> = groupsProperty
    val groups: ObservableList<TransGroup> by groupsProperty

    private val labelsProperty: ListProperty<TransLabel> = SimpleListProperty(FXCollections.emptyObservableList())
    fun labelsProperty(): ListProperty<TransLabel> = labelsProperty
    val labels: ObservableList<TransLabel> by labelsProperty

    private val groupItems: MutableList<CTreeGroupItem> = ArrayList()
    private val labelItems: MutableList<MutableList<CTreeLabelItem>> = ArrayList()

    init {
        // Init
        root = TreeItem()
        contextMenu = ATreeMenu.apply { update(emptyList()) }
        selectionModel.selectionMode = SelectionMode.MULTIPLE

        // Update tree menu when requested
        addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED) { ATreeMenu.update(selectionModel.selectedItems.toList()) }

        // Listen & bind
        root.valueProperty().bind(rootNameProperty())
        root.valueProperty().addListener(onNew { Platform.runLater { root.expandAll() } })
        viewModeProperty.addListener { _, o, n -> update(o, n) }
        groupsProperty.addListener(ListChangeListener {
            while (it.next()) {
                if (it.wasPermutated()) {
                    // will not happen
                    throw IllegalStateException("Permuted: $it")
                } else if (it.wasUpdated()) {
                    // will not happen
                    throw IllegalStateException("Updated: $it")
                } else {
                    if (it.wasAdded()) for (transGroup in it.addedSubList) createGroupItem(transGroup)
                    if (it.wasRemoved()) for (transGroup in it.removed) removeGroupItem(transGroup)
                }
            }
        })
        labelsProperty.addListener(ListChangeListener {
            while (it.next()) {
                if (it.wasPermutated()) {
                    // will not happen
                    throw IllegalStateException("Permuted: $it")
                } else if (it.wasUpdated()) {
                    // will not happen
                    throw IllegalStateException("Updated: $it")
                } else {
                    if (it.wasAdded()) for (transLabel in it.addedSubList) createLabelItem(transLabel)
                    if (it.wasRemoved()) for (transLabel in it.removed) removeLabelItem(transLabel)
                }
            }
        })
    }

    private fun update(fromMode: ViewMode, toMode: ViewMode) {
        for (transLabel in labels) removeLabelItem(transLabel, fromMode)
        for (transGroup in groups) removeGroupItem(transGroup, fromMode)
        for (transGroup in groups) createGroupItem(transGroup, toMode)
        for (transLabel in labels) createLabelItem(transLabel, toMode)
    }
    private fun createGroupItem(transGroup: TransGroup, viewMode: ViewMode = this.viewMode) {
        labelItems.add(ArrayList())

        val groupItem = CTreeGroupItem().apply {
            nameProperty().bind(transGroup.nameProperty)
            colorProperty().bind(Bindings.createObjectBinding(
                { Color.web(transGroup.colorHex) },
                transGroup.colorHexProperty
            ))
        }

        // Add data
        groupItems.add(groupItem)

        // In IndexMode registration is enough
        if (viewMode == ViewMode.IndexMode) return

        // Add view
        root.children.add(groupItem)
    }
    private fun removeGroupItem(transGroup: TransGroup, viewMode: ViewMode = this.viewMode) {
        val groupId = groupItems.indexOfFirst { it.name == transGroup.name }
        labelItems.removeAt(groupId)

        val groupItem = groupItems[groupId].apply {
            nameProperty().unbind()
            colorProperty().unbind()
        }

        // Remove data
        groupItems.remove(groupItem)

        // In IndexMode un-registration is enough
        if (viewMode == ViewMode.IndexMode) return

        // Remove view
        root.children.remove(groupItem)
    }
    private fun createLabelItem(transLabel: TransLabel, viewMode: ViewMode = this.viewMode) {
        val labelItem = CTreeLabelItem().apply {
            indexProperty().bind(transLabel.indexProperty)
            textProperty().bind(transLabel.textProperty)
            if (viewMode == ViewMode.IndexMode)
                graphicProperty().bind(Bindings.createObjectBinding(
                    { Circle(GRAPHICS_CIRCLE_RADIUS, Color.web(groups[transLabel.groupId].colorHex)) },
                    transLabel.groupIdProperty
                ))
        }

        // Add view
        val parent = when (viewMode) {
            ViewMode.IndexMode -> root
            ViewMode.GroupMode -> groupItems[transLabel.groupId]
        }
        val index = parent.children.indexOfLast { (it as CTreeLabelItem).index < transLabel.index }
        if (index == parent.children.size - 1) parent.children.add(labelItem) else parent.children.add(index + 1, labelItem)
        // Add data
        labelItems[transLabel.groupId].add(labelItem)
    }
    private fun removeLabelItem(transLabel: TransLabel, viewMode: ViewMode = this.viewMode) {
        val labelItem = labelItems[transLabel.groupId].first { it.index == transLabel.index }

        // Unbind
        labelItem.indexProperty().unbind()
        labelItem.textProperty().unbind()
        labelItem.graphicProperty().unbind()

        // Remove view
        when (viewMode) {
            ViewMode.IndexMode -> root.children.remove(labelItem)
            ViewMode.GroupMode -> groupItems[transLabel.groupId].children.remove(labelItem)
        }
        // Remove data
        labelItems[transLabel.groupId].remove(labelItem)
    }

    private fun getGroupItem(groupName: String): CTreeGroupItem {
        for (item in groupItems) if (item.name == groupName) return item
        throw IllegalArgumentException(String.format(I18N["exception.tree_view.no_such_group_item.s"], groupName))
    }
    private fun getLabelItem(labelIndex: Int): CTreeLabelItem {
        for (labelItems in labelItems) for (labelItem in labelItems) if (labelItem.index == labelIndex) return labelItem
        throw IllegalArgumentException(String.format(I18N["exception.tree_view.no_such_label_item.i"], labelIndex))
    }
    private fun select(item: TreeItem<String>, scrollTo: Boolean) {
        selectionModel.clearSelection()
        selectionModel.select(item)
        if (scrollTo) scrollTo(getRow(item))
    }
    fun selectGroup(groupName: String, scrollTo: Boolean) {
        // In IndexMode this will not available
        if (viewMode == ViewMode.IndexMode)
            throw IllegalStateException(I18N["exception.tree_view.group_operation_in_index_mode"])

        select(getGroupItem(groupName), scrollTo)
    }
    fun selectLabel(labelIndex: Int, scrollTo: Boolean = true) {
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
