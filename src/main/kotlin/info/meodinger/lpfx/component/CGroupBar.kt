package info.meodinger.lpfx.component

import info.meodinger.lpfx.NOT_FOUND
import info.meodinger.lpfx.type.TransGroup
import info.meodinger.lpfx.util.doNothing
import info.meodinger.lpfx.util.property.getValue

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
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

    val onGroupSelectProperty = SimpleObjectProperty<Consumer<String>>(Consumer {})
    val onGroupSelect: Consumer<String> by onGroupSelectProperty
    fun setOnGroupSelect(consumer: Consumer<String>) {
        onGroupSelectProperty.value = consumer
    }

    fun reset() {
        this.groups.clear()
        this.children.clear()
    }
    fun render(transGroups: List<TransGroup>) {
        this.groups.clear()
        this.children.clear()
        for (transGroup in transGroups) addGroup(transGroup)
    }

    private fun tagCGroupId(cGroup: CGroup, groupId: Int) {
        cGroup.properties[C_GROUP_ID] = groupId
    }
    private fun useCGroupId(cGroup: CGroup): Int {
        return cGroup.properties[C_GROUP_ID] as Int
    }

    fun select(groupId: Int) {
        if (groupId in 0 until groups.size) select(groups[groupId].name)
        else if (groups.size == 0 && groupId == 0) doNothing()
        else throw IllegalArgumentException("GroupId $groupId invalid")
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

    fun addGroup(transGroup: TransGroup) {
        val cGroup = CGroup(transGroup.name, Color.web(transGroup.colorHex)).also { tagCGroupId(it, groups.size) }

        cGroup.setOnMouseClicked { onGroupSelect.accept(cGroup.name) }

        cGroup.nameProperty.bind(transGroup.nameProperty)
        cGroup.colorProperty.bind(Bindings.createObjectBinding(
            { Color.web(transGroup.colorHex) },
            transGroup.colorHexProperty
        ))

        groups.add(cGroup)
        children.add(cGroup)

        if (groups.size == 1) select(0)
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
    }

}