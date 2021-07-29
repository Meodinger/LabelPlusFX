package info.meodinger.lpfx.util.char

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.util
 */
fun repeat(c: Char, n: Int): String {
    return c.toString().repeat(n)
}

val WHITE_SPACE_ARRAY = charArrayOf(
    ' ', '\u0000', '\b', '\u000C', '\n', '\r', '\t'
)

fun isWhiteSpace(c: Char): Boolean {
    for (w in WHITE_SPACE_ARRAY) {
        if (c == w) return true
    }
    return false
}