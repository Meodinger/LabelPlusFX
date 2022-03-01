package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.NOT_FOUND
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.util.addLast
import ink.meodinger.lpfx.util.component.boxHGrow
import ink.meodinger.lpfx.util.doNothing
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get

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

    companion object {
        private const val TAG_GROUP_ID = "TAG_GROUP_ID"
    }

    // ----- Properties ----- //

    private val groupsProperty: ListProperty<TransGroup> = SimpleListProperty(FXCollections.emptyObservableList())
    fun groupsProperty(): ListProperty<TransGroup> = groupsProperty
    val groups: ObservableList<TransGroup> by groupsProperty

    private val selectedGroupIndexProperty: IntegerProperty = SimpleIntegerProperty(NOT_FOUND)
    fun selectedGroupIndexProperty(): IntegerProperty = selectedGroupIndexProperty
    var selectedGroupIndex: Int by selectedGroupIndexProperty

    private val onGroupCreateProperty: ObjectProperty<EventHandler<ActionEvent>> = SimpleObjectProperty(EventHandler {})
    fun onGroupCreateProperty(): ObjectProperty<EventHandler<ActionEvent>> = onGroupCreateProperty
    val onGroupCreate: EventHandler<ActionEvent> by onGroupCreateProperty
    fun setOnGroupCreate(handler: EventHandler<ActionEvent>) = onGroupCreateProperty.set(handler)

    // ----- Common Components ----- //

    private val cGroups: MutableList<CGroup> = ArrayList()
    private val placeHolder: HBox = HBox().apply {
        boxHGrow = Priority.ALWAYS
    }
    private val addItem: CGroup = CGroup("+", Color.BLACK).apply {
        onSelectProperty().bind(onGroupCreateProperty)
    }
    private val vShift = 2

    init {
        groupsProperty.addListener(ListChangeListener {
            while (it.next()) {
                if (it.wasPermutated()) {
                    // will not happen
                    throw IllegalStateException("Permuted: $it")
                } else if (it.wasUpdated()) {
                    // will not happen
                    throw IllegalStateException("Updated: $it")
                } else {
                    if (it.wasAdded()) for (transGroup in it.addedSubList) createGroup(transGroup)
                    if (it.wasRemoved()) for (transGroup in it.removed) removeGroup(transGroup)
                }
            }

            if (cGroups.isEmpty()) selectedGroupIndex = NOT_FOUND
            if (selectedGroupIndex != NOT_FOUND && selectedGroupIndex < cGroups.size) cGroups[selectedGroupIndex].select()
        })

        selectedGroupIndexProperty.addListener { _, o, n ->
            if ((o as Int) != NOT_FOUND && o < cGroups.size) cGroups[o].unselect()
            if ((n as Int) != NOT_FOUND && n < cGroups.size) cGroups[n].select()
        }
    }

    private fun tagGroupId(cGroup: CGroup, groupId: Int) {
        cGroup.properties[TAG_GROUP_ID] = groupId
    }
    private fun useGroupId(cGroup: CGroup): Int {
        return cGroup.properties[TAG_GROUP_ID] as Int
    }

    private fun createGroup(transGroup: TransGroup) {
        if (cGroups.isEmpty()) {
            children.add(placeHolder)
            children.add(addItem)
        }

        children.addLast(CGroup().apply {
            val groupId = cGroups.size // Don't put this into lambda

            nameProperty().bind(transGroup.nameProperty)
            colorProperty().bind(Bindings.createObjectBinding(
                { Color.web(transGroup.colorHex) },
                transGroup.colorHexProperty
            ))

            tagGroupId(this, groupId)
            setOnSelect { select(useGroupId(this)) }
        }.also { cGroups.add(it) }, vShift)
    }
    private fun removeGroup(transGroup: TransGroup) {
        val groupId = cGroups.indexOfFirst { it.name == transGroup.name }

        children.remove(cGroups.removeAt(groupId))
        cGroups.forEach {
            val oldGroupId = useGroupId(it)
            if (oldGroupId > groupId) tagGroupId(it, oldGroupId - 1)
        }

        if (cGroups.isEmpty()) {
            children.remove(placeHolder)
            children.remove(addItem)
        }
    }

    fun select(groupId: Int) {
        if (groupId in 0 until cGroups.size) selectedGroupIndex = groupId
        else if (cGroups.size == 0 && groupId == 0) doNothing()
        else throw IllegalArgumentException(String.format(I18N["exception.group_bar.group_id_invalid.i"], groupId))
    }

}
