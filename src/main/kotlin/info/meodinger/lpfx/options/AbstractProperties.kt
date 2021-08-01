package info.meodinger.lpfx.options

import info.meodinger.lpfx.type.CProperty
import info.meodinger.lpfx.type.CProperty.Companion.COMMENT_HEAD
import info.meodinger.lpfx.type.CProperty.Companion.KV_SEPARATOR
import info.meodinger.lpfx.util.dialog.showException
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
                val properties = ArrayList<CProperty>()
                val lines = Files.newBufferedReader(path).readLines()
                for (line in lines) {
                    if (line.isBlank()) continue
                    if (line.trim().startsWith(COMMENT_HEAD)) continue

                    val prop = line.split(KV_SEPARATOR, limit = 2)
                    properties.add(CProperty(prop[0], prop[1]))
                }

                instance.properties.clear()
                instance.properties.addAll(properties)
            } catch (e: IOException) {
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
    operator fun get(key: String): CProperty {
        for (property in properties) {
            if (property.key == key) {
                return property
            }
        }
        throw IllegalStateException("Property not found")
    }
}