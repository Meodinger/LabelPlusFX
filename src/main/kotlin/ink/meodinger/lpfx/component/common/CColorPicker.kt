package ink.meodinger.lpfx.component.common

import ink.meodinger.lpfx.util.color.toHex
import ink.meodinger.lpfx.util.string.repeat

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.skin.ColorPickerSkin
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import java.util.stream.Collectors


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: ink.meodinger.lpfx.component
 */

/**
 * A ColorPicker costumed to can be applied to ContextMenu
 * Replace color link with hex TextField
 */
class CColorPicker() : ColorPicker() {

    private val colorHexField = TextField()
    private val colorHexProperty = colorHexField.textProperty()

    constructor(color: Color): this() { this.value = color }

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

            hide()
        }

        colorHexProperty.addListener { _, _, newValue ->
            when (newValue.length) {
                1 -> value = Color.web(newValue.repeat(6))
                2 -> value = Color.web(newValue.repeat(3))
                3 -> {
                    val builder = StringBuilder()
                    for (c in newValue.toCharArray()) {
                        builder.append(c.repeat(2))
                    }
                    value = Color.web(builder.toString())
                }
                6 -> value = Color.web(newValue)
            }
        }

        addEventHandler(ON_SHOWN) { colorHexProperty.set(value.toHex()) }
        addEventHandler(ON_HIDDEN) { colorHexProperty.set(value.toHex()) }
    }

    override fun createDefaultSkin(): Skin<*> {
        return object : ColorPickerSkin(this) {
            override fun getPopupContent(): Node {
                // This is an instance of private API ColorPalette which extends Region
                val colorPalette: Region = super.getPopupContent() as Region

                // This ColorPalette contains a VBox which contains the Hyperlink we want to remove.
                val nVBoxes: List<Node> = colorPalette.childrenUnmodifiable
                    .stream()
                    .filter { e -> e is VBox }
                    .collect(Collectors.toList())
                for (node in nVBoxes) {
                    val vbox: VBox = node as VBox
                    val hyperlinks: List<Node> = vbox.children
                        .stream()
                        .filter { e -> e is Hyperlink }
                        .collect(Collectors.toList())
                    if (hyperlinks.isNotEmpty()) {
                        vbox.children.removeAll(hyperlinks) // Remove the hyperlink
                        vbox.alignment = Pos.CENTER
                        vbox.children.add(colorHexField)
                    }
                }
                return colorPalette
            }
        }
    }
}