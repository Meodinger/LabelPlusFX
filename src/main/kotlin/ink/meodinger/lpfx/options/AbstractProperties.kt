package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.using

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.collections.ArrayList


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * Abstract properties structure for save/load/check
 */
abstract class AbstractProperties(val name: String) {

    companion object {

        private const val COMMENT_HEAD = '#'
        private const val KV_SPILT = '='

        @Throws(IOException::class)
        fun load(path: Path, instance: AbstractProperties) {
            try {
                val lines = Files.newBufferedReader(path).readLines()
                var index = 0

                while (index < lines.size) {
                    val line = lines[index]

                    if (line.isBlank()) continue
                    if (line.trim().startsWith(COMMENT_HEAD)) continue

                    val prop = line.split(KV_SPILT, limit = 2).takeIf {
                        it.size == 2 && instance.tryGet(it[0]) != null
                    } ?: continue

                    // property=|
                    // |element 1
                    // |element 2
                    // ...
                    if (prop[1][0] == CProperty.LIST_SEPARATOR) {
                        val propList = ArrayList<String>()
                        while (++index < lines.size && lines[index].startsWith(CProperty.LIST_SEPARATOR)) {
                            propList.add(lines[index].substring(1))
                        }
                        instance[prop[0]].set(propList)
                    } else {
                        instance[prop[0]].set(prop[1])
                        index++
                    }
                }
            } catch (e: IndexOutOfBoundsException) {
                throw IOException("Load properties failed: KV format invalid").initCause(e)
            } catch (e: IOException) {
                throw IOException("Save properties I/O failed").initCause(e)
            }
        }

        @Throws(IOException::class)
        fun save(path: Path, instance: AbstractProperties) {
            using {
                val writer = Files.newBufferedWriter(path).autoClose()
                for (property in instance.properties) {
                    val builder = StringBuilder()
                    if (property.isList) {
                        builder.append(property.key).append(KV_SPILT).append(CProperty.LIST_SEPARATOR).append('\n')
                        for (value in property.asStringList()) builder.append(CProperty.LIST_SEPARATOR).appendLine(value)
                    } else {
                        builder.append(property.key).append(KV_SPILT).append(property.value).append('\n')
                    }
                    writer.write(builder.toString())
                }
            } catch { e: Exception ->
                throw IOException("Save properties I/O failed").initCause(e)
            }
        }

        /**
         * Be careful!
         */
        fun getPropertiesOf(properties: AbstractProperties): List<CProperty> = properties.properties
    }

    protected abstract val default: List<CProperty>
    protected val properties = ArrayList<CProperty>()

    @Throws(IOException::class, NumberFormatException::class)
    abstract fun load()
    @Throws(IOException::class)
    abstract fun save()

    fun useDefault() {
        properties.clear()
        properties.addAll(default.map(CProperty::copy))
    }

    private fun tryGet(key: String): CProperty? {
        for (property in properties) if (property.key == key) return property
        return null
    }

    operator fun get(key: String): CProperty = tryGet(key) ?: throw IllegalArgumentException(I18N["exception.property.property_not_found.k"])

}
