package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.NOT_FOUND
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.util.component.hgrow
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.property.onNew

import javafx.beans.property.*
import javafx.collections.*
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.SingleSelectionModel
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.paint.Color


/**
 * Author: Meodinger
 * Date: 2021/9/30
 * Have fun with my code!
 */

/**
 * A ToolBar to display groups
 */
class CGroupBar : HBox() {

    // Maybe: Fix the overlap when there are not enough space to layout

    // region Properties

    private val groupsProperty: ListProperty<TransGroup> = SimpleListProperty(FXCollections.emptyObservableList())
    /**
     * The TransGroups to display
     */
    fun groupsProperty(): ListProperty<TransGroup> = groupsProperty
    /**
     * @see groupsProperty
     */
    var groups: ObservableList<TransGroup> by groupsProperty

    private val indexProperty: IntegerProperty = SimpleIntegerProperty(NOT_FOUND)
    /**
     * An export to `selectionModel::selectedIndexProperty()`
     * @see javafx.scene.control.SelectionModel.selectedIndexProperty
     */
    fun indexProperty(): IntegerProperty = indexProperty
    /**
     * @see indexProperty
     */
    var index: Int by indexProperty

    private val selectionModelProperty: ObjectProperty<SingleSelectionModel<TransGroup>> = SimpleObjectProperty(GroupBarSelectionModel())
    /**
     * The CGroupBar's SingleSelectionModel, readonly
     */
    fun selectionModelProperty(): ReadOnlyObjectProperty<SingleSelectionModel<TransGroup>> = selectionModelProperty
    /**
     * @see selectionModelProperty
     */
    val selectionModel: SingleSelectionModel<TransGroup> by selectionModelProperty

    private val onGroupCreateProperty: ObjectProperty<EventHandler<ActionEvent>> = SimpleObjectProperty(EventHandler {})
    /**
     * How to handle GroupCreate (Click on Create-CGroup)
     */
    fun onGroupCreateProperty(): ObjectProperty<EventHandler<ActionEvent>> = onGroupCreateProperty
    /**
     * @see onGroupCreateProperty
     */
    val onGroupCreate: EventHandler<ActionEvent> by onGroupCreateProperty
    /**
     * @see onGroupCreateProperty
     */
    fun setOnGroupCreate(handler: EventHandler<ActionEvent>) = onGroupCreateProperty.set(handler)

    // endregion

    // region Instance

    private val holder = HBox().apply {
        hgrow = Priority.ALWAYS
    }
    private val adder = CGroup("+", Color.BLACK).apply {
        setOnAction {
            onGroupCreate.handle(it)
            isSelected = false
        }
    }


    // endregion

    init {
        groupsProperty.addListener(ListChangeListener {
            while (it.next()) {
                if (it.wasPermutated()) {
                    // will not happen
                    throw IllegalStateException("Permuted: $it")
                } else if (it.wasUpdated()) {
                    // Ignore, TransGroup's Property changed,
                } else {
                    if (it.wasRemoved()) {
                        it.removed.forEach(this::removeGroupItem)
                    }
                    if (it.wasAdded()) {
                        it.addedSubList.forEachIndexed { index, group ->
                            createGroupItem(group, it.from + index)
                        }
                    }
                }
            }
        })

        indexProperty.addListener(onNew<Number, Int>(selectionModel::select))
        selectionModel.selectedIndexProperty().addListener(onNew<Number, Int>(indexProperty::set))
    }

    private fun createGroupItem(transGroup: TransGroup, groupId: Int) {
        if (children.isEmpty()) {
            children.add(holder)
            children.add(adder)
        }

        val node = CGroup().apply {
            nameProperty().bind(transGroup.nameProperty())
            colorProperty().bind(transGroup.colorProperty())
            setOnAction { selectionModel.select(transGroup) }
        }
        children.add(groupId, node)
    }
    private fun removeGroupItem(transGroup: TransGroup) {
        if (children.size == 3) {
            children.removeLast()
            children.removeLast()
        }

        val node = children.filterIsInstance(CGroup::class.java).first { it.name == transGroup.name }.apply {
            nameProperty().unbind()
            colorProperty().unbind()
        }
        children.remove(node)
    }

    private inner class GroupBarSelectionModel : SingleSelectionModel<TransGroup>() {

        init {
            groupsProperty.addListener(WeakListChangeListener {
                select(groupsProperty.get()?.indexOf(selectedItem) ?: NOT_FOUND)
            })

            selectedIndexProperty().addListener { _, o, n ->
                if (o != NOT_FOUND && o in 0 until children.size - 2) (children[o as Int] as CGroup).unselect()
                if (n != NOT_FOUND && n in 0 until children.size - 2) (children[n as Int] as CGroup).select()
            }
        }

        override fun getModelItem(index: Int): TransGroup? = groups.getOrNull(index)
        override fun getItemCount(): Int = groups.size

        /**
         * Override. Do not set if `selectedItem` is it not contained in the `items`.
         */
        override fun select(transGroup: TransGroup?) {
            if (transGroup == null || transGroup !in groups) {
                selectedIndex = -1
                selectedItem = null
            } else {
                selectedIndex = groups.indexOf(transGroup)
                selectedItem = transGroup
            }
        }

    }

}
