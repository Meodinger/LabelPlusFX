package info.meodinger.lpfx.component

import info.meodinger.lpfx.util.accelerator.isAltDown
import javafx.beans.property.SimpleBooleanProperty

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

        fun createSymbolItem(symbol: String, displayable: Boolean): MenuItem {
            return MenuItem(
                symbol,
                if (displayable) Circle(radius, Color.GREEN)
                else Circle(radius, Color.RED)
            ).also {
                it.style = "-fx-font-family: \"Segoe UI Symbol\""
            }
        }

        init {
            for (symbol in symbols) items.add(createSymbolItem(symbol.first, symbol.second).also {
                it.setOnAction { this@CTransArea.insertText(this@CTransArea.caretPosition, symbol.first) }
            })
        }
    }

    val isBoundProperty = SimpleBooleanProperty(false)
    var isBound: Boolean
        get() = isBoundProperty.value
        set(value) {
            isBoundProperty.value = value
        }

    init {
        // Symbol Menu
        addEventHandler(KeyEvent.KEY_PRESSED) {
            if (isAltDown(it) && it.code == KeyCode.A) {
                symbolMenu.show(this, Side.LEFT, 0.0, 0.0)
            }
        }
    }

}