package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.transform
import ink.meodinger.lpfx.util.string.emptyString
import ink.meodinger.lpfx.util.string.pad
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
    labelIndex: Int    = -1,
    labelText:  String = emptyString(),
    labelColor: Color? = null
) : TreeItem<String>() {

    companion object {
        /**
         * TreeItem Graphic radius
         */
        const val GRAPHICS_CIRCLE_RADIUS: Double = 8.0
    }

    // region Properties

    private val indexProperty: IntegerProperty = SimpleIntegerProperty(labelIndex)
    fun indexProperty(): IntegerProperty = indexProperty
    var index: Int by indexProperty

    private val textProperty: StringProperty = SimpleStringProperty(labelText)
    fun textProperty(): StringProperty = textProperty
    var text: String by textProperty

    private val colorProperty: ObjectProperty<Color?> = SimpleObjectProperty(labelColor)
    fun colorProperty(): ObjectProperty<Color?> = colorProperty
    var color: Color? by colorProperty

    // endregion

    init {
        graphicProperty().bind(colorProperty.transform { it?.let { Circle(GRAPHICS_CIRCLE_RADIUS, it) } })
        valueProperty().bind(Bindings.createStringBinding({ "${index.pad(2)}: ${text.replaceEOL(" ")}" }, indexProperty, textProperty))
    }

}
