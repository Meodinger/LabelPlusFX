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

    var controllerAccessor: ControllerAccessor? = null
        set(value) {
            if (field != null) field = value
            else throw IllegalStateException(I18N["exception.accessor_already_set"])
        }

    lateinit var application: Application
    lateinit var stage: Stage

    var transFile: TransFile? = null
    var transPath: String? = null
    var currentGroupId: Int = 0
    var currentPicName: String = ""

    var isChanged = false
    var workMode = DefaultWorkMode
    var viewMode = DefaultViewMode

    fun reset() {
        transFile = null
        transPath = null
        currentGroupId = 0
        currentPicName = ""
        isChanged = false
        workMode = DefaultWorkMode
        viewMode = DefaultViewMode

        stage.title = INFO["application.name"]
        controllerAccessor!!.reset()
    }

    fun getGroupIdByName(name: String): Int {
        val size = transFile!!.groupList.size
        for (i in 0 until size) {
            if (transFile!!.groupList[i].name == name) {
                return i
            }
        }
        return -1
    }

    fun getGroupColorByName(name: String): String {
        for (group in transFile!!.groupList) {
            if (group.name == name) return group.color
        }
        return Color.WHITE.toHex()
    }

    fun getFileFolder(): String = File(transPath!!).parent
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
        fun updateLabelLayer(index: Int)
        fun removeLabelLayer(index: Int)
        fun updateLabelLayers()

        fun updateTree()
        fun updateGroupList()

        operator fun get(field: String): Any
    }
}