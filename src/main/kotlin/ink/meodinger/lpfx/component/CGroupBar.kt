package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.NOT_FOUND
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.util.doNothing
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get

import javafx.beans.binding.Bindings
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.HBox
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
        const val C_GROUP_ID = "C_GROUP_ID"
    }

    private val onGroupSelectProperty: ObjectProperty<(String) -> Unit> = SimpleObjectProperty {}
    fun onGroupSelectedProperty(): ObjectProperty<(String) -> Unit> = onGroupSelectProperty
    val onGroupSelect: (String) -> Unit by onGroupSelectProperty
    fun setOnGroupSelect(consumer: (String) -> Unit) = onGroupSelectProperty.set(consumer)

    /**
     * This field now have no effect, but may be used in the future
     * Leave it here to make this component fit to reset-render-update standard (Like CTreeView)
     */
    private val transGroups: MutableList<TransGroup> = ArrayList()
    private val cGroups: MutableList<CGroup> = ArrayList()

    fun reset() {
        cGroups.clear()
        transGroups.clear()

        children.clear()
    }
    fun render(transGroups: List<TransGroup>) {
        reset()

        update(transGroups)
    }
    fun update(transGroups: List<TransGroup> = this.transGroups.toList()) {
        this.transGroups.clear()
        cGroups.clear()

        children.clear()

        for (transGroup in transGroups) createGroup(transGroup)
    }

    private fun tagCGroupId(cGroup: CGroup, groupId: Int) {
        cGroup.properties[C_GROUP_ID] = groupId
    }
    private fun useCGroupId(cGroup: CGroup): Int {
        return cGroup.properties[C_GROUP_ID] as Int
    }

    fun createGroup(transGroup: TransGroup) {
        transGroups.add(transGroup)

        val cGroup = CGroup().apply {
            tagCGroupId(this, cGroups.size)

            setOnMouseClicked { onGroupSelect(this@apply.name) }

            nameProperty().bind(transGroup.nameProperty)
            colorProperty().bind(Bindings.createObjectBinding(
                { Color.web(transGroup.colorHex) },
                transGroup.colorHexProperty
            ))
        }

        cGroups.add(cGroup)
        children.add(cGroup)

        if (cGroups.size == 1) select(0)
    }
    fun removeGroup(oldName: String) {
        var groupId: Int = NOT_FOUND
        for (i in cGroups.indices) if (cGroups[i].name == oldName) groupId = i

        if (groupId == NOT_FOUND) return

        // Edit tags
        for (cGroup in cGroups) {
            val tag = useCGroupId(cGroup)
            if (tag > groupId) tagCGroupId(cGroup, tag - 1)
        }

        val cGroup = cGroups[groupId]

        // Unbind
        cGroup.nameProperty().unbind()
        cGroup.colorProperty().unbind()

        // Remove view
        children.remove(cGroup)
        // Remove data
        cGroups.remove(cGroup)
        transGroups.removeAt(groupId)
    }

    fun select(groupId: Int) {
        if (groupId in 0 until cGroups.size) select(cGroups[groupId].name)
        else if (cGroups.size == 0 && groupId == 0) doNothing()
        else throw IllegalArgumentException(String.format(I18N["exception.group_bar.group_id_invalid.i"], groupId))
    }
    fun select(groupName: String) {
        for (cGroup in cGroups)
            if (cGroup.name == groupName) {
                cGroup.select()
            } else {
                cGroup.unselect()
            }
    }
    fun unselectAll() {
        for (node in children) if (node is CGroup) node.unselect()
    }

}
