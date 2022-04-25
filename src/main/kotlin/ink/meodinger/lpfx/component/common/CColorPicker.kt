package ink.meodinger.lpfx.component.common

import ink.meodinger.lpfx.util.color.toHexRGB
import ink.meodinger.lpfx.util.color.toHexRGBA
import ink.meodinger.lpfx.util.property.onNew
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.string.repeat

import javafx.beans.property.*
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.skin.ColorPickerSkin
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.paint.Color


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * JavaFX ColorPicker will throw an Exception when be applied to
 * a MenuItem and click the 'custom color' link. This ColorPicker
 * replaces the link with a ColorHex TextField which can be used
 * to enter a hex string.
 *
 * @see <a href="https://stackoverflow.com/questions/42812471/javafx-colorpicker-nullpointerexception">ColorPicker-NullPointerException</a>
 */
class CColorPicker() : ColorPicker() {

    // DO NOT EXPORT this property, use valueProperty() instead.
    private val colorHexField: TextField = TextField()
    private val colorHexProperty: StringProperty = colorHexField.textProperty()

    private val enableAlphaProperty: BooleanProperty = SimpleBooleanProperty(false)
    fun enableAlphaProperty(): BooleanProperty = enableAlphaProperty
    var enableAlpha: Boolean by enableAlphaProperty

    constructor(color: Color): this() { value = color }

    init {
        colorHexField.textFormatter = TextFormatter<String> { change ->
            if (change.isAdded) {
                val text = change.text.uppercase().filter { (it in '0'..'9') || (it in 'A'..'F') }

                change.text = text
                change.anchor = change.rangeStart + text.length
                change.caretPosition = change.rangeStart + text.length
            }
            change
        }
        colorHexField.setOnAction {
            // Add to recent colors
            if (!customColors.contains(value)) customColors.add(value)

            fireEvent(ActionEvent(it.source, this))
            hide() // Manually invoke hide() to let PopupControl logically hide
        }

        colorHexProperty.addListener(onNew {
            value = when (it.length) {
                1 -> Color.web(it.repeat(6))
                2 -> Color.web(it.repeat(3))
                3 -> Color.web(it.map { c -> c.repeat(2) }.joinToString())
                6 -> Color.web(it)
                8 -> Color.web(if (enableAlpha) it else it.dropLast(2))
                else -> value
            }
        })

        val handler = EventHandler<Event> {
            colorHexProperty.set(if (enableAlpha) value.toHexRGBA() else value.toHexRGB())
        }
        addEventHandler(ON_SHOWN, handler)
        addEventHandler(ON_HIDDEN, handler)
    }

    override fun createDefaultSkin(): Skin<*> {
        return object : ColorPickerSkin(this) {
            override fun getPopupContent(): Node {
                // This is an instance of private API ColorPalette which extends Region
                val colorPalette: Region = super.getPopupContent() as Region

                // This ColorPalette contains a VBox which contains the Hyperlink we want to remove.
                val vbox = colorPalette.childrenUnmodifiable.find { it is VBox } as VBox
                val hyperlink = vbox.children.find { it is Hyperlink } as Hyperlink?
                if (hyperlink != null) {
                    vbox.children.removeAll(hyperlink) // Remove the hyperlink
                    vbox.alignment = Pos.CENTER
                    vbox.children.add(colorHexField)
                }
                return colorPalette
            }
        }
    }
}
