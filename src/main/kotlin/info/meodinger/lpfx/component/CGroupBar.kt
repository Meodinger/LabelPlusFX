package info.meodinger.lpfx.component

import info.meodinger.lpfx.NOT_FOUND
import info.meodinger.lpfx.type.TransGroup
import info.meodinger.lpfx.util.property.setValue
import info.meodinger.lpfx.util.property.getValue

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
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

    private val groups = ArrayList<CGroup>()

    val groupNamesProperty = SimpleListProperty<String>(FXCollections.emptyObservableList())
    val groupColorsProperty = SimpleListProperty<Color>(FXCollections.emptyObservableList())
    var groupNames: ObservableList<String> by groupNamesProperty
    var groupColors: ObservableList<Color> by groupColorsProperty

    val onGroupSelectProperty = SimpleObjectProperty<Consumer<String>>(Consumer {})
    val onGroupSelect: Consumer<String> by onGroupSelectProperty
    fun setOnGroupSelect(consumer: Consumer<String>) {
        onGroupSelectProperty.value = consumer
    }

    fun reset() {
        children.clear()
    }
    fun render(names: List<String>, colors: List<String>) {
        this.children.clear()

        groupNames = FXCollections.observableList(names)
        groupColors = FXCollections.observableList(List(colors.size) { Color.web(colors[it]) })

        for (i in names.indices) addGroup(groupNames[i], groupColors[i])
    }

    private fun tagCGroupId(cGroup: CGroup, groupId: Int) {
        cGroup.properties[C_GROUP_ID] = groupId
    }
    private fun useCGroupId(cGroup: CGroup): Int {
        return cGroup.properties[C_GROUP_ID] as Int
    }

    fun select(groupName: String) {
        for (node in groups)
            if (node.name == groupName) {
                node.select()
            } else {
                node.unselect()
            }
    }
    fun unselectAll() {
        for (node in children) if (node is CGroup) node.unselect()
    }

    fun addGroup(name: String, color: Color) {
        val cGroup = CGroup(name, color).also { tagCGroupId(it, groups.size) }

        cGroup.setOnMouseClicked { onGroupSelect.accept(cGroup.name) }

        cGroup.nameProperty.bind(Bindings.createStringBinding(
            { groupNames[useCGroupId(cGroup)] },
            groupNamesProperty
        ))
        cGroup.colorProperty.bind(Bindings.createObjectBinding(
            { groupColors[useCGroupId(cGroup)] },
            groupColorsProperty
        ))

        groups.add(cGroup)
        children.add(cGroup)
    }
    fun removeGroup(oldName: String) {
        var id: Int = NOT_FOUND
        for (i in groups.indices) if (groups[i].name == oldName) id = i

        if (id == NOT_FOUND) return

        for (cGroup in groups) {
            val tag = useCGroupId(cGroup)
            if (tag > id) tagCGroupId(cGroup, tag - 1)
        }

        val toRemove = groups[id]
        groups.remove(toRemove)
        children.remove(toRemove)
        groupNames.remove(toRemove.name)
        groupColors.remove(toRemove.color)
    }
    fun updateGroup(oldName: String, name: String? = null, color: Color? = null) {
        var id: Int = NOT_FOUND
        for (i in groups.indices) if (groups[i].name == oldName) id = i

        if (id == NOT_FOUND) return

        if (name != null) groupNames[id] = name
        if (color != null) groupColors[id] = color
    }

}