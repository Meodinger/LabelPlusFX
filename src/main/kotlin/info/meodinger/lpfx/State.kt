package info.meodinger.lpfx

import info.meodinger.lpfx.type.TransFile
import info.meodinger.lpfx.util.color.toHex
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get

import javafx.application.Application
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
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

    val isOpenedProperty = SimpleBooleanProperty(false)
    val isChangedProperty = SimpleBooleanProperty(false)
    val transFileProperty = SimpleObjectProperty(TransFile.DEFAULT_FILE)
    val transPathProperty = SimpleStringProperty("")
    val currentGroupIdProperty = SimpleIntegerProperty(0)
    val currentPicNameProperty = SimpleStringProperty("")

    var isOpened: Boolean
        get() = isOpenedProperty.value
        set(value) {
            isOpenedProperty.value = value
        }
    var isChanged: Boolean
        get() = isChangedProperty.value
        set(value) {
            isChangedProperty.value = value
        }
    var transFile: TransFile
        get() = transFileProperty.value
        set(value) {
            transFileProperty.value = value
        }
    var transPath: String
        get() = transPathProperty.value
        set(value) {
            transPathProperty.value = value
        }
    var currentGroupId: Int
        get() = currentGroupIdProperty.value
        set(value) {
            currentGroupIdProperty.value = value
        }
    var currentPicName: String
        get() = currentPicNameProperty.value
        set(value) {
            currentPicNameProperty.value = value
        }

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

        fun addLabelLayer()
        fun removeLabelLayer(groupId: Int)

        fun updateTree()
        fun updateGroupList()

        operator fun get(fieldName: String): Any
    }
}