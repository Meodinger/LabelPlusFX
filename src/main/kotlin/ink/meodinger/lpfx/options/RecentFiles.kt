package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.util.addFirst
import ink.meodinger.lpfx.util.property.getValue

import javafx.beans.property.*
import javafx.collections.*
import java.io.File
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

    private val recentFilesProperty: ListProperty<File> = SimpleListProperty()
    fun recentFilesProperty(): ListProperty<File> = recentFilesProperty
    val recentFiles: ObservableList<File> by recentFilesProperty

    private val progressMapProperty: MapProperty<String, Pair<Int, Int>> = SimpleMapProperty()
    fun progressMapProperty(): MapProperty<String, Pair<Int, Int>> = progressMapProperty
    val progressMap: ObservableMap<String, Pair<Int, Int>> by progressMapProperty

    private val lastFileProperty: ObjectProperty<File?> = SimpleObjectProperty()
    fun lastFileProperty(): ObjectProperty<File?> = lastFileProperty
    val lastFile: File? by lastFileProperty

    override val default = listOf(
        CProperty(RECENT),
        CProperty(PROGRESS)
    )

    init { useDefault() }

    @Throws(IOException::class)
    override fun load() {
        load(Options.recentFiles, this)

        val recentPaths = this[RECENT].asStringList()
        recentFilesProperty.set(FXCollections.observableArrayList(recentPaths.map(::File)))

        val progressList = this[PROGRESS].asPairList().map { it.first.toInt() to it.second.toInt() }
        val progressMap = recentPaths.mapIndexed { index, path -> path to progressList.getOrElse(index) { -1 to -1 } }
        progressMapProperty.set(FXCollections.observableHashMap<String, Pair<Int, Int>>().apply { putAll(progressMap) })

        lastFileProperty.set(recentFiles.firstOrNull())
    }

    @Throws(IOException::class)
    override fun save() {
        this[RECENT].set(recentFiles)
        this[PROGRESS].set(recentFiles.map { progressMap[it.path] })

        save(Options.recentFiles, this)
    }

    fun add(file: File) {
        recentFiles.remove(file)
        recentFiles.addFirst(file)

        progressMap[file.path] = progressMap[file.path] ?: (-1 to -1)

        if (recentFiles.size > MAX_SIZE) progressMap.remove(recentFiles.removeLast().path)
    }

    fun remove(file: File) {
        recentFiles.remove(file)
        progressMap.remove(file.path)
    }

    fun getProgressOf(path: String): Pair<Int, Int> {
        return progressMap[path] ?: (-1 to -1)
    }

    fun setProgressOf(path: String, current: Pair<Int, Int>) {
        progressMap[path] = current
    }

}
