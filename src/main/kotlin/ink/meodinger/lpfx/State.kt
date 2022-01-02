package ink.meodinger.lpfx

import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.HookedApplication
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

    val DEFAULT_WORK_MODE = WorkMode.InputMode
    val DEFAULT_VIEW_MODE = ViewMode.IndexMode

    lateinit var application: HookedApplication
    lateinit var controller: Controller
    lateinit var stage: Stage

    val isOpenedProperty = SimpleBooleanProperty(false)
    val isChangedProperty = SimpleBooleanProperty(false)
    val transFileProperty = SimpleObjectProperty(TransFile.DEFAULT_TRANSFILE)
    val translationFileProperty = SimpleObjectProperty(DEFAULT_FILE)
    val projectFolderProperty = SimpleObjectProperty(DEFAULT_FILE)
    val currentPicNameProperty = SimpleStringProperty("")
    val currentGroupIdProperty = SimpleIntegerProperty(0)
    val currentLabelIndexProperty = SimpleIntegerProperty(NOT_FOUND)
    val viewModeProperty = SimpleObjectProperty(DEFAULT_VIEW_MODE)
    val workModeProperty = SimpleObjectProperty(DEFAULT_WORK_MODE)

    /**
     * Whether opened a TransFile or not
     */
    var isOpened: Boolean by isOpenedProperty
    /**
     * Whether changed a TransFile or not
     */
    var isChanged: Boolean by isChangedProperty
    /**
     * TransFile opened
     */
    var transFile: TransFile by transFileProperty
    /**
     * TransFile's FileSystem file
     */
    var translationFile: File by translationFileProperty
    /**
     * Folder of all project pictures (no external pictures)
     */
    var projectFolder: File by projectFolderProperty
    /**
     * Name of current selected picture (usually also picture's FileSystem file's name)
     */
    var currentPicName: String by currentPicNameProperty
    /**
     * Index of current selected TransGroup
     */
    var currentGroupId: Int by currentGroupIdProperty
    /**
     * Index of current selected TransLabel
     */
    var currentLabelIndex: Int by currentLabelIndexProperty
    /**
     * Current view mode
     */
    var viewMode: ViewMode by viewModeProperty
    /**
     * Current work mode
     */
    var workMode: WorkMode by workModeProperty

    fun reset() {
        controller.reset()

        isOpened = false
        isChanged = false
        transFile = TransFile.DEFAULT_TRANSFILE
        translationFile = DEFAULT_FILE
        projectFolder = DEFAULT_FILE
        currentPicName = ""
        currentGroupId = 0
        currentLabelIndex = NOT_FOUND
        viewMode = ViewMode.getViewMode(Settings[Settings.ViewModePreference].asStringList()[0])
        workMode = WorkMode.InputMode

        Logger.info("Reset", LOGSRC_STATE)
    }

    fun setComment(comment: String) {
        transFile.comment = comment

        Logger.info("Set comment @ ${comment.replace("\n", " ")}", LOGSRC_STATE)
    }

    fun addTransGroup(transGroup: TransGroup) {
        transFile.addTransGroup(transGroup)

        Logger.info("Added $transGroup", LOGSRC_STATE)
    }
    fun removeTransGroup(groupName: String) {
        val toRemove = transFile.getTransGroup(groupName)

        transFile.removeTransGroup(groupName)

        Logger.info("Removed $toRemove", LOGSRC_STATE)
    }
    fun setTransGroupName(groupId: Int, name: String) {
        transFile.getTransGroup(groupId).name = name

        Logger.info("Set GroupID=$groupId @name=$name", LOGSRC_STATE)
    }
    fun setTransGroupColor(groupId: Int, color: String) {
        transFile.getTransGroup(groupId).colorHex = color

        Logger.info("Set GroupID=$groupId @color=$color", LOGSRC_STATE)
    }

    fun addPicture(picName: String, picFile: File? = null) {
        val file = picFile ?: projectFolder.resolve(picName)

        transFile.addTransList(picName)
        transFile.addFile(picName, file)

        Logger.info("Added picture $picName with path ${file.path}", LOGSRC_STATE)
    }
    fun removePicture(picName: String) {
        transFile.removeTransList(picName)
        transFile.removeFile(picName)

        Logger.info("Removed picture $picName", LOGSRC_STATE)
    }

    fun addTransLabel(picName: String, transLabel: TransLabel) {
        transFile.addTransLabel(picName, transLabel)

        Logger.info("Added $picName @ $transLabel", LOGSRC_STATE)
    }
    fun removeTransLabel(picName: String, labelIndex: Int) {
        val toRemove = transFile.getTransLabel(picName, labelIndex)

        transFile.removeTransLabel(picName, labelIndex)

        Logger.info("Removed $picName @ $toRemove", LOGSRC_STATE)
    }
    fun setTransLabelIndex(picName: String, index: Int, newIndex: Int) {
        transFile.getTransLabel(picName, index).index = newIndex

        Logger.info("Set $picName->Index=$index @index=$newIndex", LOGSRC_STATE)
    }
    fun setTransLabelGroup(picName: String, index: Int, groupId: Int) {
        transFile.getTransLabel(picName, index).groupId = groupId

        Logger.info("Set $picName->Index=$index @groupId=$groupId", LOGSRC_STATE)
    }

    /**
     * Get current picture's FileSystem file
     */
    fun getPicFileNow(): File {
        if (!isOpened || currentPicName == "") return DEFAULT_FILE
        return transFile.getFile(currentPicName)
    }

    /**
     * Get current TransFile's FileSystem file's directory
     */
    fun getFileFolder(): File = translationFile.parentFile

    /**
     * Get current TransFile's FileSystem file's backup directory
     */
    fun getBakFolder(): File = translationFile.parentFile.resolve(FOLDER_NAME_BAK)

}
