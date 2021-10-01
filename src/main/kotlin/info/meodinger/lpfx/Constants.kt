package info.meodinger.lpfx

import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get


/**
 * Author: Meodinger
 * Date: 2021/8/1
 * Location: info.meodinger.lpfx
 */

// Stage
const val WIDTH = 900.0
const val HEIGHT = 600.0

// Extensions
val EXTENSIONS_PIC = listOf(".png", ".jpg", ".jpeg")
const val EXTENSION_MEO = ".json"
const val EXTENSION_LP = ".txt"
const val EXTENSION_PACK = ".zip"
const val EXTENSION_BAK = ".bak"
const val FOLDER_NAME_BAK = "bak"

// Auto-save
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
enum class ViewMode(val description: String) {
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
enum class FileType(val description: String) {
    LPFile(I18N["filetype.translation_lp"]),
    MeoFile(I18N["filetype.translation_meo"]);

    override fun toString(): String = description

    companion object {
        private fun isMeoFile(filePath: String): Boolean = filePath.endsWith(EXTENSION_MEO)
        private fun isLPFile(filePath: String): Boolean = filePath.endsWith(EXTENSION_LP)
        fun getType(path: String): FileType {
            if (isMeoFile(path)) return MeoFile
            if (isLPFile(path)) return LPFile
            throw IllegalArgumentException(I18N["exception.illegal_argument.invalid_file_extension"])
        }
    }
}