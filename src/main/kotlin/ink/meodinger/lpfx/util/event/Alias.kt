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
 * Alias isMetaDown
 */
fun isControlDown(event: GestureEvent): Boolean {
    if (event.isControlDown) return true
    if (isMac && event.isMetaDown) return true

    return false
}

/**
 * Alias isMetaDown
 */
fun isControlDown(event: KeyEvent): Boolean {
    if (event.isControlDown) return true
    if (isMac && event.isMetaDown) return true

    return false
}

/**
 * Alias isMetaDown
 */
fun isAltDown(event: GestureEvent): Boolean {
    if (event.isAltDown) return true
    if (isMac && event.isMetaDown) return true

    return false
}

/**
 * Alias isMetaDown
 */
fun isAltDown(event: KeyEvent): Boolean {
    if (event.isAltDown) return true
    if (isMac && event.isMetaDown) return true

    return false
}
