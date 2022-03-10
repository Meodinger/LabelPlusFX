package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.util.color.opacity
import ink.meodinger.lpfx.util.platform.MonoFont
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.onNew
import ink.meodinger.lpfx.util.property.setValue

import javafx.beans.property.*
import javafx.geometry.VPos
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Shape
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * A Label component for LabelPane
 */
class CLabel(
    index:  Int    = DEFAULT_INDEX,
    radius: Double = DEFAULT_RADIUS,
    color:  Color  = Color.web(DEFAULT_COLOR)
) : Region() {

    /// TODO: Custom Text Color
    /// TODO: Add alpha property

    companion object {
        private const val DEFAULT_INDEX = -1
        private const val DEFAULT_RADIUS = 24.0
        private const val DEFAULT_COLOR = "66CCFF"

        const val MIN_PICK_RADIUS = 16.0
    }

    private val circle = Circle()
    private val text = Text()

    private val indexProperty: IntegerProperty = SimpleIntegerProperty(index)
    fun indexProperty(): IntegerProperty = indexProperty
    var index: Int by indexProperty

    private val radiusProperty: DoubleProperty = SimpleDoubleProperty(radius)
    fun radiusProperty(): DoubleProperty = radiusProperty
    var radius: Double by radiusProperty

    private val colorProperty: ObjectProperty<Color> = SimpleObjectProperty(color)
    fun colorProperty(): ObjectProperty<Color> = colorProperty
    var color: Color by colorProperty

    init {
        text.textAlignment = TextAlignment.CENTER
        text.textOrigin = VPos.CENTER // to get rid of editing layoutY

        indexProperty.addListener(onNew<Number, Int> { update(index = it) })
        radiusProperty.addListener(onNew<Number, Double> { update(radius = it) })
        colorProperty.addListener(onNew { update(color = it) })

        update()
    }

    private fun update(index: Int = this.index, radius: Double = this.radius, color: Color = this.color) {
        children.clear()

        text.text = index.toString()
        text.fill = Color.WHITE.opacity(color.opacity)
        circle.radius = radius
        circle.fill = color

        // Font size vary from 1.7R to 1.3R
        // 0..9 -> 1.7R
        // 10.. -> 1.3R
        val r = if (index < 10) 1.7 * radius else 1.3 * radius
        text.font = Font.font(MonoFont, FontWeight.BOLD, r)

        val pickerRadius = radius.coerceAtLeast(MIN_PICK_RADIUS)
        // Circle display based on center
        // Text display based on left-center
        // 0 →
        // ↓
        // Move to Region Center

        text.layoutX = pickerRadius - text.boundsInLocal.width / 2
        text.layoutY = pickerRadius

        circle.centerX = pickerRadius
        circle.centerY = pickerRadius

        setPrefSize(pickerRadius * 2, pickerRadius * 2)

        children.addAll(Shape.subtract(circle, text).apply { fill = color }, text)
    }
}
