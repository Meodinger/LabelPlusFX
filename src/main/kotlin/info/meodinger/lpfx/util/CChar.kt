package info.meodinger.lpfx.util.char

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.util
 */
fun Char.repeat(n: Int): String {
    return this.toString().repeat(n)
}

val Char.Companion.WHITE_SPACE_ARRAY : CharArray
    get() = charArrayOf(
        ' ', '\u0000', '\b', '\u000C', '\n', '\r', '\t'
    )

fun Char.isWhiteSpace(): Boolean {
    for (w in Char.WHITE_SPACE_ARRAY) {
        if (this == w) return true
    }
    return false
}