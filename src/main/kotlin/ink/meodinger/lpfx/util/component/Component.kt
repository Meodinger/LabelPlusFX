package ink.meodinger.lpfx.util.component

import javafx.event.ActionEvent
import javafx.scene.control.Button
import javafx.scene.control.MenuItem


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
