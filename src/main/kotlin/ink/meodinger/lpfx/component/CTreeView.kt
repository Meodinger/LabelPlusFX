package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.component.expandAll
import ink.meodinger.lpfx.util.doNothing
import ink.meodinger.lpfx.util.property.*
import ink.meodinger.lpfx.util.string.emptyString

import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.control.*
import javafx.scene.paint.Color


/**
 * Author: Meodinger
 * Date: 2021/8/16
 * Have fun with my code!
 */

/**
 * A TreeView for tree-style label display
 */
class CTreeView: TreeView<String>() {

    // region Properties:Layout

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

    // endregion

    // region Properties:Selection

    private val selectedGroupProperty: IntegerProperty = SimpleIntegerProperty(NOT_FOUND)
    fun selectedGroupProperty(): ReadOnlyIntegerProperty = selectedGroupProperty
    /**
     * Selected GroupItem's GroupId, note that set this will not clear previous selection
     */
    var selectedGroup: Int by selectedGroupProperty
        private set

    private val selectedLabelProperty: IntegerProperty = SimpleIntegerProperty(NOT_FOUND)
    fun selectedLabelProperty(): ReadOnlyIntegerProperty = selectedLabelProperty
    /**
     * Selected LabelItem's index, note that set this will not clear previous selection
     */
    var selectedLabel: Int by selectedLabelProperty
        private set

    // endregion

    private val groupItems: MutableList<CTreeGroupItem> = ArrayList()
    private val labelItems: MutableList<CTreeLabelItem> = ArrayList()

    init {
        // Init
        root = TreeItem<String?>().apply {
            valueProperty().bind(rootNameProperty())
            valueProperty().addListener(onNew { expandAll() })
        }
        selectionModel.selectionMode = SelectionMode.MULTIPLE

        // Listen
        viewModeProperty.addListener(onNew { update() })
        groupsProperty.addListener(ListChangeListener {
            while (it.next()) {
                if (it.wasPermutated()) {
                    // will not happen
                    throw IllegalStateException("Permuted: $it")
                } else if (it.wasUpdated()) {
                    // Ignore, TransGroup's Property changed
                } else {
                    if (it.wasRemoved()) it.removed.forEach(this::removeGroupItem)
                    if (it.wasAdded()) {
                        it.addedSubList.forEachIndexed { index, group ->
                            createGroupItem(group, groupId = it.from + index)
                        }
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

        // Selection
        selectionModel.selectedItemProperty().addListener(onNew {
            when (it) {
                // These set will be ignored if select by set selected properties. (old == new)
                is CTreeGroupItem -> selectedGroup = groups.map(TransGroup::name).indexOf(it.name)
                is CTreeLabelItem -> selectedLabel = it.index
            }
        })
    }

    private fun update() {
        for (item in groupItems) {
            item.nameProperty().unbind()
            item.colorProperty().unbind()
        }
        for (item in labelItems) {
            item.indexProperty().unbind()
            item.textProperty().unbind()
            item.graphicProperty().unbind()
        }

        root.children.clear()
        groupItems.clear()
        labelItems.clear()

        for ((groupId, transGroup) in groups.withIndex()) createGroupItem(transGroup, groupId)
        for (transLabel in labels) createLabelItem(transLabel)
    }
    private fun createGroupItem(transGroup: TransGroup, groupId: Int) {
        val groupItem = CTreeGroupItem().apply {
            nameProperty().bind(transGroup.nameProperty())
            colorProperty().bind(transGroup.colorHexProperty().transform(Color::web))
        }

        // Add view
        when (viewMode) {
            ViewMode.IndexMode -> doNothing()
            ViewMode.GroupMode -> root.children.add(groupId, groupItem)
        }
        // Add data
        groupItems.add(groupId, groupItem)
    }
    private fun removeGroupItem(transGroup: TransGroup) {
        val groupId = groupItems.indexOfFirst { it.name == transGroup.name }
        val groupItem = groupItems[groupId]

        // Unbind
        groupItem.nameProperty().unbind()
        groupItem.colorProperty().unbind()

        // Clear selection
        selectionModel.clearSelection(getRow(groupItem))
        // Remove view
        when (viewMode) {
            ViewMode.IndexMode -> doNothing()
            ViewMode.GroupMode -> root.children.remove(groupItem)
        }
        // Remove data
        groupItems.remove(groupItem)
    }
    private fun createLabelItem(transLabel: TransLabel) {
        val labelItem = CTreeLabelItem().apply {
            indexProperty().bind(transLabel.indexProperty())
            textProperty().bind(transLabel.textProperty())

            if (viewMode != ViewMode.IndexMode) return@apply
            colorProperty().bind(groupsProperty.valueAt(transLabel.groupIdProperty()).transform { Color.web(it.colorHex) })
        }

        // Add view
        val parent = when (viewMode) {
            ViewMode.IndexMode -> root
            ViewMode.GroupMode -> groupItems[transLabel.groupId]
        }
        val index = parent.children.indexOfLast { (it as CTreeLabelItem).index < transLabel.index }
        if (index == parent.children.size - 1) parent.children.add(labelItem) else parent.children.add(index + 1, labelItem)
        // Add data
        labelItems.add(labelItem)
    }
    private fun removeLabelItem(transLabel: TransLabel) {
        val labelItem = labelItems.first { it.index == transLabel.index }

        // Unbind
        labelItem.indexProperty().unbind()
        labelItem.textProperty().unbind()
        labelItem.graphicProperty().unbind()

        // Clear selection
        selectionModel.clearSelection(getRow(labelItem))
        // Remove view
        when (viewMode) {
            ViewMode.IndexMode -> root.children.remove(labelItem)
            ViewMode.GroupMode -> groupItems[transLabel.groupId].children.remove(labelItem)
        }
        // Remove data
        labelItems.remove(labelItem)
    }

    fun selectGroup(groupName: String, clear: Boolean, scrollTo: Boolean) {
        // In IndexMode this is not available
        if (viewMode == ViewMode.IndexMode)
            throw IllegalStateException(I18N["exception.tree_view.group_operation_in_index_mode"])

        if (clear) selectionModel.clearSelection()
        val item = groupItems.first { it.name == groupName }

        selectionModel.select(item)
        if (scrollTo) scrollTo(getRow(item))
    }
    fun selectLabel(labelIndex: Int, clear: Boolean, scrollTo: Boolean) {
        if (clear) selectionModel.clearSelection()
        val item = labelItems.first { it.index == labelIndex }

        selectionModel.select(item)
        if (scrollTo) scrollTo(getRow(item))
    }
    fun selectLabels(labelIndices: Collection<Int>, clear: Boolean, scrollTo: Boolean) {
        if (clear) selectionModel.clearSelection()
        val items = labelItems.filter { labelIndices.contains(it.index) }

        items.forEach(selectionModel::select)
        if (scrollTo) scrollTo(getRow(items.first()))
    }

    fun requestUpdate() { update() }

}
