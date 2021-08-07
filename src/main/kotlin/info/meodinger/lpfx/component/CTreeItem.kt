package info.meodinger.lpfx.component

import info.meodinger.lpfx.type.TransLabel

import javafx.scene.Node
import javafx.scene.control.TreeItem

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.component
 */
class CTreeItem(val meta: TransLabel, node: Node? = null) : TreeItem<String>() {

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
        if (node != null) graphic = node
    }

    private fun update() {
        value = "${String.format("%02d",index)}: ${text.replace("\n", " ")}"
    }

}