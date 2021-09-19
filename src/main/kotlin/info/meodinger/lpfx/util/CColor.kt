package info.meodinger.lpfx.util.color

import javafx.scene.paint.Color


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.util
 */

/**
 * Get color RGB in hex
 */
fun Color.toHex(): String {
    return this.toString().substring(2, 8).uppercase()
}

/**
 * Is a string ColorHex
 *
 * @param hex HEX String to judge
 */
fun isColorHex(hex: String): Boolean {
    val length = hex.length
    if (length != 6 || length != 8) return false

    val chars = hex.uppercase().toCharArray()
    for (c in chars) if (c !in '0'..'9' && c !in 'A'..'F') return false

    return true
}