package info.meodinger.lpfx.util.keyboard

import javafx.scene.input.GestureEvent
import javafx.scene.input.KeyEvent
import java.util.*

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.util
 */
private val OS: String = System.getProperty("os.name").lowercase(Locale.getDefault())

val isMac = OS.contains("mac")

fun isControlDown(event: GestureEvent): Boolean {
    if (event.isControlDown) return true
    return if (isMac) event.isMetaDown else false
}

fun isControlDown(event: KeyEvent): Boolean {
    if (event.isControlDown) return true
    return if (isMac) event.isMetaDown else false
}