package info.meodinger.lpfx.options

import info.meodinger.lpfx.options.CProperty.Companion.COMMENT_HEAD
import info.meodinger.lpfx.options.CProperty.Companion.KV_SEPARATOR
import info.meodinger.lpfx.util.using

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.options
 */

/**
 * Abstract properties structure for save/load/check
 */
abstract class AbstractProperties {

    companion object {
        @Throws(IOException::class, CPropertyException::class)
        fun load(path: Path, instance: AbstractProperties) {
            try {
                val lines = Files.newBufferedReader(path).readLines()
                for (line in lines) {
                    if (line.isBlank()) continue
                    if (line.trim().startsWith(COMMENT_HEAD)) continue

                    val prop = line.split(KV_SEPARATOR, limit = 2)
                    instance[prop[0]] = prop[1]
                }
            } catch (e: Exception) {
                throw e
            }
        }

        @Throws(IOException::class)
        fun save(path: Path, instance: AbstractProperties) {
            using {
                val writer = Files.newBufferedWriter(path).autoClose()
                for (property in instance.properties) {
                    writer.write(StringBuilder()
                        .append(property.key)
                        .append(KV_SEPARATOR)
                        .append(property.value)
                        .append("\n")
                        .toString()
                    )
                }
            } catch { e: Exception ->
                throw e
            } finally {

            }
        }
    }

    val properties = ArrayList<CProperty>()
    abstract val default: List<CProperty>

    @Throws(CPropertyException::class, IOException::class)
    abstract fun load()
    @Throws(IOException::class)
    abstract fun save()
    @Throws(CPropertyException::class)
    open fun check() {
        for (property in default) if (this[property.key].isEmpty()) this[property.key] = property
    }
    fun useDefault() {
        properties.clear()
        properties.addAll(default)
    }

    operator fun get(key: String): CProperty {
        for (property in properties) {
            if (property.key == key) {
                return property
            }
        }
        throw CPropertyException.propertyNotFound(key)
    }
    operator fun set(key: String, value: String) {
        get(key).value = value
    }
    operator fun set(key: String, value: Boolean) {
        set(key, value.toString())
    }
    operator fun set(key: String, value: Number) {
        set(key, value.toString())
    }
    operator fun set(key: String, value: List<*>) {
        set(key, CProperty.parseList(value))
    }
    operator fun set(key: String, another: CProperty) {
        set(key, another.value)
    }
}