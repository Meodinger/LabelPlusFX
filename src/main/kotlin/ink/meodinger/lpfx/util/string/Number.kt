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
    val isNegative = chars[0] == '-'

    val iterator = chars.iterator()
    if (isNegative) iterator.nextChar()

    var dot = false
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