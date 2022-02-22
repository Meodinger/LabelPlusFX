package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.NOT_FOUND
import ink.meodinger.lpfx.component.singleton.ATreeMenu
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.util.addLast
import ink.meodinger.lpfx.util.component.boxHGrow
import ink.meodinger.lpfx.util.doNothing
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.property.onNew
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get

import javafx.beans.binding.Bindings
import javafx.beans.property.IntegerProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.input.MouseEvent
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
        private const val C_GROUP_ID = "C_GROUP_ID"
    }

    class GroupEvent(
        eventType: EventType<GroupEvent>,
        val source: MouseEvent,
        val groupName: String, val groupColor: Color,
        val labelX: Double, val labelY: Double,
        val displayX: Double, val displayY: Double,
    ) : Event(eventType) {
        companion object {
            val GROUP_ANY     = EventType<GroupEvent>(EventType.ROOT, "GROUP_ANY")
            val GROUP_POINTED = EventType(GROUP_ANY, "GROUP_POINTED")
            val GROUP_CLICKED = EventType(GROUP_ANY, "GROUP_CLICKED")
            val GROUP_SELECT  = EventType(GROUP_ANY, "GROUP_SELECT")
        }
    }

    private val onGroupPointedProperty: ObjectProperty<EventHandler<GroupEvent>> = SimpleObjectProperty(EventHandler {})
    private val onGroupClickedProperty: ObjectProperty<EventHandler<GroupEvent>> = SimpleObjectProperty(EventHandler {})
    private val onGroupSelectProperty:  ObjectProperty<EventHandler<GroupEvent>> = SimpleObjectProperty(EventHandler {})
    fun onGroupPointedProperty():       ObjectProperty<EventHandler<GroupEvent>> = onGroupPointedProperty
    fun onGroupClickedProperty():       ObjectProperty<EventHandler<GroupEvent>> = onGroupClickedProperty
    fun onGroupSelectProperty():        ObjectProperty<EventHandler<GroupEvent>> = onGroupSelectProperty
    val onGroupPointed:                                EventHandler<GroupEvent> by onGroupPointedProperty
    val onGroupClicked:                                EventHandler<GroupEvent> by onGroupClickedProperty
    val onGroupSelect:                                 EventHandler<GroupEvent> by onGroupSelectProperty
    fun setOnGroupPointed(handler: EventHandler<GroupEvent>)                     = onGroupPointedProperty.set(handler)
    fun setOnGroupClicked(handler: EventHandler<GroupEvent>)                     = onGroupClickedProperty.set(handler)
    fun setOnGroupSelect(handler: EventHandler<GroupEvent>)                      = onGroupSelectProperty.set(handler)

    private val groupListProperty: ObjectProperty<ObservableList<TransGroup>> = SimpleObjectProperty(FXCollections.observableArrayList())
    fun groupListProperty(): ObjectProperty<ObservableList<TransGroup>> = groupListProperty
    val groupList: ObservableList<TransGroup> by groupListProperty

    private val selectedGroupIndexProperty: IntegerProperty = SimpleIntegerProperty(NOT_FOUND)
    fun selectedGroupIndexProperty(): IntegerProperty = selectedGroupIndexProperty
    var selectedGroupIndex: Int by selectedGroupIndexProperty

    private val cGroups: MutableList<CGroup> = ArrayList()
    private val placeHolder: HBox = HBox().apply {
        boxHGrow = Priority.ALWAYS
    }
    private val addItem: CGroup = CGroup("+", Color.BLACK).apply {
        setOnMouseClicked { ATreeMenu.toggleGroupCreate() }
    }
    private val vShift = 2

    init {
        groupListProperty.addListener(onNew { update(it) })
        selectedGroupIndexProperty.addListener { _, o, n ->
            if ((o as Int) != NOT_FOUND && o < cGroups.size) cGroups[o].unselect()
            if ((n as Int) != NOT_FOUND && n < cGroups.size) cGroups[n].select()
        }
    }

    private fun update(transGroups: List<TransGroup>) {
        cGroups.clear()
        children.clear()

        if (transGroups.isNotEmpty()) {
            children.addLast(placeHolder)
            children.addLast(addItem)
        }
        for (transGroup in transGroups) {
            children.addLast(CGroup().apply {
                tagCGroupId(this, cGroups.size)

                /*
                 * TODO: Add event handle to CGroup
                 *     GroupEvent.Select,
                 *     GroupEvent.Pointed,
                 *     GroupEvent.Clicked,
                 */

                addEventHandler(MouseEvent.MOUSE_MOVED) {
                    onGroupPointed.handle(GroupEvent(
                        GroupEvent.GROUP_POINTED, it,
                        name, color,
                        it.x / width, it.y / height, it.x, it.y,
                    ))
                }
                addEventHandler(MouseEvent.MOUSE_CLICKED) {
                    onGroupClicked.handle(GroupEvent(
                        GroupEvent.GROUP_CLICKED, it,
                        name, color,
                        it.x / width, it.y / height, it.x, it.y,
                    ))
                }
                addEventHandler(MouseEvent.MOUSE_CLICKED) {
                    onGroupSelect.handle(GroupEvent(
                        GroupEvent.GROUP_SELECT, it,
                        name, color,
                        it.x / width, it.y / height, it.x, it.y,
                    ))
                    selectedGroupIndex = useCGroupId(this)
                }
                /*
                selectedProperty().addListener(onNew {
                    if (it) {
                        onGroupSelect.handle(GroupEvent(
                            GroupEvent.GROUP_SELECT, it,
                            name, color,
                            it.x / width, it.y / height, it.x, it.y,
                        ))
                        selectedGroupIndex = useCGroupId(this)
                    }
                })
                 */

                nameProperty().bind(transGroup.nameProperty)
                colorProperty().bind(Bindings.createObjectBinding(
                    { Color.web(transGroup.colorHex) },
                    transGroup.colorHexProperty
                ))
            }.also { cGroups.add(it) }, vShift)
        }

        if (selectedGroupIndex != NOT_FOUND && selectedGroupIndex < cGroups.size) select(selectedGroupIndex)
    }

    private fun tagCGroupId(cGroup: CGroup, groupId: Int) {
        cGroup.properties[C_GROUP_ID] = groupId
    }
    private fun useCGroupId(cGroup: CGroup): Int {
        return cGroup.properties[C_GROUP_ID] as Int
    }

    fun select(groupId: Int) {
        if (groupId in 0 until cGroups.size) selectedGroupIndex = groupId
        else if (cGroups.size == 0 && groupId == 0) doNothing()
        else throw IllegalArgumentException(String.format(I18N["exception.group_bar.group_id_invalid.i"], groupId))
    }

}
