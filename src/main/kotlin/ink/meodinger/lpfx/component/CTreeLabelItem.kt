package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.GRAPHICS_CIRCLE_RADIUS
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.string.replaceEOL

import javafx.beans.binding.Bindings
import javafx.beans.property.*
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
    labelIndex: Int    = DEFAULT_INDEX,
    labelText:  String = DEFAULT_TEXT,
    labelColor: Color? = null
) : TreeItem<String>() {

    companion object {
        private const val DEFAULT_INDEX = -1
        private const val DEFAULT_TEXT = ""
    }

    private val indexProperty: IntegerProperty = SimpleIntegerProperty(labelIndex)
    fun indexProperty(): IntegerProperty = indexProperty
    var index: Int by indexProperty

    private val textProperty: StringProperty = SimpleStringProperty(labelText)
    fun textProperty(): StringProperty = textProperty
    var text: String by textProperty

    private val colorProperty: ObjectProperty<Color?> = SimpleObjectProperty(labelColor)
    fun colorProperty(): ObjectProperty<Color?> = colorProperty
    var color: Color? by colorProperty

    init {
        graphicProperty().bind(Bindings.createObjectBinding(
            {
                color?.let { Circle(GRAPHICS_CIRCLE_RADIUS, color) }
            }, colorProperty
        ))
        valueProperty().bind(Bindings.createStringBinding(
            {
                "${String.format("%02d", index)}: ${text.replaceEOL(" ")}"
            }, indexProperty, textProperty
        ))
    }

}
