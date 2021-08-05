package info.meodinger.lpfx

/**
 * Author: Meodinger
 * Date: 2021/8/1
 * Location: info.meodinger.lpfx
 */
const val WIDTH = 900.0
const val HEIGHT = 600.0

enum class WorkMode { LabelMode, InputMode }
val DefaultWorkMode = WorkMode.InputMode

enum class ViewMode { IndexMode, GroupMode }
val DefaultViewMode = ViewMode.GroupMode

val EXTENSIONS_PIC = listOf(".png", ".jpg", ".jpeg")
const val EXTENSION_MEO = ".json"
const val EXTENSION_LP = ".txt"
const val EXTENSION_PACK = ".zip"
const val EXTENSION_BAK = ".bak"
const val FOLDER_NAME_BAK = "bak"

const val AUTO_SAVE_DELAY = 5 * 60 * 1000
const val AUTO_SAVE_PERIOD = 3 * 60 * 1000