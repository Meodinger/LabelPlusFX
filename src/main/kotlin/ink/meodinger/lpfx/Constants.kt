package ink.meodinger.lpfx

import ink.meodinger.lpfx.util.Version
import ink.meodinger.lpfx.util.component.genTextFormatter

import javafx.scene.control.TextFormatter
import java.io.File


/**
 * Author: Meodinger
 * Date: 2021/8/1
 * Have fun with my code!
 */

// Current Version
val V = Version(2, 3, 0)

/**
 * Size related constants
 */
const val PANE_WIDTH  = 600.0
const val PANE_HEIGHT = 400.0
const val COMMON_GAP  = 16.0

/**
 * Scale
 */
const val SCALE_MIN  = 0.1
const val SCALE_MAX  = 4.0
const val SCALE_INIT = 0.8

/**
 * CLabel Radius Range
 */
const val LABEL_RADIUS_MIN = 8.0
const val LABEL_RADIUS_MAX = 48.0

/**
 * TextArea Font Size Range
 */
const val FONT_SIZE_MIN = 12
const val FONT_SIZE_MAX = 64

/**
 * TreeItem Graphic radius
 */
const val GRAPHICS_CIRCLE_RADIUS = 8.0

const val GENERAL_ICON_RADIUS = 32.0

const val FILENAME_DEFAULT   = "Nova traduko" // It's Esperanto!
const val FOLDER_NAME_BAK    = "backup"
const val EXTENSION_PIC_BMP  = "bmp"
const val EXTENSION_PIC_GIF  = "gif"
const val EXTENSION_PIC_PNG  = "png"
const val EXTENSION_PIC_JPG  = "jpg"
const val EXTENSION_PIC_JPEG = "jpeg"
const val EXTENSION_PIC_TIF  = "tif"
const val EXTENSION_PIC_TIFF = "tiff"
const val EXTENSION_FILE_LP  = "txt"
const val EXTENSION_FILE_MEO = "json"
const val EXTENSION_PACK     = "zip"
const val EXTENSION_BAK      = "bak"

/**
 * Extensions: png, jpg, jpeg
 */
val EXTENSIONS_PIC  = listOf(
    EXTENSION_PIC_BMP,
    EXTENSION_PIC_GIF,
    EXTENSION_PIC_PNG,
    EXTENSION_PIC_JPG,
    EXTENSION_PIC_JPEG,
    EXTENSION_PIC_TIF,
    EXTENSION_PIC_TIFF
)
val EXTENSIONS_FILE = listOf(EXTENSION_FILE_LP, EXTENSION_FILE_MEO)

/**
 * For label/group not found
 */
const val NOT_FOUND = -1

/**
 * Log Source
 */
const val LOGSRC_APPLICATION = "Application"
const val LOGSRC_CONTROLLER  = "Controller"
const val LOGSRC_STATE       = "State"
const val LOGSRC_ACTION      = "Action"
const val LOGSRC_OPTIONS     = "Options"
const val LOGSRC_DIALOGS     = "Dialog"
const val LOGSRC_LOGGER      = "Logger"
const val LOGSRC_SENDER      = "Sender"
const val LOGSRC_DICTIONARY  = "Dictionary"

/**
 * Work Mode
 * @param description Display name for WorkMode
 */
enum class WorkMode(private val description: String) {
    InputMode(I18N["mode.work.input"]),
    LabelMode(I18N["mode.work.label"]);

    override fun toString(): String = description

}

/**
 * Label View Mode
 * @param description Display name for ViewMode
 */
enum class ViewMode(private val description: String) {
    IndexMode(I18N["mode.view.index"]),
    GroupMode(I18N["mode.view.group"]);

    override fun toString(): String = description

}

/**
 * Translation File Type
 */
enum class FileType(private val description: String, val extension: String) {
    LPFile(I18N["file_type.translation_lp"], EXTENSION_FILE_LP),
    MeoFile(I18N["file_type.translation_meo"], EXTENSION_FILE_MEO);

    override fun toString(): String = description

    companion object {
        /**
         * NOTE: It's not safe to determine a file's type by its extension
         */
        fun getFileType(file: File): FileType = when (file.extension) {
            EXTENSION_FILE_MEO -> MeoFile
            EXTENSION_FILE_LP -> LPFile
            else -> throw IllegalArgumentException(String.format(I18N["exception.file_type.invalid_file_extension.s"], file.extension))
        }
    }
}

/**
 * Get a TextFormatter for Text should have '|' ' ' ','
 */
fun genGeneralFormatter(): TextFormatter<String> = genTextFormatter {
    it.text.trim().replace(Regex("[|, ]"), "_")
}
