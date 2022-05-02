package ink.meodinger.lpfx.util.string


/**
 * Author: Meodinger
 * Date: 2021/12/5
 * Have fun with my code!
 */

/**
 * Is the String a mathematical natural number
 */
fun String.isMathematicalNatural(): Boolean {
    val iterator = this.toCharArray().iterator()
    while (iterator.hasNext())
        if (iterator.nextChar() !in '0'..'9')
            return false

    return true
}

/**
 * Is the String a mathematical integer
 */
fun String.isMathematicalInteger(): Boolean {
    val chars = this.toCharArray()
    val isNegative = chars[0] == '-'

    return if (isNegative) substring(1).isMathematicalNatural() else isMathematicalNatural()
}

/**
 * Is the String a mathematical decimal
 */
fun String.isMathematicalDecimal(): Boolean {
    val chars = this.toCharArray()

    var index = 0
    val isNegative = chars[index] == '-'
    if (isNegative) index++
    val isSimplifiedDecimal = chars[index] == '.'
    if (isSimplifiedDecimal) index++

    val iterator = chars.iterator()
    while (--index >= 0) iterator.nextChar()

    var dot = isSimplifiedDecimal
    while (iterator.hasNext()) {
        val char = iterator.nextChar()
        if (char == '.') {
            if (dot) return false
            dot = true
            continue
        }
        if (char !in '0'..'9') {
            return false
        }
    }

    return true
}

/**
 * Return a String that is the number in fixed style
 * Alias for String.format("%.[count]f", this.toDouble())
 * @param count Fix
 */
fun Number.fixed(count: Int): String = String.format("%.${count}f", toDouble())

/**
 * Alias for String.format("%0[length]", this)
 * @param length Length
 */
fun Int.pad(length: Int): String = String.format("%0${length}d", this)
