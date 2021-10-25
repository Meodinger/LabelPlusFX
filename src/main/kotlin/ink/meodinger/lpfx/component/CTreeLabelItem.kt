package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.GRAPHICS_CIRCLE_RADIUS
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.property.getValue

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TreeItem
import javafx.scene.paint.Color
import javafx.scene.shape.Circle


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: ink.meodinger.lpfx.component
 */

/**
 * A TreeItem for TransLabel containing
 */
class CTreeLabelItem(index: Int, text: String, color: Color? = null) : TreeItem<String>() {

    val indexProperty = SimpleIntegerProperty(index)
    var index: Int by indexProperty

    val textProperty = SimpleStringProperty(text)
    var text: String by textProperty

    init {
        if (color != null) this.graphic = Circle(GRAPHICS_CIRCLE_RADIUS, color)

        indexProperty.addListener { _, _, newIndex -> update(index = newIndex as Int) }
        textProperty.addListener { _, _, newText -> update(text = newText) }

        update()
    }

    private fun update(index: Int = this.index, text: String = this.text) {
        this.value = "${String.format("%02d", index)}: ${text.replace("\n", " ")}"
    }

}