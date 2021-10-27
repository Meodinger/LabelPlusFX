package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.options.CProperty.CPropertyException

import java.io.IOException


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: ink.meodinger.lpfx.io
 */

/**
 * The recent files that user opens while using
 */
object RecentFiles : AbstractProperties() {

    private const val MAX_SIZE = 10
    private const val RECENT = "recent"

    override val default = listOf(CProperty(RECENT))

    private val recent = ArrayList<String>()

    init {
        this.properties.addAll(listOf(
            CProperty(RECENT)
        ))
    }

    @Throws(IOException::class, CPropertyException::class)
    override fun load() {
        load(Options.recentFiles, this)

        recent.clear()
        recent.addAll(this[RECENT].asStringList())
    }

    @Throws(IOException::class)
    override fun save() {
        this[RECENT].set(recent)

        save(Options.recentFiles, this)
    }

    fun getAll(): List<String> {
        return recent
    }

    fun getLastOpenFile(): String? {
        if (recent.size == 0) return null
        return recent[0]
    }

    fun add(path: String) {
        recent.remove(path)
        recent.add(0, path)
        if (recent.size > MAX_SIZE) {
            recent.removeAt(MAX_SIZE)
        }
    }

    fun remove(path: String) {
        recent.remove(path)
    }
}