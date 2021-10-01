package info.meodinger.lpfx.component

import info.meodinger.lpfx.util.property.setValue
import info.meodinger.lpfx.util.property.getValue

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.Node
import javafx.scene.control.TreeItem


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.component
 */

/**
 * A TreeItem for TransLabel containing
 */
class CTreeItem(index: Int, text: String, node: Node? = null) : TreeItem<String>() {

    val indexProperty = SimpleIntegerProperty(index)
    var index: Int by indexProperty

    val textProperty = SimpleStringProperty(text)
    var text: String by textProperty

    init {
        this.graphic = node

        indexProperty.addListener { _, _, newIndex -> update(index = newIndex as Int) }
        textProperty.addListener { _, _, newText -> update(text = newText) }

        update()
    }

    private fun update(index: Int = this.index, text: String = this.text) {
        this.value = "${String.format("%02d", index)}: ${text.replace("\n", " ")}"
    }

}