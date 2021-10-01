package info.meodinger.lpfx.component

import info.meodinger.lpfx.util.component.invoke
import info.meodinger.lpfx.util.property.setValue
import info.meodinger.lpfx.util.property.getValue

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.VPos
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment


/**
 * Author: Meodinger
 * Date: 2021/9/20
 * Location: info.meodinger.lpfx.component
 */

/**
 * A Region that displays a TransGroup
 */
class CGroup(name: String, color: Color) : Region() {

    companion object {
        private const val CORNER_RADII = 4.0
        private const val BORDER_WIDTH = 1.0
        private const val PADDING = 2.0
    }

    private val backgroundSelected: Background = Background(BackgroundFill(
        Color.LIGHTGRAY,
        CornerRadii(CORNER_RADII),
        Insets(0.0)
    ))
    private val borderDefault: Border = Border(BorderStroke(
        Color.TRANSPARENT,
        BorderStrokeStyle.NONE,
        CornerRadii(CORNER_RADII),
        BorderWidths(BORDER_WIDTH)
    ))

    private val text: Text = Text(name)(color, TextAlignment.CENTER, VPos.CENTER)

    val nameProperty = SimpleStringProperty(name)
    var name: String by nameProperty

    val colorProperty = SimpleObjectProperty(color)
    var color: Color by colorProperty

    init {
        this.padding = Insets(PADDING)
        this.border = borderDefault
        this.hoverProperty().addListener { _, _, isHover ->
            if (isHover) this.border = Border(BorderStroke(
                color,
                BorderStrokeStyle.SOLID,
                CornerRadii(CORNER_RADII),
                BorderWidths(BORDER_WIDTH)
            ))
            else this.border = borderDefault
        }

        nameProperty.addListener { _ ,_ , newName -> update(name = newName) }
        colorProperty.addListener { _ ,_ , newColor -> update(color = newColor) }

        this.children.add(text)

        update()
    }

    private fun update(name: String = this.name, color: Color = this.color) {
        text.text = name
        text.fill = color

        val textW = text.boundsInLocal.width
        val textH = text.boundsInLocal.height
        val prefW = textW + (BORDER_WIDTH + PADDING) * 2
        val prefH = textH + (BORDER_WIDTH + PADDING) * 2

        this.setPrefSize(prefW, prefH)
        text.layoutX = (-textW + prefW) / 2
        text.layoutY = prefH / 2
    }

    fun select() {
        this.background = backgroundSelected
    }

    fun unselect() {
        this.background = null
    }
}