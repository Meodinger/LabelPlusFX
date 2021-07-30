package info.meodinger.lpfx.component

import info.meodinger.lpfx.util.platform.MonoType

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.VPos
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.text.*


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.component
 */
class CLabel(index: Int, radius: Double, color: Color) : Region() {

    companion object {
        private const val MINIMUM_WIDTH = 16.0
        private const val MINIMUM_HEIGHT = 16.0
        private const val MAXIMUM_WIDTH = 64.0
        private const val MAXIMUM_HEIGHT = 64.0
    }

    private val circle = Circle(radius, color)
    private val text = Text(index.toString())

    val indexProperty = SimpleIntegerProperty(index)
    val radiusProperty = SimpleDoubleProperty(radius)
    val colorProperty = SimpleObjectProperty(color)

    var index: Int
        get() = indexProperty.value
        set(value) {
            indexProperty.value = value
            text.text = value.toString()
            update(radius, value)
        }
    var radius: Double
        get() = radiusProperty.value
        set(value) {
            radiusProperty.value = value
            circle.radius = value
            update(value, index)
        }
    var color: Color
        get() = colorProperty.value
        set(value) {
            colorProperty.value = value
            circle.fill = value
        }

    init {
        setPrefSize(radius, radius)

        text.fill = Color.WHITE
        text.textAlignment = TextAlignment.CENTER
        text.textOrigin = VPos.CENTER // avoid edit layoutY
        update(radius, index)

        children.setAll(circle, text)
    }

    // ----- Methods ----- //
    override fun computeMinWidth(height: Double): Double = MINIMUM_WIDTH
    override fun computeMinHeight(width: Double): Double = MINIMUM_HEIGHT
    override fun computeMaxWidth(height: Double): Double = MAXIMUM_WIDTH
    override fun computeMaxHeight(width: Double): Double = MAXIMUM_HEIGHT

    private fun update(radius: Double, index: Int) {

        // Font size vary from 1.8R to 1.3R
        // 0..9 -> 1.8R
        // 10.. -> 1.3R
        val r = if (index < 10) 1.8 * radius else 1.3 * radius
        text.font = Font.font(MonoType, FontWeight.BOLD, r)

        // Layout 0,0 is the center of the circle
        // Text display based on left-down corner
        // Axis is 0 →
        //         ↓
        text.layoutX = -text.layoutBounds.width / 2
    }
}