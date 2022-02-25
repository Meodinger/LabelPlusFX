package ink.meodinger.lpfx.component.common

import ink.meodinger.lpfx.util.color.toHexRGB
import ink.meodinger.lpfx.util.event.actionEvent
import ink.meodinger.lpfx.util.property.onNew
import ink.meodinger.lpfx.util.string.repeat

import javafx.beans.property.StringProperty
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
 * @see <a href="https://stackoverflow.com/questions/42812471/javafx-colorpicker-nullpointerexception">colorpicker-nullpointerexception</a>
 */
class CColorPicker() : ColorPicker() {

    private val colorHexField: TextField = TextField()
    private val colorHexProperty: StringProperty = colorHexField.textProperty()
    // fun colorHexProperty(): StringProperty = colorHexProperty
    // var colorHex: String by colorHexProperty

    constructor(color: Color): this() { value = color }

    init {
        colorHexField.textFormatter = TextFormatter<String> { change ->
            change.text = change.text.uppercase()
            if (change.isAdded) {
                val builder = StringBuilder()
                for (c in change.text.toCharArray()) {
                    if ((c in '0'..'9') || (c in 'A'..'F')) {
                        builder.append(c)
                    }
                }
                change.text = builder.toString()
            }
            change
        }
        colorHexField.setOnAction {
            if (!customColors.contains(value)) customColors.add(value)

            fireEvent(actionEvent(it, source = this))
            hide() // Manually invoke hide() to let PopupControl really hide
        }

        colorHexProperty.addListener(onNew {
            when (it.length) {
                1 -> value = Color.web(it.repeat(6))
                2 -> value = Color.web(it.repeat(3))
                3 -> {
                    val builder = StringBuilder()
                    for (c in it.toCharArray()) {
                        builder.append(c.repeat(2))
                    }
                    value = Color.web(builder.toString())
                }
                6 -> value = Color.web(it)
            }
        })

        addEventHandler(ON_SHOWN) { colorHexProperty.set(value.toHexRGB()) }
        addEventHandler(ON_HIDDEN) { colorHexProperty.set(value.toHexRGB()) }
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
