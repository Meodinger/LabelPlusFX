package info.meodinger.lpfx.util.keyboard

import info.meodinger.lpfx.util.platform.isMac

import javafx.scene.input.GestureEvent
import javafx.scene.input.KeyEvent

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.util
 */
fun isControlDown(event: GestureEvent): Boolean {
    if (event.isControlDown) return true
    return if (isMac) event.isMetaDown else false
}

fun isControlDown(event: KeyEvent): Boolean {
    if (event.isControlDown) return true
    return if (isMac) event.isMetaDown else false
}