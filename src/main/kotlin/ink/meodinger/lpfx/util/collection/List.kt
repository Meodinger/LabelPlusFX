package ink.meodinger.lpfx.util.collection

/**
 * Author: Meodinger
 * Date: 2022/3/11
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
