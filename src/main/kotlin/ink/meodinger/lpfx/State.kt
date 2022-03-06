package ink.meodinger.lpfx

import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.HookedApplication
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.string.emptyString

import javafx.beans.property.*
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

    private val isOpenedProperty = SimpleBooleanProperty(false)
    fun isOpenedProperty(): BooleanProperty = isOpenedProperty
    /**
     * Whether opened a TransFile or not
     */
    var isOpened: Boolean by isOpenedProperty

    private val isChangedProperty = SimpleBooleanProperty(false)
    fun isChangedProperty(): BooleanProperty = isChangedProperty
    /**
     * Whether changed a TransFile or not
     */
    var isChanged: Boolean by isChangedProperty

    private val transFileProperty = SimpleObjectProperty(TransFile.DEFAULT_TRANS_FILE)
    fun transFileProperty(): ObjectProperty<TransFile> = transFileProperty
    /**
     * The opened TransFile
     */
    var transFile: TransFile by transFileProperty

    private val translationFileProperty = SimpleObjectProperty(DEFAULT_FILE)
    fun translationFileProperty(): ObjectProperty<File> = translationFileProperty
    /**
     * The FileSystem file of the opened TransFile
     */
    var translationFile: File by translationFileProperty

    private val projectFolderProperty = SimpleObjectProperty(DEFAULT_FILE)
    fun projectFolderProperty(): ObjectProperty<File> = projectFolderProperty
    /**
     * The folder of all project pictures (no external pictures)
     */
    var projectFolder: File by projectFolderProperty

    private val currentPicNameProperty = SimpleStringProperty(emptyString())
    fun currentPicNameProperty(): StringProperty = currentPicNameProperty
    /**
     * Name of current selected picture (usually also picture's FileSystem file's name)
     */
    var currentPicName: String by currentPicNameProperty

    private val currentGroupIdProperty = SimpleIntegerProperty(0)
    fun currentGroupIdProperty(): IntegerProperty = currentGroupIdProperty
    /**
     * Index of current selected TransGroup
     */
    var currentGroupId: Int by currentGroupIdProperty

    private val currentLabelIndexProperty = SimpleIntegerProperty(NOT_FOUND)
    fun currentLabelIndexProperty(): IntegerProperty = currentLabelIndexProperty
    /**
     * Index of current selected TransLabel
     */
    var currentLabelIndex: Int by currentLabelIndexProperty

    private val viewModeProperty = SimpleObjectProperty(ViewMode.IndexMode)
    fun viewModeProperty(): ObjectProperty<ViewMode> = viewModeProperty
    /**
     * Current view mode
     */
    var viewMode: ViewMode by viewModeProperty

    private val workModeProperty = SimpleObjectProperty(WorkMode.InputMode)
    fun workModeProperty(): ObjectProperty<WorkMode> = workModeProperty
    /**
     * Current work mode
     */
    var workMode: WorkMode by workModeProperty

    fun reset() {
        controller.reset()

        isOpened = false
        isChanged = false
        transFile = TransFile.DEFAULT_TRANS_FILE
        translationFile = DEFAULT_FILE
        projectFolder = DEFAULT_FILE
        currentPicName = emptyString()
        currentGroupId = NOT_FOUND
        currentLabelIndex = NOT_FOUND
        viewMode = Settings.viewModes[WorkMode.InputMode.ordinal]
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
        val toRemoveId = transFile.getGroupIdByName(groupName)
        val toRemove = transFile.getTransGroup(toRemoveId)

        transFile.removeTransGroup(toRemoveId)
        for (picName in transFile.picNames) for (label in transFile.getTransList(picName))
            if (label.groupId >= toRemoveId) label.groupId--

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
        transFile.setFile(picName, file)

        Logger.info("Added picture $picName with path ${file.path}", LOGSRC_STATE)
    }
    fun removePicture(picName: String) {
        transFile.removeTransList(picName)
        transFile.removeFile(picName)

        Logger.info("Removed picture $picName", LOGSRC_STATE)
    }

    fun addTransLabel(picName: String, transLabel: TransLabel) {
        val labelIndex = transLabel.index

        for (label in transFile.getTransList(picName)) if (label.index >= labelIndex) label.index++
        transFile.addTransLabel(picName, transLabel)

        Logger.info("Added $picName @ $transLabel", LOGSRC_STATE)
    }
    fun removeTransLabel(picName: String, labelIndex: Int) {
        val toRemove = transFile.getTransLabel(picName, labelIndex)

        transFile.removeTransLabel(picName, labelIndex)
        for (label in transFile.getTransList(picName)) if (label.index > labelIndex) label.index--

        Logger.info("Removed $picName @ $toRemove", LOGSRC_STATE)
    }
    fun setTransLabelGroup(picName: String, index: Int, groupId: Int) {
        transFile.getTransLabel(picName, index).groupId = groupId

        Logger.info("Set $picName @ Index=$index @groupId=$groupId", LOGSRC_STATE)
    }

    /**
     * Get current picture's FileSystem file
     */
    fun getPicFileNow(): File = if (isOpened && currentPicName.isNotEmpty()) transFile.getFileOrByProject(
        currentPicName,
        projectFolder
    ) else DEFAULT_FILE

    /**
     * Get current TransFile's FileSystem file's directory
     */
    fun getFileFolder(): File = translationFile.parentFile

    /**
     * Get current TransFile's FileSystem file's backup directory
     */
    fun getBakFolder(): File = translationFile.parentFile.resolve(FOLDER_NAME_BAK)

}
