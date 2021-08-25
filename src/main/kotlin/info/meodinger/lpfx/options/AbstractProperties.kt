package info.meodinger.lpfx.options

import info.meodinger.lpfx.type.CProperty
import info.meodinger.lpfx.type.CProperty.Companion.COMMENT_HEAD
import info.meodinger.lpfx.type.CProperty.Companion.KV_SEPARATOR
import info.meodinger.lpfx.util.dialog.showException
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get
import info.meodinger.lpfx.util.using

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.options
 */
abstract class AbstractProperties {

    companion object {
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
                showException(e)
            }
        }

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
            } catch { e: IOException ->
                showException(e)
            } finally {

            }
        }
    }

    val properties = ArrayList<CProperty>()

    abstract fun load()
    abstract fun save()
    @Throws(Exception::class)
    abstract fun check()

    operator fun get(key: String): CProperty {
        for (property in properties) {
            if (property.key == key) {
                return property
            }
        }
        throw IllegalArgumentException(String.format(I18N["exception.illegal_argument.property_not_found.format"], key))
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
}