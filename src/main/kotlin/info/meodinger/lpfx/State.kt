package info.meodinger.lpfx

import info.meodinger.lpfx.type.TransFile
import info.meodinger.lpfx.util.color.toHex
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.INFO
import info.meodinger.lpfx.util.resource.get

import javafx.application.Application
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.io.File

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx
 */
object State {

    lateinit var application: Application
    lateinit var stage: Stage

    var controllerAccessor: ControllerAccessor? = null
        set(value) {
            if (field == null) field = value
            else throw IllegalStateException(I18N["exception.illegal_state.accessor_already_set"])
        }
    val accessor: ControllerAccessor
        get() = controllerAccessor!!

    var isOpened = false
    var transFile = TransFile()
    var transPath = ""
    var currentGroupId: Int = 0
    var currentPicName: String = ""

    var isChanged = false
    var workMode = DefaultWorkMode
    var viewMode = DefaultViewMode

    fun reset() {
        isOpened = false
        transFile = TransFile()
        transPath = ""
        currentGroupId = 0
        currentPicName = ""
        isChanged = false
        workMode = DefaultWorkMode
        viewMode = DefaultViewMode

        stage.title = INFO["application.name"]
        accessor.reset()
    }

    fun getGroupIdByName(name: String): Int {
        val size = transFile.groupList.size
        for (i in 0 until size) {
            if (transFile.groupList[i].name == name) {
                return i
            }
        }
        return -1
    }

    fun getGroupColorByName(name: String): String {
        for (group in transFile.groupList) {
            if (group.name == name) return group.color
        }
        return Color.WHITE.toHex()
    }

    fun getFileFolder(): String = File(transPath).parent
    fun getBakFolder(): String = getFileFolder() + File.separator + FOLDER_NAME_BAK
    fun getPicPathOf(picName: String): String = getFileFolder() + File.separator + picName
    fun getPicPathNow(): String = getPicPathOf(currentPicName)

    override fun toString(): String {
        return """State{
          |transPath=${transPath}
          |currentGroupId=${currentGroupId}
          |currentPicName=${currentPicName}
          |isChanged=${isChanged}
          |workMode=${workMode}
          |viewMode=${viewMode}
        """.trimIndent()
    }

    interface ControllerAccessor {
        fun close()
        fun reset()

        fun addLabelLayer()
        fun removeLabelLayer(groupId: Int)

        fun updateTree()
        fun updateGroupList()

        operator fun get(fieldName: String): Any
    }
}