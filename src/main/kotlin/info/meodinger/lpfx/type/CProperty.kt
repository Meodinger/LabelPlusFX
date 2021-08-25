package info.meodinger.lpfx.type

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.type
 */
class CProperty(val key: String, var value: String) {

    companion object {
        const val LIST_SEPARATOR = "|"
        const val KV_SEPARATOR = "="
        const val COMMENT_HEAD = "#"

        fun parseList(values: List<*>): String {
            val builder = StringBuilder()
            for (value in values) builder.append(value).append(LIST_SEPARATOR)
            if (builder.isNotEmpty())
                builder.deleteCharAt(builder.length - 1)
            return builder.toString()
        }
    }

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
    fun asInteger(): Int {
        return value.toInt()
    }
    fun asDouble(): Double {
        return value.toDouble()
    }
    fun asStringList(): List<String> {
        if (value.isBlank()) return emptyList()
        return listOf(*value.split(LIST_SEPARATOR).toTypedArray())
    }
    fun asBooleanList(): List<Boolean> {
        if (value.isBlank()) return emptyList()
        val rawList = value.split(LIST_SEPARATOR)
        return MutableList(rawList.size) { rawList[it].toBoolean() }
    }
    fun asIntegerList(): List<Int> {
        if (value.isBlank()) return emptyList()
        val rawList = value.split(LIST_SEPARATOR)
        return MutableList(rawList.size) { rawList[it].toInt() }
    }
    fun asDoubleList(): List<Double> {
        if (value.isBlank()) return emptyList()
        val rawList = value.split(LIST_SEPARATOR)
        return MutableList(rawList.size) { rawList[it].toDouble() }
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

    override fun toString() = "CProperty(key=${key}, value=${value})"

}
