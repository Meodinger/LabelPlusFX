package info.meodinger.lpfx.util.string

import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get

import javafx.scene.text.Font
import javafx.scene.text.Text
import java.util.*


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.util
 */

/**
 * Delete tail string
 *
 * @param tail Tail to delete
 */
fun StringBuilder.deleteTail(tail: String) {
    if (tail.isEmpty()) return

    val tailLength = tail.length
    val builderLength = this.length

    this.deleteRange(builderLength - tailLength, builderLength)
}

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

/**
 * Trim a list of string
 *
 * @param strings Strings to trim
 * @return A list of String, first two is the same head & tail of input strings, others are strings that trimmed head & tail
 */
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

/**
 * Sort a list of string by their int value
 */
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
            val sorted = trimmed.subList(2, trimmed.size).sortedBy { it.toInt() }

            return List(sorted.size) { trimmed[0] + sorted[it] + trimmed[1] }
        }
    }

    // default
    return strings.toMutableList().sorted()
}

/**
 * Omit high text
 */
fun omitHighText(longText: String, maxRowCount: Int = 10): String {
    val lines = longText.split("\n")

    if (lines.size <= maxRowCount) return longText

    val builder = StringBuilder()
    for (i in 0 until maxRowCount - 1) builder.appendLine(lines[i])
    builder.append(String.format(I18N["util.long_text.format.i"], lines.size - maxRowCount - 1))

    return builder.toString()
}

/**
 * Omit wide text
 */
fun omitWideText(longText: String, maxWidth: Double, font: Font? = null): String {
    val lines = longText.split("\n")
    val builder = StringBuilder()

    val t = Text().also { it.font = font }
    val b = StringBuilder()
    var p: Int
    for (line in lines) {
        t.text = line
        b.clear()
        p = t.text.length - 1
        if (t.boundsInLocal.width > maxWidth) {
            b.append(line).append("...")
            while (t.boundsInLocal.width > maxWidth) {
                b.deleteAt(p--)
                t.text = b.toString()
            }
            builder.appendLine(b.toString())
        } else {
            builder.appendLine(line)
        }
    }
    if (builder.isNotEmpty()) builder.deleteTail("\n")

    return builder.toString()
}