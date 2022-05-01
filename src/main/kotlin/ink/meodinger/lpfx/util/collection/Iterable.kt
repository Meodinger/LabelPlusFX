package ink.meodinger.lpfx.util.collection

/**
 * Author: Meodinger
 * Date: 2022/1/17
 * Have fun with my code!
 */

/**
 * Contact some Iterables
 * @param iterables All Iterables to be contacted
 * @return An Iterable that contains all iterables in gived order
 */
fun <E> contact(vararg iterables: Iterable<E>): Iterable<E> {
    return iterables.reduce { acc, iterable -> acc + iterable }
}
