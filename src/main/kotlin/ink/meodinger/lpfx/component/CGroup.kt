package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.onChange
import ink.meodinger.lpfx.util.property.onNew

import javafx.beans.binding.Bindings
import javafx.beans.property.*
import javafx.event.ActionEvent
import javafx.event.EventHandler
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
 */
class CGroup(
    name:  String = DEFAULT_NAME,
    color: Color  = Color.web(DEFAULT_COLOR_HEX)
) : Region() {

    companion object {
        private const val DEFAULT_NAME = ""
        private const val DEFAULT_COLOR_HEX = "66CCFF"

        private const val CORNER_RADII = 4.0
        private const val BORDER_WIDTH = 1.0
        private const val PADDING = 4.0
    }

    private val text: Text = Text(name).apply {
        fill = color
        textAlignment = TextAlignment.CENTER
        textOrigin = VPos.CENTER
    }

    private val nameProperty: StringProperty = SimpleStringProperty(name)
    fun nameProperty(): StringProperty = nameProperty
    var name: String by nameProperty

    private val colorProperty: ObjectProperty<Color> = SimpleObjectProperty(color)
    fun colorProperty(): ObjectProperty<Color> = colorProperty
    var color: Color by colorProperty

    private val selectedProperty: BooleanProperty = SimpleBooleanProperty(false)
    fun selectedProperty(): BooleanProperty = selectedProperty
    var selected: Boolean by selectedProperty

    private val onSelectProperty: ObjectProperty<EventHandler<ActionEvent>> = SimpleObjectProperty(EventHandler {})
    fun onSelectProperty(): ObjectProperty<EventHandler<ActionEvent>> = onSelectProperty
    val onSelect: EventHandler<ActionEvent> by onSelectProperty
    fun setOnSelect(handler: EventHandler<ActionEvent>) = onSelectProperty.set(handler)

    init {
        padding = Insets(PADDING)

        nameProperty.addListener(onNew { update(name = it) })
        colorProperty.addListener(onNew { update(color = it) })

        backgroundProperty().bind(Bindings.createObjectBinding({
            if (selected) Background(BackgroundFill(
                Color.LIGHTGRAY,
                CornerRadii(CORNER_RADII),
                Insets(0.0)
            )) else null
        }, selectedProperty))
        borderProperty().bind(Bindings.createObjectBinding({
            Border(BorderStroke(
                if (isHover) this.color else Color.TRANSPARENT,
                BorderStrokeStyle.SOLID,
                CornerRadii(CORNER_RADII),
                BorderWidths(BORDER_WIDTH)
            ))
        }, hoverProperty()))

        children.add(text)

        widthProperty().addListener(onChange { text.layoutX = (width - text.boundsInLocal.width) / 2 })

        update()
    }

    private fun update(name: String = this.name, color: Color = this.color) {
        text.text = name
        text.fill = color

        val textW = text.boundsInLocal.width
        val textH = text.boundsInLocal.height
        val prefW = textW + (BORDER_WIDTH + PADDING) * 2
        val prefH = textH + (BORDER_WIDTH + PADDING) * 2

        // TEXT----
        // |      |
        // --------
        // Layout first to avoid width listener overwrite value set there
        text.layoutX = BORDER_WIDTH + PADDING
        text.layoutY = prefH / 2

        setPrefSize(prefW, prefH)
    }

    fun select() {
        selected = true
        onSelect.handle(ActionEvent(name, this))
    }

    fun unselect() {
        selected = false
    }
}
