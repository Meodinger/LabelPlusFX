package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.string.deleteTail

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: ink.meodinger.lpfx.type
 */

/**
 * A data class for property storage
 */
class CProperty(val key: String, var value: String = UNINITIALIZED) {

    companion object {
        private const val LIST_SEPARATOR = "|"
        private const val PAIR_SEPARATOR = ","

        const val UNINITIALIZED = "<@Uninitialized@>"
        const val EMPTY = ""

        fun parseList(values: List<*>): String {
            val builder = StringBuilder()
            for (value in values) builder.append(value).append(LIST_SEPARATOR)
            return builder.deleteTail(LIST_SEPARATOR).toString()
        }

    }

    // ----- Exception ----- //

    class CPropertyException(message: String) : RuntimeException(message) {
        companion object {
            fun propertyNotFound(key: String) =
                CPropertyException(String.format(I18N["exception.property.property_not_found.format.k"], key))
            fun propertyValueInvalid(key: String, value: Any) =
                CPropertyException(String.format(I18N["exception.property.property_value_invalid.format.vk"], value.toString(), key))
            fun propertyElementInvalid(key: String, element: Any) =
                CPropertyException(String.format(I18N["exception.property.property_element_invalid.format.ek"], element.toString(), key))
            fun propertyListSizeInvalid(key: String, size: Int) =
                CPropertyException(String.format(I18N["exception.property.property_list_size_invalid.format.sk"], size, key))
        }
    }

    // ----- Constructors ----- //

    constructor(key: String, value: Boolean): this(key, value.toString())
    constructor(key: String, value: Number) : this(key, value.toString())
    constructor(key: String, value: List<*>) : this(key, parseList(value))
    constructor(key: String, vararg value: Any) : this(key, listOf(*value))

    fun isUninitialized(): Boolean {
        return value == UNINITIALIZED
    }

    fun asString(): String {
        return value
    }
    fun asBoolean(): Boolean {
        return value.toBoolean()
    }
    fun asInteger(): Int {
        return value.toInt()
    }
    fun asInteger(radix: Int): Int {
        return value.toInt(radix)
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

            val first = pair.substring(1, sepIndex)
            val second = pair.substring(sepIndex + 2, pair.length - 1)
            Pair(first, second)
        }
    }
    fun asStringMap(): Map<String, String> {
        val list = asPairList()
        val map = HashMap<String, String>()
        for (pair in list) map[pair.first] = pair.second

        return map
    }
    fun asBooleanMap(): Map<String, Boolean> {
        val list = asPairList()
        val map = HashMap<String, Boolean>()
        for (pair in list) map[pair.first] = pair.second.toBoolean()

        return map
    }
    fun asIntegerMap(): Map<String, Int> {
        val list = asPairList()
        val map = HashMap<String, Int>()
        for (pair in list) map[pair.first] = pair.second.toInt()

        return map
    }
    fun asDoubleMap(): Map<String, Double> {
        val list = asPairList()
        val map = HashMap<String, Double>()
        for (pair in list) map[pair.first] = pair.second.toDouble()

        return map
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
