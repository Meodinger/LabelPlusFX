package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.util.string.deleteTail
import ink.meodinger.lpfx.util.string.emptyString


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * A data class for property storage
 */
data class CProperty(val key: String, var value: String = emptyString()) {

    // NOTE: May be deprecated in 3.x versions. May use serialize/java.util.properties instead.

    companion object {
        internal const val LIST_SEPARATOR = '|'
        private const val PAIR_SEPARATOR = ','
        private const val PAIR_START     = '<'
        private const val PAIR_STOP      = '>'

        private fun parseList(values: List<*>): String {
            val builder = StringBuilder()
            for (value in values) {
                val content = when (value) {
                    is Pair<*, *> -> "$PAIR_START${value.first}$PAIR_SEPARATOR${value.second}$PAIR_STOP"
                    else -> value.toString()
                }

                builder.append(content).append(LIST_SEPARATOR)
            }
            return builder.deleteTail(LIST_SEPARATOR.toString()).toString()
        }

    }

    // ----- Constructors ----- //

    constructor(key: String, value: Boolean): this(key, value.toString())
    constructor(key: String, value: Number) : this(key, value.toString())
    constructor(key: String, value: List<*>) : this(key, parseList(value))
    constructor(key: String, vararg value: Any) : this(key, listOf(*value))

    val isList: Boolean get() = value.contains(LIST_SEPARATOR)
    val isEmpty: Boolean get() = value.isEmpty()

    fun asString(): String {
        return value
    }
    fun asBoolean(): Boolean {
        return value.toBoolean()
    }
    fun asInteger(radix: Int = 10): Int {
        return value.toInt(radix)
    }
    fun asLong(): Long {
        return value.toLong()
    }
    fun asDouble(): Double {
        return value.toDouble()
    }

    fun asStringList(): List<String> {
        return value.takeIf(String::isNotEmpty)?.split(LIST_SEPARATOR) ?: emptyList()
    }
    fun asBooleanList(): List<Boolean> {
        return asStringList().map(String::toBoolean)
    }
    fun asIntegerList(): List<Int> {
        return asStringList().map(String::toInt)
    }
    fun asLongList(): List<Long> {
        return asStringList().map(String::toLong)
    }
    fun asDoubleList(): List<Double> {
        return asStringList().map(String::toDouble)
    }

    fun asPairList(): List<Pair<String, String>> {
        return asStringList().map {
            val sepIndex = it.indexOf(PAIR_SEPARATOR)

            var spaces = 0
            while (it[sepIndex + spaces + 1] == ' ') spaces++

            Pair(it.substring(1, sepIndex), it.substring(sepIndex + 1 + spaces, it.length - 1))
        }
    }

    fun set(value: String) {
        this.value = value
    }
    fun set(value: Boolean) {
        set(value.toString())
    }
    fun set(value: Number) {
        set(value.toString())
    }
    fun set(list: List<*>) {
        set(parseList(list))
    }
    fun set(vararg list: Any) {
        set(listOf(*list))
    }
    fun set(another: CProperty) {
        set(another.value)
    }

    override fun toString(): String = "CProperty(key=${key}, value=${value})"

}
