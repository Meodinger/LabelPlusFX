package ink.meodinger.lpfx.util.component

import javafx.event.ActionEvent
import javafx.scene.control.Button
import javafx.scene.control.MenuItem
import javafx.scene.control.TextFormatter
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
infix fun Button.does(onAction: Button.(ActionEvent) -> Unit): Button {
    return apply { setOnAction { onAction(this, it) } }
}

infix fun MenuItem.does(onAction: MenuItem.(ActionEvent) -> Unit): MenuItem {
    return apply { setOnAction { onAction(this, it) } }
}

fun <T : Stage> T.closeOnEscape() {
    addEventFilter(KeyEvent.KEY_PRESSED) { close() }
}

fun <T> genTextFormatter(replacer: (TextFormatter.Change) -> String): TextFormatter<T> {
    return TextFormatter<T> {
        if (it.isAdded) it.apply {
            text = replacer(this)
            anchor = rangeStart + text.length
            caretPosition = rangeStart + text.length
        } else it
    }
}
