package info.meodinger.lpfx.component

import info.meodinger.lpfx.State
import info.meodinger.lpfx.type.TransLabel

import javafx.scene.control.TreeItem
import javafx.scene.paint.Color
import javafx.scene.shape.Circle

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.component
 */
class CTreeItem(val meta: TransLabel) : TreeItem<String>() {

    var index: Int
        get() = meta.index
        set(value) {
            meta.index = value
            update()
        }
    var groupId: Int
        get() = meta.groupId
        set(value) {
            meta.groupId = value
            update()
        }
    var text: String
        get() = meta.text
        set(value) {
            meta.text = value
            update()
        }

    init {
        update()
    }

    private fun update() {
        value = "${String.format("%02d",index)}: ${text.replace("\n", " ")}"
        if (parent.graphic == null) graphic = Circle(8.0, Color.web(State.transFile!!.groupList[groupId].color))
    }

}