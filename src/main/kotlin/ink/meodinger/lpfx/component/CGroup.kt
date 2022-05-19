package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.util.component.add
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
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Skin
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
) : Control() {

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

    // region Skin

    /**
     * Create default Skin
     * @see javafx.scene.control.Control.createDefaultSkin
     */
    override fun createDefaultSkin(): Skin<CGroup> = CGroupSkin(this)

    private class CGroupSkin(private val control: CGroup) : Skin<CGroup> {

        private val root = Pane()
        private val text = Text()

        init {
            root.apply root@{
                padding = Insets(PADDING)

                // Trigger action when clicked/touched/Enter-ed
                val actionListener = EventHandler<Event> {
                    control.isSelected = true
                    requestFocus()
                    control.fire(it)
                }
                addEventHandler(MouseEvent.MOUSE_CLICKED) {
                    if (it.clickCount != 1) return@addEventHandler
                    actionListener.handle(it)
                }
                addEventHandler(TouchEvent.TOUCH_RELEASED) {
                    if (it.touchCount != 1) return@addEventHandler
                    actionListener.handle(it)
                }
                addEventHandler(KeyEvent.KEY_RELEASED) {
                    if (it.code != KeyCode.ENTER) return@addEventHandler
                    actionListener.handle(it)
                }

                borderProperty().bind(Bindings.createObjectBinding(
                    {
                        Border(BorderStroke(
                            if (isHover) control.color else Color.TRANSPARENT,
                            BorderStrokeStyle.SOLID,
                            CornerRadii(CORNER_RADII),
                            BorderWidths(BORDER_WIDTH)
                        ))
                    }, hoverProperty()
                ))
                backgroundProperty().bind(Bindings.createObjectBinding(
                    {
                        if (control.isSelected) Background(BackgroundFill(
                            Color.LIGHTGRAY,
                            CornerRadii(CORNER_RADII),
                            Insets(0.0)
                        )) else null
                    }, control.selectedProperty
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

                add(text) {
                    textOrigin = VPos.TOP
                    textProperty().bind(control.nameProperty)
                    fillProperty().bind(control.colorProperty)

                    // FIXME: Text will move a little upper if Group-Name changes

                    layoutXProperty().bind(control.widthProperty().transform { (it - boundsInLocal.width) / 2 })
                    layoutYProperty().bind(control.heightProperty().transform { (it - boundsInLocal.height) / 2 })
                }
            }
        }

        override fun getSkinnable(): CGroup = control

        override fun getNode(): Node = root

        override fun dispose() {
            text.apply {
                textProperty().unbind()
                fillProperty().unbind()
                layoutXProperty().unbind()
                layoutYProperty().unbind()
                root.children.remove(this)
            }
            root.apply {
                borderProperty().unbind()
                backgroundProperty().unbind()
                prefWidthProperty().unbind()
                prefHeightProperty().unbind()
            }
        }

    }

    // endregion

}
