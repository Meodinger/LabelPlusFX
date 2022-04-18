package ink.meodinger.lpfx.util.event

import ink.meodinger.lpfx.isMac
import javafx.scene.input.GestureEvent
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */


/**
 * For multi-platform use
 * Some macOS users will use Command (Control) as their Shortcut key.
 */
val GestureEvent.isControlOrMetaDown: Boolean get() = (isControlDown || (isMac && isMetaDown))

/**
 * For multi-platform use
 * Some macOS users will use Command (Control) as their Shortcut key.
 */
val KeyEvent.isControlOrMetaDown: Boolean get() = (isControlDown || (isMac && isMetaDown))

/**
 * If click count equals 2
 */
val MouseEvent.isDoubleClick: Boolean get() = clickCount == 2
