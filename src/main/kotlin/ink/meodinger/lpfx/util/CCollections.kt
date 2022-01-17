package ink.meodinger.lpfx.util

/**
 * Author: Meodinger
 * Date: 2022/1/17
 * Have fun with my code!
 */

/**
 * Add at last
 */
fun <E> MutableList<E>.addAtLast(element: E, indexFromLast: Int = 0) {
    this.add(this.size - indexFromLast, element)
}

/**
 * Add at first
 */
fun <E> MutableList<E>.addAtFirst(element: E, indexFromFirst: Int = 0) {
    this.add(indexFromFirst, element)
}
