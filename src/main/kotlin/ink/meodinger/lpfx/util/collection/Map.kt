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

fun <K ,V, R> Map<K, V>.map(transformer: (K, V) -> R): List<R> {
    return map { entry -> transformer(entry.key, entry.value) }
}

fun <K, V, R, C : MutableList<R>> Map<K, V>.mapTo(destination: C, transformer: (K, V) -> R): C {
    return mapTo(destination) { entry -> transformer(entry.key, entry.value) }
}
