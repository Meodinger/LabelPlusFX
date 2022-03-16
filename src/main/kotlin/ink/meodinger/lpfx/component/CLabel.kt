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
    labelIndex:  Int    = DEFAULT_INDEX,
    labelRadius: Double = DEFAULT_RADIUS,
    labelColor:  Color  = Color.web(DEFAULT_COLOR),
) : Region() {

    /// TODO: Custom Text Color

    companion object {
        private const val DEFAULT_INDEX = -1
        private const val DEFAULT_RADIUS = 24.0
        private const val DEFAULT_COLOR = "66CCFF"

        const val MIN_PICK_RADIUS = 16.0
    }

    private val text = Text()
    private val circle = Circle()
    private var contour: Shape = circle

    private val indexProperty: IntegerProperty = SimpleIntegerProperty(labelIndex)
    fun indexProperty(): IntegerProperty = indexProperty
    var index: Int by indexProperty

    private val radiusProperty: DoubleProperty = SimpleDoubleProperty(labelRadius)
    fun radiusProperty(): DoubleProperty = radiusProperty
    var radius: Double by radiusProperty

    private val colorProperty: ObjectProperty<Color> = SimpleObjectProperty(labelColor)
    fun colorProperty(): ObjectProperty<Color> = colorProperty
    var color: Color by colorProperty

    private val colorOpacityProperty: DoubleProperty = SimpleDoubleProperty(1.0)
    fun colorOpacityProperty(): DoubleProperty = colorOpacityProperty
    var colorOpacity: Double by colorOpacityProperty

    private val textOpaqueProperty: BooleanProperty = SimpleBooleanProperty(false)
    fun textOpaqueProperty(): BooleanProperty = textOpaqueProperty
    var isTextOpaque: Boolean by textOpaqueProperty

    init {
        text.textAlignment = TextAlignment.CENTER
        text.textOrigin = VPos.CENTER // to get rid of editing layoutY

        indexProperty.addListener(onNew<Number, Int> { updateShape(index = it) })
        radiusProperty.addListener(onNew<Number, Double> { updateShape(radius = it) })
        colorProperty.addListener(onNew { updateColor(color = it) })
        colorOpacityProperty.addListener(onNew<Number, Double> { updateColor(colorOpacity = it) })
        textOpaqueProperty.addListener(onNew { updateColor(isTextOpaque = it) })

        update()
    }

    private fun update(
        index: Int = this.index,
        radius: Double = this.radius,
        color: Color = this.color,
        colorOpacity: Double = this.colorOpacity,
        isTextOpaque: Boolean = this.isTextOpaque
    ) {
        updateShape(index, radius)
        updateColor(color, colorOpacity, isTextOpaque)
    }

    private fun updateShape(
        index: Int = this.index,
        radius: Double = this.radius
    ) {
        children.clear()

        text.text = index.toString()
        circle.radius = radius

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

        val shape = Shape.subtract(circle, text).apply {
            fill = contour.fill
        }.also { contour = it }

        children.addAll(shape, text)
    }

    private fun updateColor(
        color: Color = this.color,
        colorOpacity: Double = this.colorOpacity,
        isTextOpaque: Boolean = this.isTextOpaque
    ) {
        text.fill = if (isTextOpaque) Color.WHITE else Color.WHITE.opacity(colorOpacity)
        contour.fill = color.opacity(colorOpacity)
    }
}
