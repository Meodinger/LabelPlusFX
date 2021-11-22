package ink.meodinger.lpfx

import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.stage.Stage
import java.io.File


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * Modal & Manager for LPFX
 */
object State {

    lateinit var application: HookedApplication
    lateinit var controller: Controller
    lateinit var stage: Stage

    val isOpenedProperty = SimpleBooleanProperty(false)
    val isChangedProperty = SimpleBooleanProperty(false)
    val transFileProperty = SimpleObjectProperty(TransFile.DEFAULT_FILE)
    val translationFileProperty = SimpleObjectProperty(File(""))
    val currentPicNameProperty = SimpleStringProperty("")
    val currentGroupIdProperty = SimpleIntegerProperty(0)
    val currentLabelIndexProperty = SimpleIntegerProperty(NOT_FOUND)
    val viewModeProperty = SimpleObjectProperty(DEFAULT_VIEW_MODE)
    val workModeProperty = SimpleObjectProperty(DEFAULT_WORK_MODE)

    var isOpened: Boolean by isOpenedProperty
    var isChanged: Boolean by isChangedProperty
    var transFile: TransFile by transFileProperty
    var translationFile: File by translationFileProperty
    var currentPicName: String by currentPicNameProperty
    var currentGroupId: Int by currentGroupIdProperty
    var currentLabelIndex: Int by currentLabelIndexProperty
    var viewMode: ViewMode by viewModeProperty
    var workMode: WorkMode by workModeProperty

    fun reset() {
        controller.reset()

        isOpened = false
        transFile = TransFile.DEFAULT_FILE
        translationFile = File("")
        currentPicName = ""
        currentGroupId = 0
        currentLabelIndex = NOT_FOUND
        isChanged = false
        workMode = WorkMode.InputMode
        viewMode = ViewMode.getMode(Settings[Settings.ViewModePreference].asStringList()[0])

        Logger.info("Reset")
    }

    fun setComment(comment: String) {
        transFile.comment = comment

        Logger.info("Set comment @ ${comment.replace("\n", " ")}")
    }

    fun addTransGroup(transGroup: TransGroup) {
        transFile.addTransGroup(transGroup)

        Logger.info("Added $transGroup", "State")
    }
    fun removeTransGroup(groupName: String) {
        val toRemove = transFile.getTransGroup(groupName)

        transFile.removeTransGroup(groupName)

        Logger.info("Removed $toRemove", "State")
    }
    fun setTransGroupName(groupId: Int, name: String) {
        transFile.getTransGroup(groupId).name = name

        Logger.info("Set GroupID=$groupId @name=$name", "State")
    }
    fun setTransGroupColor(groupId: Int, color: String) {
        transFile.getTransGroup(groupId).colorHex = color

        Logger.info("Set GroupID=$groupId @color=$color", "State")
    }

    fun addPicture(picName: String, picFile: File? = null) {
        transFile.addTransList(picName)
        transFile.addFile(picName, picFile ?: getFileFolder().resolve(picName))

        Logger.info("Added picture $picName", "State")
    }
    fun removePicture(picName: String) {
        transFile.removeTransList(picName)
        transFile.removeFile(picName)

        Logger.info("Removed picture $picName", "State")
    }

    fun addTransLabel(picName: String, transLabel: TransLabel) {
        transFile.addTransLabel(picName, transLabel)

        Logger.info("Added $picName @ $transLabel", "State")
    }
    fun removeTransLabel(picName: String, labelIndex: Int) {
        val toRemove = transFile.getTransLabel(picName, labelIndex)

        transFile.removeTransLabel(picName, labelIndex)

        Logger.info("Removed $picName @ $toRemove", "State")
    }
    fun setTransLabelIndex(picName: String, index: Int, newIndex: Int) {
        transFile.getTransLabel(picName, index).index = newIndex

        Logger.info("Set $picName->Index=$index @index=$newIndex", "State")
    }
    fun setTransLabelGroup(picName: String, index: Int, groupId: Int) {
        transFile.getTransLabel(picName, index).groupId = groupId

        Logger.info("Set $picName->Index=$index @groupId=$groupId", "State")
    }

    fun getPicFileNow(): File {
        if (!isOpened || currentPicName == "") return File("")
        return transFile.getFile(currentPicName)
    }
    fun getFileFolder(): File = translationFile.parentFile
    fun getBakFolder(): File = translationFile.parentFile.resolve(FOLDER_NAME_BAK)

}