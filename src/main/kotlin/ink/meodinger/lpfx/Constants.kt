package ink.meodinger.lpfx

import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get

import javafx.scene.control.TextFormatter
import java.io.File


/**
 * Author: Meodinger
 * Date: 2021/8/1
 * Have fun with my code!
 */

/**
 * Size related constants
 */
const val WINDOW_WIDTH  = 900.0
const val WINDOW_HEIGHT = 600.0
const val DIALOG_WIDTH  = 600.0
const val DIALOG_HEIGHT = 400.0
const val COMMON_GAP    = 16.0

/**
 * Scroll Delta Default
 */
const val SCROLL_DELTA = 32.0

/**
 * TreeItem Graphic radius
 */
const val GRAPHICS_CIRCLE_RADIUS = 8.0

const val FOLDER_NAME_BAK    = "backup"
const val EXTENSION_PIC_PNG  = "png"
const val EXTENSION_PIC_JPG  = "jpg"
const val EXTENSION_PIC_JPEG = "jpeg"
const val EXTENSION_FILE_LP  = "txt"
const val EXTENSION_FILE_MEO = "json"
const val EXTENSION_PACK     = "zip"
const val EXTENSION_BAK      = "bak"

/**
 * Extensions: png, jpg, jpeg
 */
val EXTENSIONS_PIC  = listOf(EXTENSION_PIC_PNG, EXTENSION_PIC_JPG, EXTENSION_PIC_JPEG)
val EXTENSIONS_FILE = listOf(EXTENSION_FILE_LP, EXTENSION_FILE_MEO)

/**
 * Filenames
 */
const val INITIAL_FILE_NAME  = "New Translation"
const val RECOVERY_FILE_NAME = "Recovery"
const val PACKAGE_FILE_NAME  = "Package"
const val EXPORT_FILE_NAME   = "Export"

/**
 * For label/group not found
 */
const val NOT_FOUND = -1

const val LOGSRC_APPLICATION = "Application"
const val LOGSRC_CONTROLLER  = "Controller"
const val LOGSRC_STATE       = "State"
const val LOGSRC_OPTIONS     = "Options"
const val LOGSRC_DIALOGS     = "Dialog"
const val LOGSRC_LOGGER      = "Logger"
const val LOGSRC_SENDER      = "Sender"
const val LOGSRC_CHECKER     = "UpdateCheck"

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

/**
 * Label View Mode
 * @param description Display name for ViewMode
 */
enum class ViewMode(private val description: String) {
    IndexMode(I18N["mode.view.index"]),
    GroupMode(I18N["mode.view.group"]);

    override fun toString(): String = description

    companion object {
        fun getViewMode(description: String): ViewMode = when (description) {
            IndexMode.description -> IndexMode
            GroupMode.description -> GroupMode
            else -> throw IllegalArgumentException("exception.illegal_argument.invalid_view_mode")
        }
    }
}

/**
 * Translation File Type
 */
enum class FileType(private val description: String) {
    LPFile(I18N["filetype.translation_lp"]),
    MeoFile(I18N["filetype.translation_meo"]);

    override fun toString(): String = description

    companion object {
        private fun isLPFile(file: File): Boolean = file.extension == EXTENSION_FILE_LP
        private fun isMeoFile(file: File): Boolean = file.extension == EXTENSION_FILE_MEO
        fun getType(file: File): FileType {
            if (isLPFile(file)) return LPFile
            if (isMeoFile(file)) return MeoFile
            throw IllegalArgumentException(String.format(I18N["exception.file_type.invalid_file_extension.s"], file.extension))
        }
    }
}

/**
 * Get a TextFormatter for TransGroup name
 */
fun getGroupNameFormatter(): TextFormatter<String> {
    return TextFormatter<String> { it.also {
        it.text = it.text.trim().replace(Regex("[| ]"), "_")
    } }
}

/**
 * Get a TextFormatter for CProperty
 */
fun getPropertyFormatter(): TextFormatter<String> {
    return  TextFormatter<String> { it.also {
        it.text = it.text.trim().replace(Regex("[|, ]"), "_")
    } }
}
