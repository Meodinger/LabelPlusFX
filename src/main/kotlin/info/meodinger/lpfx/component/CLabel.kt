package info.meodinger.lpfx.component

import info.meodinger.lpfx.util.platform.MonoType

import info.meodinger.lpfx.util.property.getValue
import info.meodinger.lpfx.util.property.setValue

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
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

/**
 * A Label component for LabelPane
 */
class CLabel(
    index: Int = DEFAULT_INDEX,
    radius: Double = DEFAULT_RADIUS,
    color: Color = Color.web(DEFAULT_COLOR)
) : Region() {

    companion object {
        const val DEFAULT_INDEX = -1
        const val DEFAULT_RADIUS = 16.0
        const val DEFAULT_COLOR = "66CCFFFF"

        const val MIN_PICK_RADIUS = 16.0
    }

    private val circle = Circle()
    private val text = Text()

    val indexProperty = SimpleIntegerProperty(index)
    var index: Int by indexProperty

    val radiusProperty = SimpleDoubleProperty(radius)
    var radius: Double by radiusProperty

    val colorProperty = SimpleObjectProperty(color)
    var color: Color by colorProperty

    init {
        text.fill = Color.WHITE
        text.textAlignment = TextAlignment.CENTER
        text.textOrigin = VPos.CENTER // to avoid edit layoutY

        indexProperty.addListener { _, _, newIndex -> update(index = newIndex as Int) }
        radiusProperty.addListener { _, _, newRadius -> update(radius = newRadius as Double) }
        colorProperty.addListener { _, _, newColor -> update(color = newColor) }

        this.children.setAll(circle, text)
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
        text.font = Font.font(MonoType, FontWeight.BOLD, r)

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