package ink.meodinger.lpfx.util.string


/**
 * Author: Meodinger
 * Date: 2021/12/5
 * Have fun with my code!
 */


/**
 * Return an empty String
 * @return ""
 */
fun emptyString(): String = ""

/**
 * Repeat char
 * @param n times to repeat
 */
fun Char.repeat(n: Int): String = toString().repeat(n)

/**
 * Remove end-of-line in the String
 */
fun String.replaceEOL(replacement: String = "\\n"): String = replace("\n", replacement)
