package info.meodinger.lpfx.component

import info.meodinger.lpfx.util.property.setValue
import info.meodinger.lpfx.util.property.getValue

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import java.util.function.Consumer


/**
 * Author: Meodinger
 * Date: 2021/9/30
 * Location: info.meodinger.lpfx.component
 */

/**
 * A ToolBar to display groups
 */
class CGroupBar : HBox() {

    companion object {
        const val C_GROUP_ID = "C_GROUP_ID"
    }

    val groupNamesProperty = SimpleListProperty<String>(FXCollections.emptyObservableList())
    var groupNames: ObservableList<String> by groupNamesProperty

    val groupColorsProperty = SimpleListProperty<Color>(FXCollections.emptyObservableList())
    var groupColors: ObservableList<Color> by groupColorsProperty

    val onGroupSelectProperty = SimpleObjectProperty<Consumer<String>>(Consumer {})
    var onGroupSelect: Consumer<String> by onGroupSelectProperty

    fun reset() {
        this.children.clear()
    }
    fun render(names: List<String>, colors: List<String>) {
        this.children.clear()

        groupNames = FXCollections.observableList(names)
        groupColors = FXCollections.observableList(List(colors.size) { Color.web(colors[it]) })

        for (i in names.indices) {
            val cGroup = CGroup(groupNames[i], groupColors[i]).also { tagCGroupId(it, i) }

            cGroup.setOnMouseClicked { onGroupSelect.accept(cGroup.name) }

            cGroup.nameProperty.bind(Bindings.createStringBinding(
                { groupNames[useCGroupId(cGroup)] },
                groupNamesProperty
            ))
            cGroup.colorProperty.bind(Bindings.createObjectBinding(
                { groupColors[useCGroupId(cGroup)] },
                groupColorsProperty
            ))

            this.children.add(cGroup)
        }
    }
    fun update(id: Int, name: String = groupNames[id], color: Color = groupColors[id]) {
        groupNames[id] = name
        groupColors[id] = color
    }

    private fun tagCGroupId(cGroup: CGroup, groupId: Int) {
        cGroup.properties[C_GROUP_ID] = groupId
    }
    private fun useCGroupId(cGroup: CGroup): Int {
        return cGroup.properties[C_GROUP_ID] as Int
    }

    fun select(groupName: String) {
        for (node in children) if (node is CGroup) {
            if (node.name == groupName) {
                node.select()
            } else {
                node.unselect()
            }
        }
    }
    /*
    fun unselectAll() {
        for (node in children) if (node is CGroup) node.unselect()
    }
     */

}