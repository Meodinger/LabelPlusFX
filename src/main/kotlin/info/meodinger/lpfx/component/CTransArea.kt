package info.meodinger.lpfx.component

import info.meodinger.lpfx.util.accelerator.isAltDown
import info.meodinger.lpfx.util.property.getValue
import info.meodinger.lpfx.util.property.setValue

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.StringProperty
import javafx.geometry.Side
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TextArea
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.paint.Color
import javafx.scene.shape.Circle


/**
 * Author: Meodinger
 * Date: 2021/8/16
 * Location: info.meodinger.lpfx.component
 */

/**
 * A TextArea with a symbol ContextMenu
 */
class CTransArea: TextArea() {

    private val symbolMenu = object : ContextMenu() {

        private val radius = 6.0
        private val symbols = listOf(
            Pair("※", true),
            Pair("◎", true),
            Pair("★", true),
            Pair("☆", true),
            Pair("～", true),
            Pair("♡", false),
            Pair("♥", false),
            Pair("♢", false),
            Pair("♦", false),
            Pair("♪", false)
        )

        private fun createSymbolItem(symbol: String, displayable: Boolean): MenuItem {
            return MenuItem(symbol, Circle(radius, if (displayable) Color.GREEN else Color.RED)).also {
                it.isMnemonicParsing = false
                it.style = "-fx-font-family: \"Segoe UI Symbol\"; -fx-font-size: 12px"
            }
        }

        init {
            for ((symbol, displayable) in symbols) items.add(createSymbolItem(symbol, displayable).also {
                it.setOnAction {
                    this@CTransArea.insertText(this@CTransArea.caretPosition, symbol)
                }
            })
        }
    }

    private val boundTextPropertyProperty = SimpleObjectProperty<StringProperty>(null)
    private var boundTextProperty: StringProperty? by boundTextPropertyProperty

    val isBound: Boolean get() = textProperty().isBound

    init {
        // Symbol Menu
        addEventHandler(KeyEvent.KEY_PRESSED) {
            if (isAltDown(it) && it.code == KeyCode.A) {
                symbolMenu.show(this, Side.LEFT, 0.0, 0.0)
            }
        }
    }

    fun reset() {
        unbindBidirectional()
    }

    fun bindBidirectional(property: StringProperty) {
        textProperty().bindBidirectional(property)
        boundTextProperty = property
    }

    fun unbindBidirectional() {
        if (boundTextProperty == null)  return

        textProperty().unbindBidirectional(boundTextProperty)
        boundTextProperty = null
        text = ""
    }

}