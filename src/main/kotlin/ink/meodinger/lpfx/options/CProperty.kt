package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.util.string.deleteTail


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * A data class for property storage
 */
class CProperty(val key: String, var value: String = EMPTY) {

    // NOTE: May be deprecated in 3.x versions. May use serialize/java.util.properties instead.

    companion object {
        private const val LIST_SEPARATOR = "|"
        private const val PAIR_SEPARATOR = ","
        private const val PAIR_START     = "<"
        private const val PAIR_STOP      = ">"

        const val EMPTY = ""

        private fun parseList(values: List<*>): String {
            val builder = StringBuilder()
            for (value in values) {
                val content = if (value is Pair<*,*>) {
                    PAIR_START + value.first + PAIR_SEPARATOR + value.second + PAIR_STOP
                } else value.toString()

                builder.append(content).append(LIST_SEPARATOR)
            }
            return builder.deleteTail(LIST_SEPARATOR).toString()
        }

    }

    // ----- Constructors ----- //

    constructor(key: String, value: Boolean): this(key, value.toString())
    constructor(key: String, value: Number) : this(key, value.toString())
    constructor(key: String, value: List<*>) : this(key, parseList(value))
    constructor(key: String, vararg value: Any) : this(key, listOf(*value))

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
        if (value.isBlank()) return emptyList()
        return value.split(LIST_SEPARATOR)
    }
    fun asBooleanList(): List<Boolean> {
        if (value.isBlank()) return emptyList()
        val rawList = value.split(LIST_SEPARATOR)
        return List(rawList.size) { rawList[it].toBoolean() }
    }
    fun asIntegerList(): List<Int> {
        if (value.isBlank()) return emptyList()
        val rawList = value.split(LIST_SEPARATOR)
        return List(rawList.size) { rawList[it].toInt() }
    }
    fun asLongList(): List<Long> {
        if (value.isBlank()) return emptyList()
        val rawList = value.split(LIST_SEPARATOR)
        return List(rawList.size) { rawList[it].toLong() }
    }
    fun asDoubleList(): List<Double> {
        if (value.isBlank()) return emptyList()
        val rawList = value.split(LIST_SEPARATOR)
        return List(rawList.size) { rawList[it].toDouble() }
    }

    fun asPairList(): List<Pair<String, String>> {
        val list = asStringList()

        return List(list.size) {
            val pair = list[it]
            val sepIndex = pair.indexOf(PAIR_SEPARATOR)

            var spaces = 0
            while (pair[sepIndex + spaces + 1] == ' ') spaces++

            val first = pair.substring(1, sepIndex)
            val second = pair.substring(sepIndex + spaces + 1, pair.length - 1)
            Pair(first, second)
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

    override fun toString() = "CProperty(key=${key}, value=${value})"

}
