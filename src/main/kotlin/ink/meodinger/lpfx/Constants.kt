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

/**
 * Current Version
 */
val V: Version = Version(2, 3, 3)

/**
 * Default Filename Placeholder. It's Esperanto!
 */
const val FILENAME_DEFAULT  : String = "Nova traduko"

/**
 * Backup file folder name
 */
const val FOLDER_NAME_BAK   : String = "backup"

/**
 * Extension: bmp
 */
const val EXTENSION_PIC_BMP : String = "bmp"
/**
 * Extension: gif
 */
const val EXTENSION_PIC_GIF : String = "gif"
/**
 * Extension: png
 */
const val EXTENSION_PIC_PNG : String = "png"
/**
 * Extension: jpg
 */
const val EXTENSION_PIC_JPG : String = "jpg"
/**
 * Extension: jpeg
 */
const val EXTENSION_PIC_JPEG: String = "jpeg"
/**
 * Extension: tif
 */
const val EXTENSION_PIC_TIF : String = "tif"
/**
 * Extension: tiff
 */
const val EXTENSION_PIC_TIFF: String = "tiff"
/**
 * Extension: webp
 */
const val EXTENSION_PIC_WEBP: String = "webp"
/**
 * Extension: txt
 */
const val EXTENSION_FILE_LP : String = "txt"
/**
 * Extension: json
 */
const val EXTENSION_FILE_MEO: String = "json"
/**
 * Extension: zip
 */
const val EXTENSION_PACK    : String = "zip"
/**
 * Extension: bak
 */
const val EXTENSION_BAK     : String = "bak"

/**
 * All extensions of pictures
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

/**
 * All extensions of translation files
 */
val EXTENSIONS_FILE: List<String> = listOf(
    EXTENSION_FILE_LP,
    EXTENSION_FILE_MEO
)

/**
 * General Int value for something not found
 */
const val NOT_FOUND: Int = -1

/**
 * Work Mode
 * @param description Display name for WorkMode
 */
enum class WorkMode(val description: String) {

    /**
     * A WorkMode that specified for inputing translation of labels
     */
    InputMode(I18N["mode.work.input"]),

    /**
     * A WorkMode that specified for creating labels
     */
    LabelMode(I18N["mode.work.label"]);

    override fun toString(): String = description

}

/**
 * Label View Mode
 * @param description Display name for ViewMode
 */
enum class ViewMode(val description: String) {

    /**
     * A ViewMode that displays labels by their indices
     */
    IndexMode(I18N["mode.view.index"]),

    /**
     * A ViewMode that displays labels group by their gourpId
     */
    GroupMode(I18N["mode.view.group"]);

    override fun toString(): String = description

}

/**
 * Translation File Type
 * @param description The description for the file type
 * @param extension The extension of the file type
 */
enum class FileType(val description: String, val extension: String) {
    /**
     * Legacy LP file format
     */
    LPFile(I18N["file_type.translation_lp"], EXTENSION_FILE_LP),

    /**
     * JSON Meo file format
     */
    MeoFile(I18N["file_type.translation_meo"], EXTENSION_FILE_MEO);

    override fun toString(): String = description

    companion object {
        /**
         * NOTE: It's not safe to determine a file's type by its extension
         * All files with unknown extension will be treat as MeoFile
         */
        fun getFileType(file: File): FileType = when (file.extension.lowercase()) {
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
