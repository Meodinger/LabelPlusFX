package ink.meodinger.lpfx.util.collection

/**
 * Author: Meodinger
 * Date: 2022/1/17
 * Have fun with my code!
 */

// nothing
fun <E> contact(vararg iterables: Iterable<E>): Iterable<E> {
    return iterables.reduce { acc, iterable -> acc.plus(iterable) }
}
