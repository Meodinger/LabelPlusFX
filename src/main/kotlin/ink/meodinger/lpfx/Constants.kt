package ink.meodinger.lpfx

import ink.meodinger.lpfx.util.Promise
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.control.TextFormatter
import java.io.File
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.system.exitProcess


/**
 * Author: Meodinger
 * Date: 2021/8/1
 * Have fun with my code!
 */

/**
 * Hooked Application
 */
abstract class HookedApplication : Application() {
    private val shutdownHooks = LinkedHashMap<String, (() -> Unit) -> Unit>()

    /**
     * Clear all shutdown hooks
     */
    fun clearShutdownHooks() = shutdownHooks.clear()

    /**
     * Add a shutdown hook
     * Should use the resolve function as callback
     */
    fun addShutdownHook(key: String, onShutdown: (() -> Unit) -> Unit) = shutdownHooks.put(key, onShutdown)

    /**
     * Remove a shutdown hook
     */
    fun removeShutdownHook(key: String) = shutdownHooks.remove(key)

    /**
     * MUST run this before exit or hooks will not be executed
     */
    protected fun runShutdownHooksAndExit() {

        if (shutdownHooks.isEmpty()) {
            Platform.exit()
        } else {
            val values = shutdownHooks.values.toList()
            Promise.all(List(shutdownHooks.size) {
                Promise<Unit> { resolve, _ -> values[it] { resolve(Unit) } }
            }) finally {
                Platform.exit()
            }

            // In case of something unexpected happened and the app cannot be shutdown
            Timer().schedule(object : TimerTask() {
                override fun run() { exitProcess(0) }
            }, 1000L * 60 * 5)

            clearShutdownHooks()
        }
    }

    override fun stop() {
        runShutdownHooksAndExit()
    }
}

/**
 * Stage size
 */
const val WIDTH = 900.0
const val HEIGHT = 600.0

/**
 * Scroll Delta Default
 */
const val SCROLL_DELTA = 32.0

/**
 * TreeItem Graphic radius
 */
const val GRAPHICS_CIRCLE_RADIUS = 8.0

/**
 * Extensions
 */
val EXTENSIONS_PIC = listOf("png", "jpg", "jpeg")
const val EXTENSION_LP = "txt"
const val EXTENSION_MEO = "json"
const val EXTENSION_PACK = "zip"
const val EXTENSION_BAK = "bak"
const val FOLDER_NAME_BAK = "backup"

/**
 * Filenames
 */
const val INITIAL_FILE_NAME = "New Translation"
const val RECOVERY_FILE_NAME = "Recovery"
const val PACKAGE_FILE_NAME = "Package"
const val EXPORT_FILE_NAME = "Export"

/**
 * Auto-save
 */
const val AUTO_SAVE_DELAY = 5 * 60 * 1000L
const val AUTO_SAVE_PERIOD = 3 * 60 * 1000L

/**
 * For label/group not found
 */
const val NOT_FOUND = -1

/**
 * Work Mode
 * @param description Display name for WorkMode
 */
enum class WorkMode(val description: String) {
    InputMode(I18N["mode.work.input"]),
    LabelMode(I18N["mode.work.label"]);

    override fun toString(): String = description

    companion object {
        fun getWorkMode(description: String): WorkMode = when (description) {
            InputMode.description -> InputMode
            LabelMode.description -> LabelMode
            else -> throw IllegalArgumentException("exception.illegal_argument.invalid_work_mode")
        }
    }
}
val DEFAULT_WORK_MODE = WorkMode.InputMode

/**
 * Label View Mode
 * @param description Display name for ViewMode
 */
enum class ViewMode(private val description: String) {
    IndexMode(I18N["mode.view.index"]),
    GroupMode(I18N["mode.view.group"]);

    override fun toString(): String = description

    companion object {
        fun getMode(description: String): ViewMode = when (description) {
            IndexMode.description -> IndexMode
            GroupMode.description -> GroupMode
            else -> throw IllegalArgumentException("exception.illegal_argument.invalid_view_mode")
        }
    }
}
val DEFAULT_VIEW_MODE = ViewMode.IndexMode

/**
 * Translation File Type
 */
enum class FileType(private val description: String) {
    LPFile(I18N["filetype.translation_lp"]),
    MeoFile(I18N["filetype.translation_meo"]);

    override fun toString(): String = description

    companion object {
        private fun isLPFile(file: File): Boolean = file.extension == EXTENSION_LP
        private fun isMeoFile(file: File): Boolean = file.extension == EXTENSION_MEO
        fun getType(file: File): FileType {
            if (isLPFile(file)) return LPFile
            if (isMeoFile(file)) return MeoFile
            throw IllegalArgumentException(I18N["exception.illegal_argument.invalid_file_extension"])
        }
    }
}

/**
 * Get a TextFormatter for TransGroup name
 */
fun getGroupNameFormatter() = TextFormatter<String> { change ->
    change.text = change.text.trim().replace(Regex("[| ]"), "_")

    change
}

/**
 * Get a TextFormatter for CProperty
 */
fun getPropertyFormatter() = TextFormatter<String> { change ->
    change.text = change.text.trim().replace(Regex("[|, ]"), "_")

    change
}