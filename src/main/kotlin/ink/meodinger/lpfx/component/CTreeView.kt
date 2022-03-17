package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.*
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
        selectionModel.selectionMode = SelectionMode.MULTIPLE

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
                    // Ignore, TransGroup's Property changed
                } else {
                    if (it.wasRemoved()) it.removed.forEach(this::removeGroupItem)
                    if (it.wasAdded()) it.addedSubList.forEach { group ->
                        createGroupItem(group, index = it.list.indexOf(group))
                    }
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
                    if (it.wasRemoved()) it.removed.forEach(this::removeLabelItem)
                    if (it.wasAdded()) it.addedSubList.forEach(this::createLabelItem)
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
    private fun createGroupItem(transGroup: TransGroup, viewMode: ViewMode = this.viewMode, index: Int = groupItems.size) {
        labelItems.add(ArrayList())

        val groupItem = CTreeGroupItem().apply {
            nameProperty().bind(transGroup.nameProperty)
            colorProperty().bind(Bindings.createObjectBinding(
                { Color.web(transGroup.colorHex) },
                transGroup.colorHexProperty
            ))
        }

        // Add data
        groupItems.add(index, groupItem)

        // In IndexMode registration is enough
        if (viewMode == ViewMode.IndexMode) return

        // Add view
        root.children.add(index, groupItem)
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
                    // GroupsProperty will change when group's color change
                    groupsProperty, transLabel.groupIdProperty,
                    // TODO: https://stackoverflow.com/questions/71513087/javafx-valueat-binding-only-compute-once
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

    fun moveLabelItem(labelIndex: Int, oriGroupId: Int, dstGroupId: Int) {
        val labelItem = getLabelItem(labelIndex)

        labelItems[oriGroupId].remove(labelItem)
        labelItems[dstGroupId].add(labelItem)

        if (viewMode == ViewMode.GroupMode) {
            groupItems[oriGroupId].children.remove(labelItem)
            groupItems[dstGroupId].children.add(labelItem)
        }
    }

}
