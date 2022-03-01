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
    private const val RECENT   = "RecentFiles"
    private const val PROGRESS = "ProgressMap"

    private val recentFiles = ArrayDeque<String>()
    private val progressMap = HashMap<String, Pair<Int, Int>>()

    override val default = listOf(
        CProperty(RECENT),
        CProperty(PROGRESS)
    )

    init { useDefault() }

    @Throws(IOException::class)
    override fun load() {
        load(Options.recentFiles, this)

        recentFiles.addAll(this[RECENT].asStringList())

        val progressList = this[PROGRESS].asPairList().map { it.first.toInt() to it.second.toInt() }
        progressMap.putAll(recentFiles.mapIndexed { index, path -> path to progressList.getOrElse(index) { -1 to -1 } })
    }

    @Throws(IOException::class)
    override fun save() {
        this[RECENT].set(recentFiles)
        this[PROGRESS].set(recentFiles.map(progressMap::get))

        save(Options.recentFiles, this)
    }

    fun getAll(): List<String> {
        return recentFiles
    }

    fun getLastOpenFile(): String? {
        return recentFiles.firstOrNull()
    }

    fun add(path: String) {
        recentFiles.remove(path)
        recentFiles.addFirst(path)

        progressMap[path] = progressMap[path] ?: (-1 to -1)

        if (recentFiles.size > MAX_SIZE) progressMap.remove(recentFiles.removeLast())
    }

    fun remove(path: String) {
        recentFiles.remove(path)
        progressMap.remove(path)
    }

    fun getProgressOf(path: String): Pair<Int, Int> {
        return progressMap[path] ?: (-1 to -1)
    }

    fun setProgressOf(path: String, current: Pair<Int, Int>) {
        progressMap[path] = current
    }

}
