package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.util.platform.MonoFont
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.onNew
import ink.meodinger.lpfx.util.property.setValue

import javafx.beans.property.*
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

    companion object {
        private const val DEFAULT_INDEX = -1
        private const val DEFAULT_RADIUS = 16.0
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
        text.fill = Color.WHITE
        text.textAlignment = TextAlignment.CENTER
        text.textOrigin = VPos.CENTER // to get rid of editing layoutY

        indexProperty.addListener(onNew<Number, Int> { update(index = it) })
        radiusProperty.addListener(onNew<Number, Double> { update(radius = it) })
        colorProperty.addListener(onNew { update(color = it) })

        children.setAll(circle, text)
        update()
    }

    private fun update(index: Int = this.index, radius: Double = this.radius, color: Color = this.color) {
        val pickerSize = radius.coerceAtLeast(MIN_PICK_RADIUS)
        setPrefSize(pickerSize * 2, pickerSize * 2)

        text.text = index.toString()
        circle.radius = radius
        circle.fill = color

        // Font size vary from 1.7R to 1.3R
        // 0..9 -> 1.7R
        // 10.. -> 1.3R
        val r = if (index < 10) 1.7 * radius else 1.3 * radius
        text.font = Font.font(MonoFont, FontWeight.BOLD, r)

        // Set to zero
        text.layoutX = 0.0
        text.layoutY = 0.0
        circle.layoutX = 0.0
        circle.layoutY = 0.0

        // Layout 0,0 is the center of the circle, the left-top of the rect
        // Text display based on left-down corner
        // Axis is 0 →
        //         ↓
        text.layoutX = -text.layoutBounds.width / 2

        // Move to Region Center
        text.layoutX += prefWidth / 2
        text.layoutY += prefHeight / 2
        circle.layoutX += prefWidth / 2
        circle.layoutY += prefHeight / 2
    }
}
