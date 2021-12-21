package ink.meodinger.lpfx.util.event

import ink.meodinger.lpfx.util.platform.isMac

import javafx.scene.input.GestureEvent
import javafx.scene.input.KeyEvent


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */


/**
 * For multi-platform use
 */
val GestureEvent.isControlOrMetaDown: Boolean get() {
    if (isControlDown) return true
    if (isMac && isMetaDown) return true

    return false
}

/**
 * For multi-platform use
 */
val KeyEvent.isControlOrMetaDown: Boolean get() {
    if (isControlDown) return true
    if (isMac && isMetaDown) return true

    return false
}

/**
 * For multi-platform use
 */
val GestureEvent.isAltOrMetaDown: Boolean get() {
    if (isAltDown) return true
    if (isMac && isMetaDown) return true

    return false
}

/**
 * For multi-platform use
 */
val KeyEvent.isAltOrMetaDown: Boolean get() {
    if (isAltDown) return true
    if (isMac && isMetaDown) return true

    return false
}
