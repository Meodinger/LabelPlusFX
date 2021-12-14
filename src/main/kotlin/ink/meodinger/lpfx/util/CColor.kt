package ink.meodinger.lpfx.util.color

import javafx.scene.paint.Color


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * Get color RGB in hex
 */
fun Color.toHex(): String {
    return this.toString().substring(2, 8).uppercase()
}

/**
 * Is a string ColorHex
 */
fun String?.isColorHex(): Boolean {
    if (this == null) return false

    val length = this.length
    if (length != 6 && length != 8) return false

    val chars = this.uppercase().toCharArray()
    for (c in chars) if (c !in '0'..'9' && c !in 'A'..'F') return false

    return true
}
