package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.util.doNothing
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
 * @param name The display name of the properties
 * @param path The path to properties file
 */
abstract class AbstractProperties(val name: String, val path: Path) {

    companion object {

        private const val COMMENT_HEAD = '#'
        private const val KV_SPILT = '='

        @Throws(IOException::class)
        fun load(instance: AbstractProperties) {
            using {
                val reader = Files.newBufferedReader(instance.path).autoClose()
                val lines = reader.readLines()
                var index = 0

                while (index < lines.size) {
                    val line = lines[index++]

                    if (line.isBlank()) continue
                    if (line.trim().startsWith(COMMENT_HEAD)) continue

                    val prop = line.split(KV_SPILT, limit = 2).takeIf {
                        it.size == 2 && instance.properties.any { p -> p.key == it[0] }
                    } ?: continue

                    // property=|
                    // |element 1
                    // |element 2
                    // ...
                    if (prop[1].isNotEmpty() && prop[1][0] == CProperty.LIST_SEPARATOR) {
                        val propList = ArrayList<String>()
                        while (index < lines.size && lines[index].startsWith(CProperty.LIST_SEPARATOR)) {
                            propList.add(lines[index++].substring(1))
                        }
                        instance.find(prop[0])?.set(propList)
                    } else {
                        instance.find(prop[0])?.set(prop[1])
                    }
                }
            } catch { e: IndexOutOfBoundsException ->
                throw IOException("Load properties failed: KV format invalid").initCause(e)
            } catch { e: IOException ->
                throw IOException("Save properties I/O failed").initCause(e)
            } finally ::doNothing
        }

        @Throws(IOException::class)
        fun save(instance: AbstractProperties) {
            using {
                val writer = Files.newBufferedWriter(instance.path).autoClose()
                for (property in instance.properties) {
                    val builder = StringBuilder()
                    if (property.isList) {
                        builder.append(property.key).append(KV_SPILT).append(CProperty.LIST_SEPARATOR).append('\n')
                        for (value in property.asStringList()) builder.append(CProperty.LIST_SEPARATOR).appendLine(value)
                    } else {
                        builder.append(property.key).append(KV_SPILT).appendLine(property.value)
                    }
                    writer.write(builder.toString())
                }
            } catch { e: IOException ->
                throw IOException("Save properties I/O failed").initCause(e)
            } finally ::doNothing
        }
    }

    /**
     * A List of CProperties that hold defaults
     */
    protected abstract val default: List<CProperty>

    /**
     * Actual List of CProperties
     */
    protected val properties: ArrayList<CProperty> = ArrayList()

    @Throws(IOException::class, NumberFormatException::class)
    abstract fun load()
    @Throws(IOException::class)
    abstract fun save()

    fun useDefault() {
        properties.clear()
        properties.addAll(default.map(CProperty::copy))
    }

    operator fun get(key: String): CProperty = properties.first() { it.key == key }

    fun find(key: String): CProperty? = properties.firstOrNull() { it.key == key }

    override fun toString(): String = properties.joinToString(", \n", transform = CProperty::toString)

}
