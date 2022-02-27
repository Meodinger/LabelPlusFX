package ink.meodinger.lpfx.options

import java.io.IOException


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
    private const val RECENT   = "recent"
    private const val PROGRESS = "progress"

    private val recent = ArrayDeque<String>()
    private val progress = HashMap<String, Pair<Int, Int>>()

    override val default = listOf(
        CProperty(RECENT),
        CProperty(PROGRESS)
    )

    init { useDefault() }

    @Throws(IOException::class)
    override fun load() {
        load(Options.recentFiles, this)

        recent.addAll(this[RECENT].asStringList())

        val progressList = this[PROGRESS].asPairList().map { it.first.toInt() to it.second.toInt() }
        progress.putAll(recent.mapIndexed { index, path -> path to progressList.getOrElse(index) { -1 to -1 } })
    }

    @Throws(IOException::class)
    override fun save() {
        this[RECENT].set(recent)
        this[PROGRESS].set(recent.map(progress::get))

        save(Options.recentFiles, this)
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

        progress[path] = progress[path] ?: (-1 to -1)

        if (recent.size > MAX_SIZE) progress.remove(recent.removeLast())
    }

    fun remove(path: String) {
        recent.remove(path)
        progress.remove(path)
    }

    fun getProgressOf(path: String): Pair<Int, Int> {
        return progress[path] ?: (-1 to -1)
    }

    fun setProgressOf(path: String, current: Pair<Int, Int>) {
        progress[path] = current
    }

}
