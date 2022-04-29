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
val V: Version = Version(2, 3, 0)

/**
 * Size related constants
 */
const val PANE_WIDTH : Double = 600.0
const val PANE_HEIGHT: Double = 400.0
const val COMMON_GAP : Double = 16.0

/**
 * Scale
 */
const val SCALE_MIN : Double = 0.1
const val SCALE_MAX : Double = 4.0
const val SCALE_INIT: Double = 0.8

/**
 * CLabel Radius Range
 */
const val LABEL_RADIUS_MIN: Double= 8.0
const val LABEL_RADIUS_MAX: Double = 48.0

/**
 * TextArea Font Size Range
 */
const val FONT_SIZE_MIN: Int = 12
const val FONT_SIZE_MAX: Int = 64

/**
 * TreeItem Graphic radius
 */
const val GRAPHICS_CIRCLE_RADIUS: Double = 8.0

const val GENERAL_ICON_RADIUS: Double = 32.0

const val FILENAME_DEFAULT  : String = "Nova traduko" // It's Esperanto!
const val FOLDER_NAME_BAK   : String = "backup"
const val EXTENSION_PIC_BMP : String = "bmp"
const val EXTENSION_PIC_GIF : String = "gif"
const val EXTENSION_PIC_PNG : String = "png"
const val EXTENSION_PIC_JPG : String = "jpg"
const val EXTENSION_PIC_JPEG: String = "jpeg"
const val EXTENSION_PIC_TIF : String = "tif"
const val EXTENSION_PIC_TIFF: String = "tiff"
const val EXTENSION_PIC_WEBP: String = "webp"
const val EXTENSION_FILE_LP : String = "txt"
const val EXTENSION_FILE_MEO: String = "json"
const val EXTENSION_PACK    : String = "zip"
const val EXTENSION_BAK     : String = "bak"

/**
 * Extensions: png, jpg, jpeg
 */
val EXTENSIONS_PIC: List<String> = listOf(
    EXTENSION_PIC_BMP,
    EXTENSION_PIC_GIF,
    EXTENSION_PIC_PNG,
    EXTENSION_PIC_JPG,
    EXTENSION_PIC_JPEG,
    EXTENSION_PIC_TIF,
    EXTENSION_PIC_TIFF,
    EXTENSION_PIC_WEBP,
)
val EXTENSIONS_FILE: List<String> = listOf(
    EXTENSION_FILE_LP,
    EXTENSION_FILE_MEO
)

/**
 * For label/group not found
 */
const val NOT_FOUND: Int = -1

/**
 * Log Source
 */
const val LOG_SRC_APPLICATION: String = "Application"
const val LOG_SRC_CONTROLLER : String = "Controller"
const val LOG_SRC_ACTION     : String = "Action"
const val LOG_SRC_STATE      : String = "State"
const val LOG_SRC_OTHER      : String = "Other"

/**
 * Work Mode
 * @param description Display name for WorkMode
 */
enum class WorkMode(val description: String) {
    InputMode(I18N["mode.work.input"]),
    LabelMode(I18N["mode.work.label"]);

    override fun toString(): String = description

}

/**
 * Label View Mode
 * @param description Display name for ViewMode
 */
enum class ViewMode(val description: String) {
    IndexMode(I18N["mode.view.index"]),
    GroupMode(I18N["mode.view.group"]);

    override fun toString(): String = description

}

/**
 * Translation File Type
 */
enum class FileType(val description: String, val extension: String) {
    LPFile(I18N["file_type.translation_lp"], EXTENSION_FILE_LP),
    MeoFile(I18N["file_type.translation_meo"], EXTENSION_FILE_MEO);

    override fun toString(): String = description

    companion object {
        /**
         * NOTE: It's not safe to determine a file's type by its extension
         * All files with unknown extension will be treat as MeoFile
         */
        fun getFileType(file: File): FileType = when (file.extension) {
            EXTENSION_FILE_MEO -> MeoFile
            EXTENSION_FILE_LP -> LPFile
            EXTENSION_BAK -> MeoFile
            else -> MeoFile
        }
    }
}

/**
 * Get a TextFormatter for Text should have '|' ' ' ','
 */
fun genGeneralFormatter(): TextFormatter<String> = genTextFormatter {
    it.text.trim().replace(Regex("[|, ]"), "_")
}
