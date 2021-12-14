package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.GRAPHICS_CIRCLE_RADIUS
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.onNew

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
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
 * A TreeItem for TransGroup containing
 */
class CTreeGroupItem(
    name:  String = DEFAULT_NAME,
    color: Color  = Color.web(DEFAULT_COLOR_HEX)
) : TreeItem<String>() {

    companion object {
        private const val DEFAULT_NAME = ""
        private const val DEFAULT_COLOR_HEX = "66CCFFFF"
    }

    private val nameProperty: StringProperty = SimpleStringProperty(name)
    fun nameProperty(): StringProperty = nameProperty
    var name: String by nameProperty

    private val colorProperty: ObjectProperty<Color> = SimpleObjectProperty(color)
    fun colorProperty(): ObjectProperty<Color> = colorProperty
    var color: Color by colorProperty

    init {
        graphic = Circle(GRAPHICS_CIRCLE_RADIUS, color)

        nameProperty.addListener(onNew { update(name = it) })
        colorProperty.addListener(onNew { update(color = it) })

        update()
    }

    private fun update(name: String = this.name, color: Color = this.color) {
        value = name
        (graphic as Circle).fill = color
    }

}
