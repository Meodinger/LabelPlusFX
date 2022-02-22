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
import javafx.beans.property.ListProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
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

    private val groupsProperty: ListProperty<TransGroup> = SimpleListProperty(FXCollections.observableArrayList())
    fun groupsProperty(): ListProperty<TransGroup> = groupsProperty
    val groups: ObservableList<TransGroup> by groupsProperty

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
        groupsProperty.addListener(onNew { transGroups ->
            cGroups.clear()
            children.clear()

            if (transGroups.isNotEmpty()) {
                children.addLast(placeHolder)
                children.addLast(addItem)
            }

            for (transGroup in transGroups) {
                children.addLast(CGroup().apply {
                    val groupId = cGroups.size // Don't put this into lambda

                    nameProperty().bind(transGroup.nameProperty)
                    colorProperty().bind(Bindings.createObjectBinding(
                        { Color.web(transGroup.colorHex) },
                        transGroup.colorHexProperty
                    ))
                    setOnSelect { select(groupId) }
                }.also { cGroups.add(it) }, vShift)
            }

            if (transGroups.isEmpty()) selectedGroupIndex = NOT_FOUND
            if (selectedGroupIndex != NOT_FOUND && selectedGroupIndex < cGroups.size) cGroups[selectedGroupIndex].select()
        })
        selectedGroupIndexProperty.addListener { _, o, n ->
            if ((o as Int) != NOT_FOUND && o < cGroups.size) cGroups[o].unselect()
            if ((n as Int) != NOT_FOUND && n < cGroups.size) cGroups[n].select()
        }
    }

    fun select(groupId: Int) {
        if (groupId in 0 until cGroups.size) selectedGroupIndex = groupId
        else if (cGroups.size == 0 && groupId == 0) doNothing()
        else throw IllegalArgumentException(String.format(I18N["exception.group_bar.group_id_invalid.i"], groupId))
    }

}
