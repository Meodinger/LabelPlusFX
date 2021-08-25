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

    val index: Int
        get() = meta.index

    init {
        graphic = node

        meta.textProperty.addListener { _, _, _ -> update() }
        meta.indexProperty.addListener { _,_,_ -> update() }

        update()
    }

    private fun update() {
        value = "${String.format("%02d",meta.index)}: ${meta.text.replace("\n", " ")}"
    }

}