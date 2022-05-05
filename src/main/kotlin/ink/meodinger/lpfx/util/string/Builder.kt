package ink.meodinger.lpfx.util.string


/**
 * Author: Meodinger
 * Date: 2021/12/5
 * Have fun with my code!
 */

/**
 * Delete tail string
 * @param trailing Tail to delete
 */
fun StringBuilder.deleteTrailing(trailing: String): StringBuilder {
    if (trailing.isEmpty()) return this
    if (this.isEmpty()) return this
    if (!this.endsWith(trailing)) return this

    return this.deleteRange(length - trailing.length, length)
}

/**
 * Delete tail line feed
 * @param eol line feed tail
 */
fun StringBuilder.deleteTrailingEOL(eol: String = "\n"): StringBuilder = deleteTrailing(eol)
