package ink.meodinger.lpfx

import ink.meodinger.lpfx.action.Action
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.util.collection.ArrayStack
import ink.meodinger.lpfx.util.collection.Stack
import ink.meodinger.lpfx.util.assignOnce
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
 * Modal & Manager for LPFX.
 * MUST Initialize some values in order: App -> Stage -> View -> Controller.
 */
class State {

    // ----- Global Variables ------ //

    var application: LabelPlusFX by assignOnce()
    var stage: Stage by assignOnce()
    var view: View by assignOnce()
    var controller: Controller by assignOnce()

    // ----- Runtime Properties ----- //

    private val openedProperty: BooleanProperty = SimpleBooleanProperty(false)
    fun openedProperty(): BooleanProperty = openedProperty
    /**
     * Whether opened a TransFile or not
     */
    var isOpened: Boolean by openedProperty

    private val changedProperty: BooleanProperty = SimpleBooleanProperty(false)
    fun changedProperty(): BooleanProperty = changedProperty
    /**
     * Whether changed a TransFile or not
     */
    var isChanged: Boolean by changedProperty

    private val transFileProperty: ObjectProperty<TransFile> = SimpleObjectProperty(null)
    fun transFileProperty(): ObjectProperty<TransFile> = transFileProperty
    /**
     * The opened TransFile
     */
    var transFile: TransFile by transFileProperty

    private val translationFileProperty: ObjectProperty<File> = SimpleObjectProperty(null)
    fun translationFileProperty(): ObjectProperty<File> = translationFileProperty
    /**
     * The FileSystem file of the opened TransFile
     */
    var translationFile: File by translationFileProperty

    private val currentGroupIdProperty: IntegerProperty = SimpleIntegerProperty(NOT_FOUND)
    fun currentGroupIdProperty(): IntegerProperty = currentGroupIdProperty
    /**
     * Index of current selected TransGroup
     */
    var currentGroupId: Int by currentGroupIdProperty

    private val currentPicNameProperty: StringProperty = SimpleStringProperty(emptyString())
    fun currentPicNameProperty(): StringProperty = currentPicNameProperty
    /**
     * Name of current selected picture (usually also picture's FileSystem file's name)
     */
    var currentPicName: String by currentPicNameProperty

    private val currentLabelIndexProperty: IntegerProperty = SimpleIntegerProperty(NOT_FOUND)
    fun currentLabelIndexProperty(): IntegerProperty = currentLabelIndexProperty
    /**
     * Index of current selected TransLabel
     */
    var currentLabelIndex: Int by currentLabelIndexProperty

    private val workModeProperty: ObjectProperty<WorkMode> = SimpleObjectProperty(WorkMode.InputMode)
    fun workModeProperty(): ObjectProperty<WorkMode> = workModeProperty
    /**
     * Current work mode
     */
    var workMode: WorkMode by workModeProperty

    private val viewModeProperty: ObjectProperty<ViewMode> = SimpleObjectProperty(ViewMode.IndexMode)
    fun viewModeProperty(): ObjectProperty<ViewMode> = viewModeProperty
    /**
     * Current view mode
     */
    var viewMode: ViewMode by viewModeProperty

    fun reset() {
        if (!isOpened) return

        controller.reset()

        undoStack.empty()
        redoStack.empty()

        openedProperty.set(false)
        changedProperty.set(false)
        workModeProperty.set(WorkMode.InputMode)
        viewModeProperty.set(Settings.viewModes[workMode.ordinal])
        currentGroupIdProperty.set(NOT_FOUND)
        currentPicNameProperty.set(emptyString())
        currentLabelIndexProperty.set(NOT_FOUND)
        transFileProperty.set(null)
        translationFileProperty.set(null)

        Logger.info("Reset", LOGSRC_STATE)
    }

    // ----- File-Related Methods ----- //

    /**
     * Get current picture's FileSystem file
     * @return null if not opened of currentPicName isEmpty, else `TransFile::getFile`
     */
    fun getPicFileNow(): File? {
        return if (isOpened && currentPicName.isNotEmpty()) transFile.getFile(currentPicName) else null
    }

    /**
     * Get current TransFile's FileSystem file's directory
     */
    fun getFileFolder(): File? {
        return if (isOpened) translationFile.parentFile else null
    }

    /**
     * Get current TransFile's FileSystem file's backup directory
     */
    fun getBakFolder(): File? {
        return if (isOpened) translationFile.parentFile.resolve(FOLDER_NAME_BAK) else null
    }

    // ----- UNDO/REDO STACK ----- //

    private val undoStack: Stack<Action> = ArrayStack()
    private val redoStack: Stack<Action> = ArrayStack()

    private val canUndoProperty: BooleanProperty = SimpleBooleanProperty(false)
    fun canUndoProperty(): ReadOnlyBooleanProperty = canUndoProperty
    val canUndo: Boolean by canUndoProperty

    private val canRedoProperty: BooleanProperty = SimpleBooleanProperty(false)
    fun canRedoProperty(): ReadOnlyBooleanProperty = canRedoProperty
    val canRedo: Boolean by canRedoProperty

    fun doAction(action: Action) {
        undoStack.push(action.apply(Action::commit))
        redoStack.empty()

        isChanged = true
        canUndoProperty.set(true)
        canRedoProperty.set(false)
    }

    fun undo() {
        if (undoStack.isEmpty()) return

        redoStack.push(undoStack.pop().apply(Action::revert))

        canUndoProperty.set(!undoStack.isEmpty())
        canRedoProperty.set(true)
    }

    fun redo() {
        if (redoStack.isEmpty()) return

        undoStack.push(redoStack.pop().apply(Action::commit))

        canUndoProperty.set(true)
        canRedoProperty.set(!redoStack.isEmpty())
    }

}
