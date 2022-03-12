package ink.meodinger.lpfx.util.collection

/**
 * Author: Meodinger
 * Date: 2022/3/11
 * Have fun with my code!
 */

/**
 * Remove all for Map
 */
fun <K, V> MutableMap<K, V>.removeAll(keys: Collection<K>) {
    keys.forEach(::remove)
}
