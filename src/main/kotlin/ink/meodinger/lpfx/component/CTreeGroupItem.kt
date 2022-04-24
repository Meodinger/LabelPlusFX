package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.GRAPHICS_CIRCLE_RADIUS
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.string.emptyString

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
 * A TreeItem for TransGroup containing
 */
class CTreeGroupItem(
    groupName:  String = emptyString(),
    groupColor: Color  = Color.web("66CCFF")
) : TreeItem<String>() {

    private val nameProperty: StringProperty = SimpleStringProperty(groupName)
    fun nameProperty(): StringProperty = nameProperty
    var name: String by nameProperty

    private val colorProperty: ObjectProperty<Color> = SimpleObjectProperty(groupColor)
    fun colorProperty(): ObjectProperty<Color> = colorProperty
    var color: Color by colorProperty

    init {
        graphicProperty().bind(Bindings.createObjectBinding(
            {
                Circle(GRAPHICS_CIRCLE_RADIUS, color)
            }, colorProperty
        ))
        valueProperty().bind(nameProperty)
    }

}
