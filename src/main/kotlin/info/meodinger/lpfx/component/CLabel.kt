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
    index: Int = DEFAULT_INDEX,
    radius: Double = DEFAULT_RADIUS,
    color: String = DEFAULT_COLOR
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
    val radiusProperty = SimpleDoubleProperty(radius)
    val colorProperty = SimpleStringProperty(color)

    var index: Int
        get() = indexProperty.value
        set(value) {
            indexProperty.value = value
        }
    var radius: Double
        get() = radiusProperty.value
        set(value) {
            radiusProperty.value = value
        }
    var color: String
        get() = colorProperty.value
        set(value) {
            colorProperty.value = value
        }

    init {
        text.fill = Color.WHITE
        text.textAlignment = TextAlignment.CENTER
        text.textOrigin = VPos.CENTER // to avoid edit layoutY

        indexProperty.addListener { _, _, _ -> update() }
        radiusProperty.addListener { _, _, _ -> update() }
        colorProperty.addListener { _, _, _ -> update() }

        this.children.setAll(circle, text)
        update()
    }

    private fun update() {
        val pickerSize = radius.coerceAtLeast(MIN_PICK_RADIUS)
        setPrefSize(pickerSize * 2, pickerSize * 2)

        text.text = index.toString()
        circle.radius = radius
        circle.fill = Color.web(color)

        // Font size vary from 1.7R to 1.3R
        // 0..9 -> 1.7R
        // 10.. -> 1.3R
        val r = if (index < 10) 1.7 * radius else 1.3 * radius
        text.font = Font.font(MonoType, FontWeight.BOLD, r)

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