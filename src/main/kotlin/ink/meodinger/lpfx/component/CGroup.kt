package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.string.emptyString

import javafx.beans.binding.Bindings
import javafx.beans.property.*
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.VPos
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Text


/**
 * Author: Meodinger
 * Date: 2021/9/20
 * Have fun with my code!
 */

/**
 * A Region that displays a TransGroup
 */
class CGroup(
    groupName:  String = emptyString(),
    groupColor: Color  = Color.web("66CCFF")
) : Region() {

    companion object {
        private const val CORNER_RADII = 4.0
        private const val BORDER_WIDTH = 1.0
        private const val PADDING = 4.0
    }

    private val nameProperty: StringProperty = SimpleStringProperty(groupName)
    fun nameProperty(): StringProperty = nameProperty
    var name: String by nameProperty

    private val colorProperty: ObjectProperty<Color> = SimpleObjectProperty(groupColor)
    fun colorProperty(): ObjectProperty<Color> = colorProperty
    var color: Color by colorProperty

    private val selectedProperty: BooleanProperty = SimpleBooleanProperty(false)
    fun selectedProperty(): BooleanProperty = selectedProperty
    var isSelected: Boolean by selectedProperty

    private val onSelectProperty: ObjectProperty<EventHandler<ActionEvent>> = SimpleObjectProperty(EventHandler {})
    fun onSelectProperty(): ObjectProperty<EventHandler<ActionEvent>> = onSelectProperty
    val onSelect: EventHandler<ActionEvent> by onSelectProperty
    fun setOnSelect(handler: EventHandler<ActionEvent>) = onSelectProperty.set(handler)

    init {
        padding = Insets(PADDING)
        addEventHandler(MouseEvent.MOUSE_CLICKED) {
            select()
            onSelect.handle(ActionEvent(it.source, this))
        }

        val text = Text().apply {
            textOrigin = VPos.TOP
            textProperty().bind(nameProperty)
            fillProperty().bind(colorProperty)
            layoutXProperty().bind(Bindings.createDoubleBinding({ (width - boundsInLocal.width) / 2 }, widthProperty()))
            layoutYProperty().bind(Bindings.createDoubleBinding({ (height - boundsInLocal.height) / 2 }, heightProperty()))
        }

        backgroundProperty().bind(Bindings.createObjectBinding(
            {
                if (isSelected) Background(BackgroundFill(
                    Color.LIGHTGRAY,
                    CornerRadii(CORNER_RADII),
                    Insets(0.0)
                )) else null
            }, selectedProperty
        ))
        borderProperty().bind(Bindings.createObjectBinding(
            {
                Border(BorderStroke(
                    if (isHover) this.color else Color.TRANSPARENT,
                    BorderStrokeStyle.SOLID,
                    CornerRadii(CORNER_RADII),
                    BorderWidths(BORDER_WIDTH)
                ))
            }, hoverProperty()
        ))
        prefWidthProperty().bind(Bindings.createDoubleBinding(
            {
                text.boundsInLocal.width + (BORDER_WIDTH + PADDING) * 2
            }, text.textProperty()
        ))
        prefHeightProperty().bind(Bindings.createDoubleBinding(
            {
                text.boundsInLocal.height + (BORDER_WIDTH + PADDING) * 2
            }, text.textProperty()
        ))

        children.add(text)
    }

    fun select() {
        isSelected = true
    }

    fun unselect() {
        isSelected = false
    }
}
