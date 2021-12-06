package ink.meodinger.lpfx.util.string

/**
 * Author: Meodinger
 * Date: 2021/12/5
 * Have fun with my code!
 */

/**
 * Delete tail string
 *
 * @param tail Tail to delete
 */
fun StringBuilder.deleteTail(tail: String): StringBuilder {
    if (tail.isEmpty()) return this
    if (this.isEmpty()) return this
    if (!this.endsWith(tail)) return this

    val tailLength = tail.length
    val builderLength = this.length

    return this.deleteRange(builderLength - tailLength, builderLength)
}

/**
 * CharArray to StringBuilder
 * @param start Start index of the array
 * @param end End index of the array
 * @return A builder built by CharArray with start & end index
 */
fun CharArray.toBuilder(start: Int = 0, end: Int = size): StringBuilder {
    if (start < 0) throw IllegalArgumentException("start index less than 0")
    if (end > size) throw IllegalArgumentException("end index out of bounds")

    val builder = StringBuilder()
    for (i in start until end) builder.append(this[i])

    return builder
}