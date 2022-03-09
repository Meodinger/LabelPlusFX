package ink.meodinger.lpfx.util.color

import javafx.scene.paint.Color


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * Get color RGBA in hex
 */
fun Color.toHexRGBA(): String = toString().uppercase()

/**
 * Get color RGB in hex
 */
fun Color.toHexRGB(): String = toHexRGBA().substring(2, 8)

/**
 * Is a string ColorHex
 */
fun String?.isColorHex(): Boolean {
    if (this == null) return false

    if (length != 6 && length != 8) return false

    val chars = this.uppercase().toCharArray()
    for (c in chars) if (c !in '0'..'9' && c !in 'A'..'F') return false

    return true
}

fun Color.opacity(opacity: Double): Color = Color(red, green, blue, opacity)
