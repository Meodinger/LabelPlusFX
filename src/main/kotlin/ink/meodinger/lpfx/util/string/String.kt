package ink.meodinger.lpfx.util.string


/**
 * Author: Meodinger
 * Date: 2021/12/5
 * Have fun with my code!
 */

/**
 * Repeat char
 * @param n times to repeat
 */
fun Char.repeat(n: Int): String = toString().repeat(n)

/**
 * Remove line feeds in the String
 */
fun String.replaceLineFeed(replacement: String = "\\n"): String = replace("\n", replacement)

/**
 * Return an empty String
 * @return String("")
 */
fun emptyString(): String = ""
