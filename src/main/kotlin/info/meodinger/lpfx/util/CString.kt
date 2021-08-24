package info.meodinger.lpfx.util.string

import java.util.*

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.util
 */

/**
 * Is a string only contains 0-9
 */
fun String.isInt(): Boolean {
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
        val range = example.size.coerceAtMost(chars.size)

        for (j in 0 until range) {
            if (chars[j] != example[j]) {
                head = head.coerceAtMost(j)
                break
            }
        }
        for (j in 0 until range) {
            if (chars[chars.size - j - 1] != example[example.size - j - 1]) {
                tail = tail.coerceAtMost(j)
                break
            }
        }
    }

    trimmed.add(strings[0].substring(0, head))
    trimmed.add(strings[0].substring(strings[0].length - tail))
    for (string in strings) trimmed.add(string.substring(head, string.length - tail))

    return trimmed
}

fun sortByDigit(strings: List<String>): List<String> {
    val trimmed = trimSame(strings)

    if (trimmed.size > 2) {
        var canCastToIntList = true
        for (i in 2 until trimmed.size) {
            if (!trimmed[i].isInt()) {
                canCastToIntList = false
                break
            }
        }
        if (canCastToIntList) {
            val integerList = MutableList(trimmed.size - 2) { trimmed[it + 2].toInt() }.also { it.sort() }

            return List(integerList.size) { trimmed[0] + integerList[it] + trimmed[1] }
        }
    }

    // default
    return strings.toMutableList().sorted()
}