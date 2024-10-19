package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.action.Action
import ink.meodinger.lpfx.action.ActionType
import ink.meodinger.lpfx.action.FunctionAction
import ink.meodinger.lpfx.action.LabelAction
import ink.meodinger.lpfx.options.Logger
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
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent





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
     * Selected GroupItem's GroupId
     */
    var selectedGroup: Int by selectedGroupProperty
        private set

    private val selectedLabelProperty: IntegerProperty = SimpleIntegerProperty(NOT_FOUND)
    fun selectedLabelProperty(): ReadOnlyIntegerProperty = selectedLabelProperty
    /**
     * Selected LabelItem's index
     */
    var selectedLabel: Int by selectedLabelProperty
        private set

    // endregion

    private val groupItems: MutableList<CTreeGroupItem> = ArrayList()
    private val labelItems: MutableList<CTreeLabelItem> = ArrayList()
//    private val copyTextProperty:  StringProperty = SimpleStringProperty(emptyString())
//
//    /**
//     * Selected copyText
//     */
//    var copyText: String by copyTextProperty
//        private set



    init {
        // Init
        root = TreeItem<String?>().apply {
            valueProperty().bind(rootNameProperty())
            valueProperty().addListener(onNew { expandAll() })
        }
        selectionModel.selectionMode = SelectionMode.MULTIPLE

        // Layout
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
                is CTreeGroupItem -> selectedGroup = groups.indexOf(it.transGroup)
                is CTreeLabelItem -> selectedLabel = it.transLabel.index
            }
        })
    }

    private fun update() {
        root.children.clear()
        groupItems.clear()
        labelItems.clear()

        for ((groupId, transGroup) in groups.withIndex()) createGroupItem(transGroup, groupId)
        for (transLabel in labels) createLabelItem(transLabel)
        selectLabel(selectedLabel, clear = true, scrollTo = true)
    }
    private fun createGroupItem(transGroup: TransGroup, groupId: Int) {
        val groupItem = CTreeGroupItem(transGroup)
        // Add view
        when (viewMode) {
            ViewMode.IndexMode -> doNothing()
            ViewMode.GroupMode -> root.children.add(groupId, groupItem)
        }
        // Add data
        groupItems.add(groupId, groupItem)
    }
    private fun removeGroupItem(transGroup: TransGroup) {
        val groupItem = groupItems.first { it.transGroup === transGroup }
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
        val labelItem = CTreeLabelItem(transLabel, viewMode == ViewMode.IndexMode)
        // Add view
        val parent = when (viewMode) {
            ViewMode.IndexMode -> root
            ViewMode.GroupMode -> groupItems[transLabel.groupId]
        }
        val index = parent.children.indexOfLast { (it as CTreeLabelItem).transLabel.index < transLabel.index }
        if (index == parent.children.size - 1) parent.children.add(labelItem) else parent.children.add(index + 1, labelItem)
        // Add data
        labelItems.add(labelItem)
    }
    private fun removeLabelItem(transLabel: TransLabel) {
        val labelItem = labelItems.first { it.transLabel === transLabel }
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

    fun selectRoot(clear: Boolean, scrollTo: Boolean) {
        if (clear) clearSelection()
        selectionModel.select(root)
        if (scrollTo) scrollTo(getRow(root))
    }
    fun selectFirst(clear: Boolean = true, scrollTo: Boolean = true) : Int {
        if(labelItems.isEmpty()) {
            selectRoot(clear, scrollTo)
            return NOT_FOUND
        }
        selectLabel(labelItems[0].transLabel.index, clear, scrollTo)
        return labelItems[0].transLabel.index
    }
    fun selectLast(clear: Boolean = true, scrollTo: Boolean = true) : Int {
        if(labelItems.isEmpty()) {
            selectRoot(clear, scrollTo)
            return NOT_FOUND
        }
        selectLabel(labelItems.last().transLabel.index, clear, scrollTo)
        return labelItems.last().transLabel.index
    }
    fun selectGroup(groupName: String, clear: Boolean, scrollTo: Boolean) {
        // In IndexMode this is not available
        if (viewMode == ViewMode.IndexMode) return

        if (clear) clearSelection()
        val item = groupItems.first { it.transGroup.name == groupName }

        selectionModel.select(item)
        if (scrollTo) scrollTo(getRow(item))
    }
    fun selectLabel(labelIndex: Int, clear: Boolean, scrollTo: Boolean) {
        val item = labelItems.firstOrNull{ it.transLabel.index == labelIndex } ?:return
        if (clear) clearSelection()

        selectionModel.select(item)
        if (scrollTo) scrollTo(getRow(item))
    }
    fun selectLabels(labelIndices: Collection<Int>, clear: Boolean, scrollTo: Boolean) {
        if (clear) clearSelection()
        val items = labelItems.filter { it.transLabel.index in labelIndices }

        items.forEach(selectionModel::select)
        if (scrollTo) scrollTo(getRow(items.first()))
    }

    fun copyLabelText(labelIndex: Int) {
        val item = labelItems.firstOrNull { it.transLabel.index == labelIndex } ?:return
        val clipboard = Clipboard.getSystemClipboard()
        val clipboardContent = ClipboardContent()
        clipboardContent.putString(item.transLabel.text)
        Logger.info("Copy text from the label of $labelIndex", "CTreeView")
        clipboard.setContent(clipboardContent)
    }

    fun pasteLabelsText(labelIndexes: Collection<Int>, state: State) {
        val indexes = labelItems.map { it.transLabel.index }.filter { labelIndexes.any { i -> i == it } }
        val clipboard = Clipboard.getSystemClipboard()
        if (!clipboard.hasString()||indexes.isEmpty()) return
        Logger.info("Paste text into the labels of ${indexes.joinToString(separator = ",")}", "CTreeView")
        val labelActions = indexes.map {
            LabelAction(
                ActionType.CHANGE, state,
                state.currentPicName,
                state.transFile.getTransLabel(state.currentPicName, it),
                newText = clipboard.string
            )
        }
        val pasteAction = FunctionAction(
            { labelActions.forEach(Action::commit) },
            { labelActions.forEach(Action::revert) }
        )
        state.doAction(pasteAction)
    }


    /**
     * This will also clear the selected-index
     */
    fun clearSelection() {
        selectionModel.clearSelection()
        selectedGroup = NOT_FOUND
        selectedLabel = NOT_FOUND
    }

    /**
     * Request the TreeView to re-render. This function is useful
     * when some labels' group change in IndexMode.
     */
    fun requestUpdate() {
        update()
    }

}
