package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.GRAPHICS_CIRCLE_RADIUS
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.onNew
import ink.meodinger.lpfx.util.string.replaceLineFeed

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.scene.control.TreeItem
import javafx.scene.paint.Color
import javafx.scene.shape.Circle


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * A TreeItem for TransLabel containing
 */
class CTreeLabelItem(
    index: Int    = DEFAULT_INDEX,
    text:  String = DEFAULT_TEXT,
    color: Color? = null
) : TreeItem<String>() {

    companion object {
        private const val DEFAULT_INDEX = -1
        private const val DEFAULT_TEXT = ""
    }

    private val indexProperty: IntegerProperty = SimpleIntegerProperty(index)
    fun indexProperty(): IntegerProperty = indexProperty
    var index: Int by indexProperty

    private val textProperty: StringProperty = SimpleStringProperty(text)
    fun textProperty(): StringProperty = textProperty
    var text: String by textProperty

    init {
        if (color != null) graphic = Circle(GRAPHICS_CIRCLE_RADIUS, color)

        indexProperty.addListener(onNew<Number, Int> { update(index = it) })
        textProperty.addListener(onNew { update(text = it) })

        update()
    }

    private fun update(index: Int = this.index, text: String = this.text) {
        value = "${String.format("%02d", index)}: ${text.replaceLineFeed(" ")}"
    }

}
