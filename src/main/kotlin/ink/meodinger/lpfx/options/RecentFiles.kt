package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.util.property.firstElement
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

    private val recentFilesProperty: ListProperty<File> = SimpleListProperty()
    /**
     * Recent opened files, the first is the most recent file
     */
    fun recentFilesProperty(): ListProperty<File> = recentFilesProperty
    /**
     * @see recentFilesProperty
     */
    val recentFiles: ObservableList<File> by recentFilesProperty

    private val progressMapProperty: MapProperty<String, Pair<Int, Int>> = SimpleMapProperty()
    /**
     * The work progress map, use File::path as key
     */
    fun progressMapProperty(): MapProperty<String, Pair<Int, Int>> = progressMapProperty
    /**
     * @see progressMapProperty
     */
    val progressMap: ObservableMap<String, Pair<Int, Int>> by progressMapProperty

    private val lastFileProperty: ObjectProperty<File?> = SimpleObjectProperty()
    /**
     * The most recent file
     */
    fun lastFileProperty(): ReadOnlyObjectProperty<File?> = lastFileProperty
    /**
     * @see lastFileProperty
     */
    val lastFile: File? by lastFileProperty

    override val default = listOf(
        CProperty(RECENT),
        CProperty(PROGRESS),
    )

    init { useDefault() }

    @Throws(IOException::class, NumberFormatException::class)
    override fun load() {
        load(Options.recentFiles, this)

        val recentPaths = this[RECENT].asStringList().map(::File)
        val progressPairList = this[PROGRESS].asPairList().map { it.first.toInt() to it.second.toInt() }
        val progressList = recentPaths.mapIndexed { index, file -> file.path to progressPairList[index] }.toMap()

        recentFilesProperty.set(FXCollections.observableList(ArrayList(recentPaths)))
        progressMapProperty.set(FXCollections.observableMap(HashMap(progressList)))

        lastFileProperty.bind(recentFilesProperty.firstElement())
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

        // Setup progress
        if (progressMap[file.path] == null) progressMap[file.path] = (-1 to -1)
        // Keep 20 recent files
        if (recentFiles.size > MAX_SIZE) progressMap.remove(recentFiles.removeLast().path)
    }
    fun remove(file: File) {
        recentFiles.remove(file)
        progressMap.remove(file.path)
    }

    fun getProgressOf(path: String): Pair<Int, Int> {
        return progressMap[path] ?: (-1 to -1)
    }
    fun setProgressOf(path: String, progress: Pair<Int, Int>) {
        progressMap[path] = progress
    }

}
