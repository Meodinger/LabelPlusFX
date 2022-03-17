package ink.meodinger.lpfx.util.string


/**
 * Author: Meodinger
 * Date: 2021/12/5
 * Have fun with my code!
 */

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
 * Sort a list of string by their long value
 */
fun sortByDigit(strings: Collection<String>): List<String> {
    val trimmed = trimSame(if (strings is List) strings else strings.toList())

    if (trimmed.size > 2) {
        var canCastToIntList = true
        for (i in 2 until trimmed.size) {
            if (!trimmed[i].isMathematicalNatural()) {
                canCastToIntList = false
                break
            }
        }
        if (canCastToIntList) {
            val sorted = trimmed.subList(2, trimmed.size).sortedBy(String::toLong)

            return List(sorted.size) { trimmed[0] + sorted[it] + trimmed[1] }
        }
    }

    // default
    return strings.sorted()
}
