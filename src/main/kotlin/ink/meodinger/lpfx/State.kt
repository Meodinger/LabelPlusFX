package ink.meodinger.lpfx

import ink.meodinger.lpfx.action.Action
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.util.collection.ArrayStack
import ink.meodinger.lpfx.util.collection.Stack
import ink.meodinger.lpfx.util.once
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

    // region LPFX references

    /**
     * Reference to the LPFX Application. Available after launched
     */
    var application: LabelPlusFX by once()

    /**
     * Reference to the Controller. Available after started
     */
    var controller: Controller by once()

    /**
     * Reference to the Primary Stage. Available after started
     */
    var stage: Stage by once()

    /**
     * Reference to the View. Available after started
     */
    var view: View by once()

    // endregion

    // region Workspace Properties

    private val openedProperty: BooleanProperty = SimpleBooleanProperty(false)
    /**
     * Whether opened a TransFile or not
     */
    fun openedProperty(): BooleanProperty = openedProperty
    /**
     * @see openedProperty
     */
    var isOpened: Boolean by openedProperty

    private val changedProperty: BooleanProperty = SimpleBooleanProperty(false)
    /**
     * Whether changed a TransFile or not
     */
    fun changedProperty(): BooleanProperty = changedProperty
    /**
     * @see changedProperty
     */
    var isChanged: Boolean by changedProperty

    private val transFileProperty: ObjectProperty<TransFile> = SimpleObjectProperty(null)
    /**
     * The opened TransFile
     */
    fun transFileProperty(): ObjectProperty<TransFile> = transFileProperty
    /**
     * @see transFileProperty
     */
    var transFile: TransFile by transFileProperty

    private val translationFileProperty: ObjectProperty<File> = SimpleObjectProperty(null)
    /**
     * The FileSystem file of the opened TransFile
     */
    fun translationFileProperty(): ObjectProperty<File> = translationFileProperty
    /**
     * @see translationFileProperty
     */
    var translationFile: File by translationFileProperty

    private val currentGroupIdProperty: IntegerProperty = SimpleIntegerProperty(NOT_FOUND)
    /**
     * Index of current selected TransGroup
     */
    fun currentGroupIdProperty(): IntegerProperty = currentGroupIdProperty
    /**
     * @see currentGroupIdProperty
     */
    var currentGroupId: Int by currentGroupIdProperty

    private val currentPicNameProperty: StringProperty = SimpleStringProperty(emptyString())
    /**
     * Name of current selected picture (usually also picture's FileSystem file's name)
     */
    fun currentPicNameProperty(): StringProperty = currentPicNameProperty
    /**
     * @see currentPicNameProperty
     */
    var currentPicName: String by currentPicNameProperty

    private val currentLabelIndexProperty: IntegerProperty = SimpleIntegerProperty(NOT_FOUND)
    /**
     * Index of current selected TransLabel
     */
    fun currentLabelIndexProperty(): IntegerProperty = currentLabelIndexProperty
    /**
     * @see currentLabelIndexProperty
     */
    var currentLabelIndex: Int by currentLabelIndexProperty

    private val workModeProperty: ObjectProperty<WorkMode> = SimpleObjectProperty(WorkMode.InputMode)
    /**
     * Current work mode
     */
    fun workModeProperty(): ObjectProperty<WorkMode> = workModeProperty
    /**
     * @see workModeProperty
     */
    var workMode: WorkMode by workModeProperty

    private val viewModeProperty: ObjectProperty<ViewMode> = SimpleObjectProperty(ViewMode.IndexMode)
    /**
     * Current view mode
     */
    fun viewModeProperty(): ObjectProperty<ViewMode> = viewModeProperty
    /**
     * @see viewModeProperty
     */
    var viewMode: ViewMode by viewModeProperty

    /**
     * Get current picture's FileSystem file
     */
    fun getPicFileNow(): File {
        return transFile.getFile(currentPicName)
    }

    /**
     * Get current TransFile's FileSystem file's directory
     */
    fun getFileFolder(): File {
        return translationFile.parentFile
    }

    /**
     * Get current TransFile's FileSystem file's backup directory
     */
    fun getBakFolder(): File {
        return translationFile.parentFile.resolve(FOLDER_NAME_BAK)
    }

    // endregion

    // region Undo/Redo Stack

    private val undoStack: Stack<Action> = ArrayStack()
    private val redoStack: Stack<Action> = ArrayStack()

    private val canUndoProperty: BooleanProperty = SimpleBooleanProperty(false)
    /**
     * Whether undo is available
     */
    fun undoableProperty(): ReadOnlyBooleanProperty = canUndoProperty
    /**
     * @see undoableProperty
     */
    val isUndoable: Boolean by canUndoProperty

    private val redoableProperty: BooleanProperty = SimpleBooleanProperty(false)
    /**
     * Whether redo is available
     */
    fun canRedoProperty(): ReadOnlyBooleanProperty = redoableProperty
    /**
     * @see redoableProperty
     */
    val isRedoable: Boolean by redoableProperty

    /**
     * Do an action
     */
    fun doAction(action: Action) {
        undoStack.push(action.apply(Action::commit))
        redoStack.empty()
        Logger.info("Action committed", "State")

        isChanged = true
        canUndoProperty.set(true)
        redoableProperty.set(false)
    }

    /**
     * Revert the last action if [isUndoable]
     */
    fun undo() {
        if (!isUndoable) return

        redoStack.push(undoStack.pop().apply(Action::revert))
        Logger.info("Action reverted", "State")

        canUndoProperty.set(!undoStack.isEmpty())
        redoableProperty.set(true)
    }

    /**
     * Redo (re-commit) the last reverted action if [isRedoable]
     */
    fun redo() {
        if (!isRedoable) return

        undoStack.push(redoStack.pop().apply(Action::commit))
        Logger.info("Action re-committed", "State")

        canUndoProperty.set(true)
        redoableProperty.set(!redoStack.isEmpty())
    }

    // endregion

    /**
     * Reset the entire worksapce, be ready to open another translation.
     */
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

        Logger.info("Reset", "State")
    }

}
