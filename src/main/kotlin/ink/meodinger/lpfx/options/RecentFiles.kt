package ink.meodinger.lpfx.options

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
object RecentFiles : AbstractProperties("Recent Files") {

    private const val MAX_SIZE = 20
    private const val RECENT   = "RecentFiles"
    private const val PROGRESS = "ProgressMap"

    // TODO: RecentFiles use last as first to improve performance

    private val recentFilesProperty: ListProperty<File> = SimpleListProperty()
    fun recentFilesProperty(): ListProperty<File> = recentFilesProperty
    val recentFiles: ObservableList<File> by recentFilesProperty

    private val lastFileProperty: ObjectProperty<File?> = SimpleObjectProperty()
    fun lastFileProperty(): ReadOnlyObjectProperty<File?> = lastFileProperty
    val lastFile: File? by lastFileProperty

    private val progressMapProperty: MapProperty<String, Pair<Int, Int>> = SimpleMapProperty()
    fun progressMapProperty(): MapProperty<String, Pair<Int, Int>> = progressMapProperty
    val progressMap: ObservableMap<String, Pair<Int, Int>> by progressMapProperty

    override val default = listOf(
        CProperty(RECENT),
        CProperty(PROGRESS),
    )

    init { useDefault() }

    @Throws(IOException::class, NumberFormatException::class)
    override fun load() {
        load(Options.recentFiles, this)

        val recentPaths = this[RECENT].asStringList()
        recentFilesProperty.set(FXCollections.observableArrayList(recentPaths.map(::File)))

        lastFileProperty.bind(recentFilesProperty.valueAt(0))

        val progressPairList = this[PROGRESS].asPairList().map { it.first.toInt() to it.second.toInt() }
        val progressList = recentPaths.mapIndexed { index, path -> path to progressPairList.getOrElse(index) { -1 to -1 } }
        progressMapProperty.set(FXCollections.observableHashMap<String, Pair<Int, Int>>().apply { putAll(progressList) })
    }

    @Throws(IOException::class)
    override fun save() {
        this[RECENT].set(recentFiles)
        this[PROGRESS].set(recentFiles.map { progressMap[it.path] })

        save(Options.recentFiles, this)
    }

    fun add(file: File) {
        recentFiles.remove(file)
        recentFiles.add(0, file)

        if (progressMap[file.path] == null) progressMap[file.path] = (-1 to -1)

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
