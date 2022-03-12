package ink.meodinger.lpfx.util.collection

/**
 * Author: Meodinger
 * Date: 2022/1/17
 * Have fun with my code!
 */

/**
 * Contains If
 */
fun <E> Collection<E>.contains(predicate: (E) -> Boolean): Boolean {
    for (element in this) if (predicate(element)) return true
    return false
}
