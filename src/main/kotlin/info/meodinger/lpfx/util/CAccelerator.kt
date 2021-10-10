package info.meodinger.lpfx.util.accelerator

import info.meodinger.lpfx.util.platform.isMac

import javafx.scene.input.GestureEvent
import javafx.scene.input.KeyEvent


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.util
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