package ink.meodinger.lpfx.util.color

import javafx.scene.paint.Color


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */


/**
 * Apply an opacity to a Color
 */
fun Color.opacity(opacity: Double): Color = Color(red, green, blue, opacity)

/**
 * Get color RGBA in hex
 * @return String format in RRGGBBAA
 */
fun Color.toHexRGBA(): String = toString().uppercase().substring(2, 10)

/**
 * Get color RGB in hex
 * @return String format in RRGGBB
 */
fun Color.toHexRGB(): String = toString().uppercase().substring(2, 8)

/**
 * Whether a string is valid ColorHex, or in the other words,
 * format in `RRGGBB` or `RRGGBBAA`.
 */
fun String?.isColorHex(): Boolean {
    if (this == null) return false
    if (length != 6 && length != 8) return false
    for (c in uppercase().toCharArray()) if (c !in '0'..'9' && c !in 'A'..'F') return false

    return true
}
