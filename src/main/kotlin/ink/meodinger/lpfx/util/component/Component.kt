package ink.meodinger.lpfx.util.component

import javafx.event.ActionEvent
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.MenuItem
import javafx.scene.control.TextFormatter
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Stage


/**
 * Author: Meodinger
 * Date: 2021/9/30
 * Have fun with my code!
 */

/**
 * Alias of Button.setOnAction
 * @param onAction Handler of ActionEvent, with Button as this and ActionEvent as it
 */
inline infix fun Button.does(crossinline onAction: Button.(ActionEvent) -> Unit): Button {
    return apply { setOnAction { onAction(this, it) } }
}

/**
 * Alias of MenuItem.setOnAction
 * @param onAction Handler of ActionEvent, with MenuItem as this and ActionEvent as it
 */
inline infix fun MenuItem.does(crossinline onAction: MenuItem.(ActionEvent) -> Unit): MenuItem {
    return apply { setOnAction { onAction(this, it) } }
}

/**
 * Close the Stage when Escape-Key pressed. Note that this action
 * could not be supressed because it uses EventFilter for event catch.
 */
fun <T : Stage> T.closeOnEscape() {
    addEventFilter(KeyEvent.KEY_PRESSED) { if (it.code == KeyCode.ESCAPE) close() }
}

/**
 * Clear canvas
 */
fun Canvas.clearGraphicContext() {
    graphicsContext2D.clearRect(0.0, 0.0, width, height)
}

/**
 * Generate a TextFormatter use a simple replacer to process the Change::text
 */
fun <T> genTextFormatter(replacer: (TextFormatter.Change) -> String): TextFormatter<T> {
    return TextFormatter<T> {
        if (it.isAdded) it.apply {
            text = replacer(this)
            anchor = rangeStart + text.length
            caretPosition = rangeStart + text.length
        } else it
    }
}
