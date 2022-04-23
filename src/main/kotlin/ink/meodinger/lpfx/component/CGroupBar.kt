package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.I18N
import ink.meodinger.lpfx.NOT_FOUND
import ink.meodinger.lpfx.get
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.util.component.hgrow
import ink.meodinger.lpfx.util.doNothing
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue

import javafx.beans.binding.Bindings
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
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

    // TODO: Fix the overlap when there are not enough space to layout

    // ----- Properties ----- //

    private val groupsProperty: ListProperty<TransGroup> = SimpleListProperty(FXCollections.emptyObservableList())
    fun groupsProperty(): ListProperty<TransGroup> = groupsProperty
    val groups: ObservableList<TransGroup> by groupsProperty

    private val indexProperty: IntegerProperty = SimpleIntegerProperty(NOT_FOUND)
    fun indexProperty(): IntegerProperty = indexProperty
    var index: Int by indexProperty

    private val onGroupCreateProperty: ObjectProperty<EventHandler<ActionEvent>> = SimpleObjectProperty(EventHandler {})
    fun onGroupCreateProperty(): ObjectProperty<EventHandler<ActionEvent>> = onGroupCreateProperty
    val onGroupCreate: EventHandler<ActionEvent> by onGroupCreateProperty
    fun setOnGroupCreate(handler: EventHandler<ActionEvent>) = onGroupCreateProperty.set(handler)

    // ----- Common Components ----- //

    private val cGroups: MutableList<CGroup> = ArrayList()
    private val placeHolder: HBox = HBox().apply {
        hgrow = Priority.ALWAYS
    }
    private val addItem: CGroup = CGroup("+", Color.BLACK).apply {
        setOnSelect { onGroupCreate.handle(it); isSelected = false }
    }

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

            if (it.list.isEmpty()) index = NOT_FOUND
            if (index != NOT_FOUND && index < it.list.size) cGroups[index].select()
        })
    }

    private fun createGroupItem(transGroup: TransGroup, groupId: Int) {
        if (cGroups.isEmpty()) {
            children.add(placeHolder)
            children.add(addItem)
        }

        children.add(groupId, CGroup().apply {
            nameProperty().bind(transGroup.nameProperty)
            colorProperty().bind(Bindings.createObjectBinding(
                { Color.web(transGroup.colorHex) },
                transGroup.colorHexProperty
            ))
            setOnSelect { select(cGroups.indexOf(this)) }
        }.also { cGroups.add(groupId, it) })
    }
    private fun removeGroupItem(transGroup: TransGroup) {
        val groupId = cGroups.indexOfFirst { it.name == transGroup.name }

        children.remove(cGroups.removeAt(groupId))

        if (cGroups.isEmpty()) {
            children.remove(placeHolder)
            children.remove(addItem)
        }
    }

    fun select(groupId: Int) {
        if (groupId in 0 until groups.size) index = groupId
        else if (groups.size == 0 && groupId == 0) doNothing()
        else throw IllegalArgumentException(String.format(I18N["exception.group_bar.group_id_invalid.i"], groupId))
    }

}
