package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.util.component.invoke
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.property.getValue

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.geometry.Insets
import javafx.geometry.VPos
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment


/**
 * Author: Meodinger
 * Date: 2021/9/20
 * Have fun with my code!
 */

/**
 * A Region that displays a TransGroup
 *
 * Bind Status: All bind
 */
class CGroup(
    name:  String = DEFAULT_NAME,
    color: Color  = Color.web(DEFAULT_COLOR_HEX)
) : Region() {

    companion object {
        private const val DEFAULT_NAME = ""
        private const val DEFAULT_COLOR_HEX = "66CCFFFF"

        private const val CORNER_RADII = 4.0
        private const val BORDER_WIDTH = 1.0
        private const val PADDING = 4.0
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

    private val nameProperty: StringProperty = SimpleStringProperty(name)
    fun nameProperty(): StringProperty = nameProperty
    var name: String by nameProperty

    private val colorProperty: ObjectProperty<Color> = SimpleObjectProperty(color)
    fun colorProperty(): ObjectProperty<Color> = colorProperty
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
