package info.meodinger.lpfx.component

import info.meodinger.lpfx.util.platform.MonoType

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.VPos
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.component
 */
class CLabel(
    index: Int = 0,
    radius: Double = 16.0,
    color: String = "66CCFF"
) : Region() {

    private val circle = Circle(radius, Color.web(color))
    private val text = Text(index.toString())

    val indexProperty = SimpleIntegerProperty(index)
    val radiusProperty = SimpleDoubleProperty(radius)
    val colorProperty = SimpleStringProperty(color)

    var index: Int
        get() = indexProperty.value
        set(value) {
            indexProperty.value = value
            update()
        }
    var radius: Double
        get() = radiusProperty.value
        set(value) {
            radiusProperty.value = value
            update()
        }
    var color: String
        get() = colorProperty.value
        set(value) {
            colorProperty.value = value
            update()
        }

    init {

        text.fill = Color.WHITE
        text.textAlignment = TextAlignment.CENTER
        text.textOrigin = VPos.CENTER // to avoid edit layoutY

        setPrefSize(radius, radius)
        update()
        children.setAll(circle, text)
    }

    private fun update() {
        text.text = index.toString()
        circle.radius = radius
        circle.fill = Color.web(color)

        // Font size vary from 1.7R to 1.3R
        // 0..9 -> 1.7R
        // 10.. -> 1.3R
        val r = if (index < 10) 1.7 * radius else 1.3 * radius
        text.font = Font.font(MonoType, FontWeight.BOLD, r)

        // Layout 0,0 is the center of the circle
        // Text display based on left-down corner
        // Axis is 0 →
        //         ↓
        text.layoutX = -text.layoutBounds.width / 2
    }
}