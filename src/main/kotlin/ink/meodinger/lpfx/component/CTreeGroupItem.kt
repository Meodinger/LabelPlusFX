package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.transform
import ink.meodinger.lpfx.util.string.emptyString

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
 * A TreeItem for TransGroup containing
 */
class CTreeGroupItem(
    groupName:  String = emptyString(),
    groupColor: Color  = Color.web("66CCFF")
) : TreeItem<String>() {

    // TODO: Use TransGroup

    companion object {
        /**
         * TreeItem Graphic radius
         */
        const val GRAPHICS_CIRCLE_RADIUS: Double = 8.0
    }

    // region Properties

    private val nameProperty: StringProperty = SimpleStringProperty(groupName)
    /**
     * The name of the TransGroup which this item stands for
     */
    fun nameProperty(): StringProperty = nameProperty
    /**
     * @see nameProperty
     */
    var name: String by nameProperty

    private val colorProperty: ObjectProperty<Color> = SimpleObjectProperty(groupColor)
    /**
     * The color of the TransGroup which this item stands for
     */
    fun colorProperty(): ObjectProperty<Color> = colorProperty
    /**
     * @see colorProperty
     */
    var color: Color by colorProperty

    // endregion

    init {
        graphicProperty().bind(colorProperty.transform { Circle(GRAPHICS_CIRCLE_RADIUS, it) })
        valueProperty().bind(nameProperty)
    }

}
