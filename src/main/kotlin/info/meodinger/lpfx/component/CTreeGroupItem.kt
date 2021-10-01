package info.meodinger.lpfx.component

import info.meodinger.lpfx.GRAPHICS_CIRCLE_RADIUS
import info.meodinger.lpfx.util.property.setValue
import info.meodinger.lpfx.util.property.getValue

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TreeItem
import javafx.scene.paint.Color
import javafx.scene.shape.Circle


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.component
 */

/**
 * A TreeItem for TransGroup containing
 */
class CTreeGroupItem(name: String, color: Color) : TreeItem<String>() {

    val nameProperty = SimpleStringProperty(name)
    var name: String by nameProperty

    val colorProperty = SimpleObjectProperty(color)
    var color: Color by colorProperty

    init {
        this.graphic = Circle(GRAPHICS_CIRCLE_RADIUS, color)

        nameProperty.addListener { _, _, newName -> update(name = newName) }
        colorProperty.addListener { _, _, newColor -> update(color = newColor) }

        update()
    }

    private fun update(name: String = this.name, color: Color = this.color) {
        this.value = name
        (this.graphic as Circle).fill = color
    }

}