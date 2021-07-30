package info.meodinger.lpfx.util.string

import info.meodinger.lpfx.util.char.isWhiteSpace
import java.util.*


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.util
 */

fun String.isBlank(): Boolean {
    if (this.isEmpty()) return true

    val chars = this.toCharArray()
    var whiteCount = 0
    for (c in chars) {
        if (c.isWhiteSpace()) whiteCount++
    }
    return whiteCount == chars.size
}

fun String.isDigit(): Boolean {
    val chars = this.toCharArray()
    for (c in chars) {
        if (c !in '0'..'9') return false
    }
    return true
}

fun trimSame(strings: List<String>): List<String> {
    if (strings.isEmpty()) return listOf("", "")
    if (strings.size == 1) return listOf("", "", strings[0])

    val trimmed = ArrayList<String>()

    val example = strings[0].toCharArray()
    var head = example.size
    var tail = example.size
    for (i in 1 until strings.size) {
        val chars = strings[i].toCharArray()
        val range = Math.min(example.size, chars.size)

        for (j in 0 until range) {
            if (chars[j] != example[j]) {
                head = Math.min(j, head)
                break
            }
        }
        for (j in 0 until range) {
            if (chars[chars.size - j - 1] != example[example.size - j - 1]) {
                tail = Math.min(j, tail)
                break
            }
        }
    }

    trimmed.add(strings[0].substring(0, head))
    trimmed.add(strings[0].substring(strings[0].length - tail))
    for (string in strings) trimmed.add(string.substring(head, string.length - tail))

    return trimmed
}