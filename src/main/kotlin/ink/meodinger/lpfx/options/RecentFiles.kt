package ink.meodinger.lpfx.options

import java.io.IOException
import java.nio.file.InvalidPathException
import java.nio.file.Path


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * The recent files that user opens while using
 */
object RecentFiles : AbstractProperties() {

    private const val MAX_SIZE = 10
    private const val RECENT = "recent"

    private val recent = ArrayDeque<String>()

    override val default = listOf(CProperty(RECENT, CProperty.EMPTY)).toPropertiesMap()

    init { useDefault() }

    @Throws(IOException::class)
    override fun load() {
        load(Options.recentFiles, this)

        recent.clear()
        recent.addAll(this[RECENT].asStringList())
    }

    @Throws(IOException::class)
    override fun save() {
        this[RECENT] = recent

        save(Options.recentFiles, this)
    }

    override fun checkAndFix(): Boolean {
        val toRemove = HashSet<String>()
        recent.forEach {
            try {
                Path.of(it)
            } catch (e: InvalidPathException) {
                toRemove.add(it)
            }
        }
        recent.removeAll(toRemove)
        this[RECENT] = recent

        return toRemove.size != 0
    }

    fun getAll(): List<String> {
        return recent
    }

    fun getLastOpenFile(): String? {
        return recent.firstOrNull()
    }

    fun add(path: String) {
        recent.remove(path)
        recent.addFirst(path)

        if (recent.size > MAX_SIZE) recent.removeLast()
    }

    fun remove(path: String) {
        recent.remove(path)
    }
}
