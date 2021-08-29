package info.meodinger.lpfx

import info.meodinger.lpfx.options.Logger
import info.meodinger.lpfx.options.Settings
import info.meodinger.lpfx.type.TransFile
import info.meodinger.lpfx.type.TransGroup
import info.meodinger.lpfx.type.TransLabel

import javafx.application.Application
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.stage.Stage
import java.io.File

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx
 */
object State {

    lateinit var application: Application
    lateinit var controller: Controller
    lateinit var stage: Stage

    val isOpenedProperty = SimpleBooleanProperty(false)
    val isChangedProperty = SimpleBooleanProperty(false)
    val transFileProperty = SimpleObjectProperty(TransFile.DEFAULT_FILE)
    val transPathProperty = SimpleStringProperty("")
    val currentPicNameProperty = SimpleStringProperty("")
    val currentGroupIdProperty = SimpleIntegerProperty(0)
    val currentLabelIndexProperty = SimpleIntegerProperty(NOT_FOUND)
    val viewModeProperty = SimpleObjectProperty(ViewMode.GroupMode)
    val workModeProperty = SimpleObjectProperty(WorkMode.InputMode)

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
    var currentPicName: String
        get() = currentPicNameProperty.value
        set(value) {
            currentPicNameProperty.value = value
        }
    var currentGroupId: Int
        get() = currentGroupIdProperty.value
        set(value) {
            currentGroupIdProperty.value = value
        }
    var currentLabelIndex: Int
        get() = currentLabelIndexProperty.value
        set(value) {
            currentLabelIndexProperty.value = value
        }
    var viewMode: ViewMode
        get() = viewModeProperty.value
        set(value) {
            viewModeProperty.value = value
        }
    var workMode: WorkMode
        get() = workModeProperty.value
        set(value) {
            workModeProperty.value = value
        }

    fun reset() {
        isOpened = false
        transFile = TransFile.DEFAULT_FILE
        transPath = ""
        currentPicName = ""
        currentGroupId = 0
        currentLabelIndex = NOT_FOUND
        isChanged = false
        workMode = WorkMode.InputMode
        viewMode = getViewMode(Settings[Settings.ViewModePreference].asStringList()[0])

        controller.reset()

        Logger.info("Reset")
    }

    fun addTransGroup(transGroup: TransGroup) {
        transFile.groupList.add(transGroup)

        Logger.info("Added $transGroup", "State")
    }
    fun delTransGroup(transGroup: TransGroup) {
        transFile.groupList.remove(transGroup)

        Logger.info("Removed $transGroup", "State")
    }
    fun setTransGroupName(groupId: Int, name: String) {
        transFile.getTransGroupAt(groupId).name = name

        Logger.info("Set GroupID=$groupId @name=$name", "State")
    }
    fun setTransGroupColor(groupId: Int, color: String) {
        transFile.getTransGroupAt(groupId).color = color

        Logger.info("Set GroupID=$groupId @color=$color", "State")
    }

    fun addTransLabel(picName: String, transLabel: TransLabel) {
        transFile.getTransLabelListOf(picName).add(transLabel)

        Logger.info("Added $picName @ $transLabel", "State")
    }
    fun delTransLabel(picName: String, transLabel: TransLabel) {
        transFile.getTransLabelListOf(picName).remove(transLabel)

        Logger.info("Removed $picName @ $transLabel", "State")
    }
    fun setTransLabelIndex(picName: String, index: Int, newIndex: Int) {
        transFile.getTransLabelAt(picName, index).index = newIndex

        Logger.info("Set $picName->Index=$index @index=$newIndex", "State")
    }
    fun setTransLabelGroup(picName: String, index: Int, groupId: Int) {
        transFile.getTransLabelAt(picName, index).groupId = groupId

        Logger.info("Set $picName->Index=$index @groupId=$groupId", "State")
    }

    fun setComment(comment: String) {
        transFile.comment = comment

        Logger.info("Set comment @ ${comment.replace("\n", " ")}")
    }

    fun getFileFolder(): String = File(transPath).parent
    fun getBakFolder(): String = getFileFolder() + File.separator + FOLDER_NAME_BAK
    fun getPicPathOf(picName: String): String = getFileFolder() + File.separator + picName
    fun getPicPathNow(): String = getPicPathOf(currentPicName)
}