package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.property.transform
import ink.meodinger.lpfx.util.string.emptyString

import javafx.beans.binding.Bindings
import javafx.beans.property.*
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.VPos
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.TouchEvent
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

    // region Properties

    private val nameProperty: StringProperty = SimpleStringProperty(groupName)
    /**
     * The group name to display
     */
    fun nameProperty(): StringProperty = nameProperty
    /**
     * @see nameProperty
     */
    var name: String by nameProperty

    private val colorProperty: ObjectProperty<Color> = SimpleObjectProperty(groupColor)
    /**
     * The group color to display
     */
    fun colorProperty(): ObjectProperty<Color> = colorProperty
    /**
     * @see colorProperty
     */
    var color: Color by colorProperty

    private val selectedProperty: BooleanProperty = SimpleBooleanProperty(false)
    /**
     * Whether this is selected
     */
    fun selectedProperty(): BooleanProperty = selectedProperty
    /**
     * @see selectedProperty
     */
    var isSelected: Boolean by selectedProperty

    private val onActionProperty: ObjectProperty<EventHandler<ActionEvent>> = SimpleObjectProperty(EventHandler {})
    /**
     * The CGroup's action, which is invoked whenever the CGroup is fired.
     * This may be due to the user clicking on the button with the mouse,
     * or by a touch event, or by a key press, or if the developer
     * programmatically invokes the `fire()` method.
     *
     * @return the property to represent the button's action, which is invoked
     * whenever the button is fired
     */
    fun onActionProperty(): ObjectProperty<EventHandler<ActionEvent>> = onActionProperty
    /**
     * @see onActionProperty
     */
    val onAction: EventHandler<ActionEvent> by onActionProperty
    /**
     * @see onActionProperty
     */
    fun setOnAction(handler: EventHandler<ActionEvent>) = onActionProperty.set(handler)

    // endregion

    init {
        padding = Insets(PADDING)

        // Trigger action when clicked/touched/Enter-ed
        addEventHandler(MouseEvent.MOUSE_CLICKED) {
            isSelected = true
            requestFocus()
            fire(it)
        }
        addEventHandler(TouchEvent.TOUCH_RELEASED) {
            isSelected = true
            requestFocus()
            fire(it)
        }
        addEventHandler(KeyEvent.KEY_RELEASED) {
            if (it.code != KeyCode.ENTER) return@addEventHandler
            isSelected = true
            requestFocus()
            fire(it)
        }

        val text = Text().apply {
            textOrigin = VPos.TOP
            textProperty().bind(nameProperty)
            fillProperty().bind(colorProperty)
            layoutXProperty().bind(widthProperty().transform { (it - boundsInLocal.width) / 2 })
            layoutYProperty().bind(heightProperty().transform { (it - boundsInLocal.height) / 2 })
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

    /**
     * isSelected = true
     */
    fun select() {
        isSelected = true
    }
    /**
     * isSelected = false
     */
    fun unselect() {
        isSelected = false
    }

    /**
     * Invoked when a user gesture indicates that an event for this should occur
     */
    fun fire(sourceEvent: Event? = null) {
        if (sourceEvent != null) {
            onAction.handle(ActionEvent(sourceEvent.source, this))
        } else {
            onAction.handle(ActionEvent())
        }
    }

}
