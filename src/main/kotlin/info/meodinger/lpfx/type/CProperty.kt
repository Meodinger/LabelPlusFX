package info.meodinger.lpfx.type

import java.util.*

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

        fun parseList(values: List<String>): String {
            val builder = StringBuilder()
            for (value in values) builder.append(value).append(LIST_SEPARATOR)
            if (builder.isNotEmpty())
                builder.deleteCharAt(builder.length - 1)
            return builder.toString()
        }
    }

    constructor(key: String, value: Number) : this(key, value.toString())
    constructor(key: String, value: List<String>) : this(key, parseList(value))
    constructor(key: String, vararg value: String) : this(key, listOf(*value))

    fun asString(): String {
        return value
    }
    fun asInteger(): Int {
        return value.toInt()
    }
    fun asDouble(): Double {
        return value.toDouble()
    }
    fun asList(): List<String> {
        if (value.isBlank()) return emptyList()
        return listOf(*value.split(LIST_SEPARATOR).toTypedArray())
    }

    fun set(value: String) {
        this.value = value
    }
    fun set(value: Number) {
        set(value.toString())
    }
    fun set(list: List<String>) {
        set(parseList(list))
    }
    fun set(vararg list: String) {
        set(listOf(*list))
    }

    override fun toString() = "CProperty(key=${key}, value=${value})"

}
