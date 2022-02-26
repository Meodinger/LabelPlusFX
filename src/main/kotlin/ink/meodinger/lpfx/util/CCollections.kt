package ink.meodinger.lpfx.util

/**
 * Author: Meodinger
 * Date: 2022/1/17
 * Have fun with my code!
 */

/**
 * Add at last
 */
fun <E> MutableList<E>.addLast(element: E, indexFromLast: Int = 0) {
    add(size - indexFromLast, element)
}

/**
 * Add at first
 */
fun <E> MutableList<E>.addFirst(element: E, indexFromFirst: Int = 0) {
    add(indexFromFirst, element)
}

/**
 * Remove all for Map
 */
fun <K, V> MutableMap<K, V>.removeAll(keys: Collection<K>) {
    keys.forEach(::remove)
}

/**
 * Contains If
 */
fun <E> Collection<E>.contains(predicate: (E) -> Boolean): Boolean {
    for (element in this) if (predicate(element)) return true
    return false
}
