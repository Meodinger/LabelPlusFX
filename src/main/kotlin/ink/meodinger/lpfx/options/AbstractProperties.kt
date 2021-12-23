package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.using

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * Abstract properties structure for save/load/check
 */
abstract class AbstractProperties {

    companion object {

        private const val KV_SPILT = "="
        private const val COMMENT_HEAD = "#"

        @Throws(IOException::class)
        fun load(path: Path, instance: AbstractProperties) {
            try {
                val lines = Files.newBufferedReader(path).readLines()
                for (line in lines) {
                    if (line.isBlank()) continue
                    if (line.trim().startsWith(COMMENT_HEAD)) continue

                    val prop = line.split(KV_SPILT, limit = 2)
                    instance[prop[0]] = prop[1]
                }
            } catch (e: IndexOutOfBoundsException) {
                throw IOException("Load properties failed: KV format invalid").initCause(e)
            } catch (e: SecurityException) {
                throw IOException("Load properties failed: Security manager installed, check first").initCause(e)
            } catch (e: IOException) {
                throw IOException("Save properties failed").initCause(e)
            }
        }

        @Throws(IOException::class)
        fun save(path: Path, instance: AbstractProperties, comments: Map<String, String> = emptyMap()) {
            using {
                val writer = Files.newBufferedWriter(path).autoClose()
                for (property in instance.properties) {
                    if (comments[property.key] != null) {
                        writer.write(
                            StringBuilder()
                                .append("\n").append(COMMENT_HEAD).append(" ")
                                .append(comments[property.key]?.replace("\n", "\n$COMMENT_HEAD "))
                                .append("\n")
                                .toString()
                        )
                    }
                    writer.write(
                        StringBuilder()
                            .append(property.key)
                            .append(KV_SPILT)
                            .append(property.value)
                            .append("\n")
                            .toString()
                    )
                }
            } catch { e: SecurityException ->
                throw IOException("Save properties failed: Security manager installed, check first").initCause(e)
            } catch { e: Exception ->
                throw IOException("Save properties failed").initCause(e)
            }
        }

        fun List<CProperty>.toPropertiesMap(): Map<String, String> {
            val map = HashMap<String, String>()
            for (property in this) map[property.key] = property.value
            return map
        }

        /**
         * Be careful!
         */
        fun getProperties(properties: AbstractProperties): List<CProperty> = properties.properties
    }

    protected abstract val default: Map<String, String>
    protected val properties = ArrayList<CProperty>()

    @Throws(IOException::class)
    abstract fun load()
    @Throws(IOException::class)
    abstract fun save()

    abstract fun checkAndFix(): Boolean

    fun useDefault() {
        properties.clear()
        default.forEach { (k, v) -> properties.add(CProperty(k, v)) }
    }

    operator fun get(key: String): CProperty {
        for (property in properties) {
            if (property.key == key) {
                return property
            }
        }
        throw RuntimeException(String.format(I18N["exception.property.property_not_found.k"], key))
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
