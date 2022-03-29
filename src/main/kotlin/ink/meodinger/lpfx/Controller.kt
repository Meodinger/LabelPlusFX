package ink.meodinger.lpfx

import ink.meodinger.lpfx.action.ActionType
import ink.meodinger.lpfx.action.LabelAction
import ink.meodinger.lpfx.component.*
import ink.meodinger.lpfx.component.common.*
import ink.meodinger.lpfx.io.export
import ink.meodinger.lpfx.io.load
import ink.meodinger.lpfx.io.pack
import ink.meodinger.lpfx.options.*
import ink.meodinger.lpfx.type.LPFXTask
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.Version
import ink.meodinger.lpfx.util.collection.contains
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.dialog.*
import ink.meodinger.lpfx.util.doNothing
import ink.meodinger.lpfx.util.event.*
import ink.meodinger.lpfx.util.file.existsOrNull
import ink.meodinger.lpfx.util.file.notExists
import ink.meodinger.lpfx.util.file.transfer
import ink.meodinger.lpfx.util.property.*
import ink.meodinger.lpfx.util.resource.*
import ink.meodinger.lpfx.util.string.emptyString
import ink.meodinger.lpfx.util.timer.TimerTaskManager

import com.fasterxml.jackson.databind.ObjectMapper
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.binding.ObjectBinding
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Cursor
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.input.*
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.DirectoryChooser
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection
import kotlin.math.roundToInt


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * Main controller
 */
class Controller(private val state: State) {

    companion object {
        private const val ONE_SECOND = 1000L

        /**
         * Auto-save
         */
        private const val AUTO_SAVE_DELAY = 5 * 60 * ONE_SECOND
        private const val AUTO_SAVE_PERIOD = 3 * 60 * ONE_SECOND
    }

    private val dialogSpecify: CSpecifyDialog by lazy { CSpecifyDialog(state) }

    private val view: View                       = state.view
    private val bSwitchViewMode: Button          = view.bSwitchViewMode does { switchViewMode() }
    private val bSwitchWorkMode: Button          = view.bSwitchWorkMode does { switchWorkMode() }
    private val lBackup: Label                   = view.lBackup.apply { text = I18N["stats.not_backed"] }
    private val lLocation: Label                 = view.lLocation
    private val lAccEditTime: Label              = view.lAccEditTime
    private val pMain: SplitPane                 = view.pMain
    private val pRight: SplitPane                = view.pRight
    private val cGroupBar: CGroupBar             = view.cGroupBar
    private val cLabelPane: CLabelPane           = view.cLabelPane
    private val cSlider: CTextSlider             = view.cSlider
    private val cPicBox: CComboBox<String>       = view.cPicBox
    private val cGroupBox: CComboBox<TransGroup> = view.cGroupBox
    private val cTreeView: CTreeView             = view.cTreeView
    private val cTransArea: CLigatureArea        = view.cTransArea

    private val bakTimeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT)
    private val bakFileFormatter = SimpleDateFormat("yyyy-HH-mm")
    private val backupManager = TimerTaskManager(AUTO_SAVE_DELAY, AUTO_SAVE_PERIOD) {
        if (state.isChanged) {
            val time = Date()
            val bak = state.getBakFolder()!!.resolve("${bakFileFormatter.format(time)}.$EXTENSION_BAK")
            try {
                export(bak, FileType.MeoFile, state.transFile)
                Platform.runLater {
                    lBackup.text = String.format(I18N["stats.last_backup.s"], bakTimeFormatter.format(time))
                }
                Logger.info("Backed TransFile", LOGSRC_CONTROLLER)
            } catch (e: IOException) {
                Logger.error("Auto-backup failed", LOGSRC_CONTROLLER)
                Logger.exception(e)
            }
        }
    }

    private var accumulator: Long = 16 * 60 * 60 * ONE_SECOND
    private val accumulatorFormatter = SimpleDateFormat("HH:mm:ss")
    private val accumulatorManager = TimerTaskManager(0, ONE_SECOND) {
        if (state.isOpened) {
            accumulator += ONE_SECOND
            Platform.runLater {
                lAccEditTime.text = String.format(I18N["stats.accumulator.s"], accumulatorFormatter.format(accumulator))
            }
        }
    }

    // Following Bindings should create in order to avoid unexpected Exceptions
    // And must invoke get() explicitly or by delegation every time to let the property validate
    // Or by using InvalidationListener (which needs another listener but more literal)
    private val groupsBinding: ObjectBinding<ObservableList<TransGroup>> = Bindings.createObjectBinding(
        {
            state.transFileProperty().get()?.groupListProperty
                ?: FXCollections.emptyObservableList()
        }, state.transFileProperty()
    )
    private val picNamesBinding: ObjectBinding<ObservableList<String>> = Bindings.createObjectBinding(
        {
            state.transFileProperty().get()?.sortedPicNamesObservable
                ?: FXCollections.emptyObservableList()
        }, state.transFileProperty()
    )
    private val imageBinding: ObjectBinding<Image> = Bindings.createObjectBinding(
        {
            state.getPicFileNow().existsOrNull()?.let { Image(FileInputStream(it)) }
                ?: INIT_IMAGE
        }, state.currentPicNameProperty()
    )
    private val labelsBinding: ObjectBinding<ObservableList<TransLabel>> = Bindings.createObjectBinding(
        {
            state.currentPicName.takeIf(String::isNotEmpty)?.let { state.transFile.transMapProperty[it]!! }
                ?: FXCollections.emptyObservableList()
        }, state.currentPicNameProperty()
    )

    private fun switchViewMode() {
        state.viewMode = ViewMode.values()[(state.viewMode.ordinal + 1) % ViewMode.values().size]
    }
    private fun switchWorkMode() {
        state.workMode = WorkMode.values()[(state.workMode.ordinal + 1) % WorkMode.values().size]
        state.viewMode = Settings.viewModes[state.workMode.ordinal]
    }

    init {
        state.controller = this

        Logger.info("Controller initializing...", LOGSRC_CONTROLLER)

        init()
        bind()
        listen()
        effect()
        transform()

        Logger.info("Controller initialized", LOGSRC_CONTROLLER)

        // Display default image
        cLabelPane.isVisible = false
        Platform.runLater {
            cLabelPane.moveToCenter()
            cLabelPane.isVisible = true
        }
    }

    /**
     * Components Initialize
     */
    private fun init() {
        Logger.info("Initializing components...", LOGSRC_CONTROLLER)

        // Last directory
        var lastFile = RecentFiles.lastFile
        while (lastFile != null) {
            if (lastFile.exists() && lastFile.parentFile.exists()) {
                CFileChooser.lastDirectory = lastFile.parentFile
                break
            } else {
                RecentFiles.remove(lastFile)
                lastFile = RecentFiles.lastFile
            }
        }
        Logger.info("Set CFileChooser lastDirectory: ${CFileChooser.lastDirectory}", LOGSRC_CONTROLLER)

        // Settings
        state.viewMode = Settings.viewModes[state.workMode.ordinal]
        Logger.info("Applied Settings @ ViewMode", LOGSRC_CONTROLLER)

        // Drag and Drop
        view.setOnDragOver {
            if (it.dragboard.hasFiles()) it.acceptTransferModes(TransferMode.COPY)
            it.consume() // Consume used event
        }
        view.setOnDragDropped {
            if (stay()) return@setOnDragDropped

            state.reset()

            val board = it.dragboard
            if (board.hasFiles()) {
                val file = board.files.first()

                // To make sure exception can be caught
                Platform.runLater { open(file) }
                it.isDropCompleted = true
            }
            it.consume() // Consume used event
        }
        Logger.info("Enabled Drag and Drop", LOGSRC_CONTROLLER)

        // Disable mnemonic parsing in TransArea
        cTransArea.addEventFilter(KeyEvent.ANY) {
            if (it.code == KeyCode.ALT) it.consume()
        }
        Logger.info("Registered CTransArea mnemonic parsing", LOGSRC_CONTROLLER)

        // Register Alias & Global redo/undo in TransArea
        cTransArea.addEventFilter(KeyEvent.KEY_PRESSED) {
            if (it.isControlOrMetaDown && it.code == KeyCode.Z) {
                if (!it.isShiftDown) {
                    if (!cTransArea.isUndoable) state.undo() else cTransArea.undo()
                } else {
                    if (!cTransArea.isRedoable) state.redo() else cTransArea.redo()
                }
                it.consume() // disable default undo/redo
            }
        }
        Logger.info("Registered CTransArea Alias & Global undo/redo", LOGSRC_CONTROLLER)

        // Register Ctrl/Meta + Scroll with font size change in TransArea
        cTransArea.addEventHandler(ScrollEvent.SCROLL) {
            if (!(it.isControlOrMetaDown || it.isAltDown)) return@addEventHandler

            val newSize = (cTransArea.font.size + if (it.deltaY > 0) 1 else -1).roundToInt()
                .coerceAtLeast(FONT_SIZE_MIN).coerceAtMost(FONT_SIZE_MAX)

            cTransArea.font = cTransArea.font.s(newSize.toDouble())
            cTransArea.positionCaret(0)

            it.consume()
        }
        Logger.info("Registered TransArea font size change", LOGSRC_CONTROLLER)

        // Register CGroupBar handler
        cGroupBar.setOnGroupCreate { (cTreeView.contextMenu as CTreeMenu).toggleGroupCreate() }
        Logger.info("Registered CGroupBar Add Handler", LOGSRC_CONTROLLER)

        // Register CLabelPane handler
        cLabelPane.setOnLabelCreate {
            if (state.workMode != WorkMode.LabelMode) return@setOnLabelCreate
            if (state.transFile.groupCount == 0) return@setOnLabelCreate

            val newIndex =
                if (state.currentLabelIndex != -1) state.currentLabelIndex + 1
                else state.transFile.getTransList(state.currentPicName).size + 1

            state.doAction(LabelAction(
                ActionType.ADD, state,
                state.currentPicName,
                TransLabel(newIndex, state.currentGroupId, it.labelX, it.labelY, "")
            ))
            // Update selection
            cTreeView.selectLabel(newIndex)
            // If instant translate
            if (Settings.instantTranslate) cTransArea.requestFocus()
        }
        cLabelPane.setOnLabelRemove {
            if (state.workMode != WorkMode.LabelMode) return@setOnLabelRemove

            state.doAction(LabelAction(
                ActionType.REMOVE, state,
                state.currentPicName,
                state.transFile.getTransLabel(state.currentPicName, it.labelIndex)
            ))
        }
        cLabelPane.setOnLabelHover {
            val transLabel = state.transFile.getTransLabel(state.currentPicName, it.labelIndex)

            // Text display
            cLabelPane.removeText()
            when (state.workMode) {
                WorkMode.InputMode -> {
                    cLabelPane.createText(transLabel.text, Color.BLACK, it.displayX, it.displayY)
                }
                WorkMode.LabelMode -> {
                    val transGroup = state.transFile.getTransGroup(transLabel.groupId)
                    cLabelPane.createText(transGroup.name, Color.web(transGroup.colorHex), it.displayX, it.displayY)
                }
            }
        }
        cLabelPane.setOnLabelClick {
            if (state.workMode != WorkMode.InputMode) return@setOnLabelClick

            if (it.source.isDoubleClick) cLabelPane.moveToLabel(it.labelIndex)

            cTreeView.selectLabel(it.labelIndex)
        }
        cLabelPane.setOnLabelMove {
            state.doAction(LabelAction(
                ActionType.CHANGE, state,
                state.currentPicName, state.transFile.getTransLabel(state.currentPicName, it.labelIndex),
                newX = it.labelX,
                newY = it.labelY
            ))
        }
        cLabelPane.setOnLabelOther {
            if (state.workMode != WorkMode.LabelMode) return@setOnLabelOther
            if (state.transFile.groupCount == 0) return@setOnLabelOther

            val transGroup = state.transFile.getTransGroup(state.currentGroupId)

            cLabelPane.removeText()
            cLabelPane.createText(transGroup.name, Color.web(transGroup.colorHex), it.displayX, it.displayY)
        }
        Logger.info("Registered CLabelPane Handler", LOGSRC_CONTROLLER)
    }
    /**
     * Properties' bindings
     */
    private fun bind() {
        Logger.info("Binding properties...", LOGSRC_CONTROLLER)

        // Preferences
        cTransArea.fontProperty().bindBidirectional(Preference.textAreaFontProperty())
        pMain.dividers[0].positionProperty().bindBidirectional(Preference.mainDividerPositionProperty())
        pRight.dividers[0].positionProperty().bindBidirectional(Preference.rightDividerPositionProperty())
        view.showStatsBarProperty().bind(Preference.showStatsBarProperty())
        Logger.info("Bound Preferences @ DividerPositions, TextAreaFont", LOGSRC_CONTROLLER)

        // RecentFiles
        view.menuBar.recentFilesProperty().bind(RecentFiles.recentFilesProperty())
        Logger.info("Bound recent files menu", LOGSRC_CONTROLLER)

        // Set components disabled
        bSwitchViewMode.disableProperty().bind(!state.openedProperty())
        bSwitchWorkMode.disableProperty().bind(!state.openedProperty())
        cTransArea.disableProperty().bind(!state.openedProperty())
        cTreeView.disableProperty().bind(!state.openedProperty())
        cPicBox.disableProperty().bind(!state.openedProperty())
        cGroupBox.disableProperty().bind(!state.openedProperty())
        cSlider.disableProperty().bind(!state.openedProperty())
        cLabelPane.disableProperty().bind(!state.openedProperty())
        Logger.info("Bound disabled", LOGSRC_CONTROLLER)

        // CLigatureTextArea - rules
        cTransArea.ligatureRulesProperty().bind(Settings.ligatureRulesProperty())
        Logger.info("Bound ligature rules", LOGSRC_CONTROLLER)

        // CSlider - CLabelPane#scale
        cSlider.initScaleProperty().bindBidirectional(cLabelPane.initScaleProperty())
        cSlider.minScaleProperty().bindBidirectional(cLabelPane.minScaleProperty())
        cSlider.maxScaleProperty().bindBidirectional(cLabelPane.maxScaleProperty())
        cSlider.scaleProperty().bindBidirectional(cLabelPane.scaleProperty())
        Logger.info("Bound scale", LOGSRC_CONTROLLER)

        // Switch Button text
        bSwitchWorkMode.textProperty().bind(Bindings.createStringBinding({
            when (state.workMode) {
                WorkMode.InputMode -> I18N["mode.work.input"]
                WorkMode.LabelMode -> I18N["mode.work.label"]
            }
        }, state.workModeProperty()))
        bSwitchViewMode.textProperty().bind(Bindings.createStringBinding({
            when (state.viewMode) {
                ViewMode.IndexMode -> I18N["mode.view.index"]
                ViewMode.GroupMode -> I18N["mode.view.group"]
            }
        }, state.viewModeProperty()))
        Logger.info("Bound switch button text", LOGSRC_CONTROLLER)

        // GroupBox
        cGroupBox.itemsProperty().bind(Bindings.createObjectBinding(
            {
                if (!state.isOpened)
                    FXCollections.emptyObservableList()
                else
                    FXCollections.observableList(state.transFile.groupListProperty) { arrayOf(it.nameProperty) }

            }, state.transFileProperty()
        ))
        cGroupBox.indexProperty().bindBidirectional(state.currentGroupIdProperty())
        Logger.info("Bound GroupBox & CurrentGroupId", LOGSRC_CONTROLLER)

        // GroupBar
        cGroupBar.groupsProperty().bind(groupsBinding)
        cGroupBar.indexProperty().bindBidirectional(state.currentGroupIdProperty())
        Logger.info("Bound GroupBar & CurrentGroupId", LOGSRC_CONTROLLER)

        // PictureBox
        cPicBox.itemsProperty().bind(picNamesBinding)
        RuledGenericBidirectionalBinding.bind(
            cPicBox.valueProperty(), rule@{ _, _, newValue, _ -> newValue ?: emptyString() },
            state.currentPicNameProperty(), { _, _, newValue, _ -> newValue!! }
        )
        Logger.info("Bound PicBox & CurrentPicName", LOGSRC_CONTROLLER)

        // TreeView
        cTreeView.groupsProperty().bind(groupsBinding)
        cTreeView.labelsProperty().bind(labelsBinding)
        cTreeView.rootNameProperty().bind(state.currentPicNameProperty())
        cTreeView.viewModeProperty().bind(state.viewModeProperty())
        Logger.info("Bound CTreeView properties", LOGSRC_CONTROLLER)

        // LabelPane
        cLabelPane.groupsProperty().bind(Bindings.createObjectBinding(
            {
                if (!state.isOpened)
                    FXCollections.emptyObservableList()
                else
                    FXCollections.observableList(state.transFile.groupListProperty) { arrayOf(it.colorHexProperty) }
            }, state.transFileProperty()
        ))
        cLabelPane.imageProperty().bind(imageBinding)
        cLabelPane.labelsProperty().bind(labelsBinding)
        cLabelPane.labelRadiusProperty().bind(Settings.labelRadiusProperty())
        cLabelPane.labelColorOpacityProperty().bind(Settings.labelColorOpacityProperty())
        cLabelPane.labelTextOpaqueProperty().bind(Settings.labelTextOpaqueProperty())
        cLabelPane.newPictureScaleProperty().bind(Settings.newPictureScaleProperty())
        cLabelPane.commonCursorProperty().bind(Bindings.createObjectBinding({
            when (state.workMode) {
                WorkMode.LabelMode -> Cursor.CROSSHAIR
                WorkMode.InputMode -> Cursor.DEFAULT
            }
        }, state.workModeProperty()))
        Logger.info("Bound CLabelPane properties", LOGSRC_CONTROLLER)
    }
    /**
     * Properties' listeners (for unbindable)
     */
    private fun listen() {
        Logger.info("Attaching Listeners...", LOGSRC_CONTROLLER)

        // Default image auto-center
        cLabelPane.widthProperty().addListener(onChange {
            if (!state.isOpened || state.getPicFileNow().notExists()) cLabelPane.moveToCenter()
        })
        cLabelPane.heightProperty().addListener(onChange {
            if (!state.isOpened || state.getPicFileNow().notExists()) cLabelPane.moveToCenter()
        })
        Logger.info("Listened for default image location", LOGSRC_CONTROLLER)

        // isChanged
        cTransArea.textProperty().addListener(onChange {
            if (cTransArea.isBound) state.isChanged = true
        })
        Logger.info("Listened for isChanged", LOGSRC_CONTROLLER)

        // currentLabelIndex
        cTreeView.selectionModel.selectedItemProperty().addListener(onNew {
            if (it != null && it is CTreeLabelItem && cTreeView.selectionModel.selectedItems.size == 1)
                state.currentLabelIndex = it.index
        })
        state.currentPicNameProperty().addListener(onChange {
            // Clear selected when change pic
            state.currentLabelIndex = NOT_FOUND
        })
        Logger.info("Listened for CurrentLabelIndex", LOGSRC_CONTROLLER)
    }
    /**
     * Properties' effect on view
     */
    private fun effect() {
        Logger.info("Applying Affections...", LOGSRC_CONTROLLER)

        // Update StatsBar
        state.currentPicNameProperty().addListener(onNew {
            lLocation.text = String.format("%s : --", it)

            if (state.isOpened) {
                val file = state.transFile.getFile(it).takeIf(File?::notExists) ?: return@onNew
                Logger.error("Picture `${file.path}` not exists", LOGSRC_CONTROLLER)
                showError(state.stage, String.format(I18N["error.picture_not_exists.s"], file.path))
            }
        })
        state.currentLabelIndexProperty().addListener(onNew<Number, Int> {
            if (it == NOT_FOUND) {
                lLocation.text = String.format("%s : --", state.currentPicName)
            } else {
                lLocation.text = String.format("%s : %02d", state.currentPicName, it)
            }
        })
        Logger.info("Added effect: show info on InfoLabel", LOGSRC_CONTROLLER)

        // Clear text when some state change
        val clearTextListener = onChange<Any> { cLabelPane.removeText() }
        state.currentGroupIdProperty().addListener(clearTextListener)
        state.workModeProperty().addListener(clearTextListener)
        Logger.info("Added effect: clear text when some state change", LOGSRC_CONTROLLER)

        // Select TreeItem when state change
        state.currentGroupIdProperty().addListener(onNew<Number, Int> {
            if (!state.isOpened || it == NOT_FOUND || state.viewMode != ViewMode.GroupMode) return@onNew
            cTreeView.selectGroup(state.transFile.getTransGroup(it).name, false)
        })
        state.currentLabelIndexProperty().addListener(onNew<Number, Int> {
            if (!state.isOpened || it == NOT_FOUND) return@onNew
            cTreeView.selectLabel(it, false)
        })
        Logger.info("Added effect: select TreeItem on CurrentXXIndex change", LOGSRC_CONTROLLER)

        // Update text area when label change
        state.currentLabelIndexProperty().addListener(onNew<Number, Int> {
            if (!state.isOpened) return@onNew

            // unbind TextArea
            cTransArea.unbindBidirectional()

            if (it == NOT_FOUND) return@onNew

            // bind new text property
            cTransArea.bindBidirectional(state.transFile.getTransLabel(state.currentPicName, it).textProperty)
        })
        Logger.info("Added effect: bind text property on CurrentLabelIndex change", LOGSRC_CONTROLLER)

        // Bind Label and Tree
        cTreeView.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            if (it.button != MouseButton.PRIMARY || !it.isDoubleClick) return@addEventHandler

            val item = cTreeView.selectionModel.selectedItem
            if (item != null && item is CTreeLabelItem) cLabelPane.moveToLabel(item.index)
        }
        cTreeView.addEventHandler(KeyEvent.KEY_PRESSED) {
            val direction = when (it.code) {
                KeyCode.UP -> -1
                KeyCode.DOWN -> 1
                else -> return@addEventHandler
            }

            val item = cTreeView.getTreeItem(cTreeView.selectionModel.selectedIndex + direction)
            if (item != null && item is CTreeLabelItem) cLabelPane.moveToLabel(item.index)
        }
        Logger.info("Added effect: move to label on CTreeLabelItem select", LOGSRC_CONTROLLER)

        // Work Progress
        val workProgressListener = onChange<Any> {
            if (state.isOpened) RecentFiles.setProgressOf(state.translationFile.path,
                state.transFile.sortedPicNames.indexOf(state.currentPicName) to state.currentLabelIndex
            )
        }
        state.currentPicNameProperty().addListener(workProgressListener)
        state.currentLabelIndexProperty().addListener(workProgressListener)
        Logger.info("Added effect: update work progress on PicName/LabelIndex change", LOGSRC_CONTROLLER)
    }
    /**
     * Transformations
     */
    private fun transform() {
        Logger.info("Applying Transformations...", LOGSRC_CONTROLLER)

        // Transform CTreeView group selection to CGroupBox select
        cTreeView.selectionModel.selectedItemProperty().addListener(onNew {
            if (it != null && it is CTreeGroupItem)
                cGroupBox.select(state.transFile.getGroupIdByName(it.name))
        })
        Logger.info("Transformed CTreeGroupItem selected", LOGSRC_CONTROLLER)

        // Transform tab press in CTreeView to ViewModeBtn click
        cTreeView.addEventFilter(KeyEvent.KEY_PRESSED) {
            if (it.code != KeyCode.TAB) return@addEventFilter

            bSwitchViewMode.fire()
            it.consume() // Disable tab shift
        }
        Logger.info("Transformed Tab on CTreeView", LOGSRC_CONTROLLER)

        // Transform tab press in CLabelPane to WorkModeBtn click
        cLabelPane.addEventFilter(KeyEvent.KEY_PRESSED) {
            if (it.code != KeyCode.TAB) return@addEventFilter

            bSwitchWorkMode.fire()
            it.consume() // Disable tab shift
        }
        Logger.info("Transformed Tab on CLabelPane", LOGSRC_CONTROLLER)

        // Transform number key press to CGroupBox select
        view.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (!it.code.isDigitKey) return@addEventHandler

            val index = it.text.toInt() - 1
            if (index in 0..cGroupBox.items.size) cGroupBox.select(index)
        }
        Logger.info("Transformed num-key pressed", LOGSRC_CONTROLLER)

        // Transform Ctrl + Left/Right KeyEvent to CPicBox button click
        val arrowKeyChangePicHandler = EventHandler<KeyEvent> {
            if (!it.isControlOrMetaDown) return@EventHandler

            when (it.code) {
                KeyCode.LEFT -> cPicBox.back()
                KeyCode.RIGHT -> cPicBox.next()
                else -> return@EventHandler
            }

            it.consume() // Consume used event
        }
        view.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangePicHandler)
        cTransArea.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangePicHandler)
        Logger.info("Transformed Ctrl + Left/Right", LOGSRC_CONTROLLER)

        // Transform Ctrl + Up/Down KeyEvent to CTreeView select (and have effect: move to label)
        /**
         * Find next LabelItem as int index.
         * @return NOT_FOUND when have no next
         */
        fun getNextLabelItemIndex(from: Int, direction: Int): Int {
            // Make sure we have items to select
            cTreeView.getTreeItem(from).apply { this?.expand() }

            var index = from
            var item: TreeItem<String>?
            do {
                index += direction
                item = cTreeView.getTreeItem(index)
                item?.expand()

                if (item == null) return NOT_FOUND
            } while (item !is CTreeLabelItem)

            return index
        }
        val arrowKeyChangeLabelHandler = EventHandler<KeyEvent> {
            if (!(it.isControlOrMetaDown && it.code.isArrowKey)) return@EventHandler
            // Direction
            val labelItemShift: Int = when (it.code) {
                KeyCode.UP -> -1
                KeyCode.DOWN -> 1
                else -> return@EventHandler
            }
            // Make sure we'll not get into endless LabelItem find loop
            if (state.transFile.getTransList(state.currentPicName).isEmpty()) return@EventHandler

            var labelItemIndex: Int = cTreeView.selectionModel.selectedIndex + labelItemShift

            var item: TreeItem<String>? = cTreeView.getTreeItem(labelItemIndex)
            while (item !is CTreeLabelItem) {
                // if selected first and try getting previous, return last;
                // if selected last and try getting next, return first;
                labelItemIndex = getNextLabelItemIndex(
                    if (labelItemShift == -1)
                        if (labelItemIndex != NOT_FOUND) labelItemIndex else cTreeView.expandedItemCount
                    else
                        if (labelItemIndex != NOT_FOUND) labelItemIndex else 0
                , labelItemShift)
                item = cTreeView.getTreeItem(labelItemIndex)
            }

            cTreeView.selectionModel.clearSelection()
            cTreeView.selectionModel.select(labelItemIndex)
            cTreeView.scrollTo(labelItemIndex)
            cLabelPane.moveToLabel(state.currentLabelIndex)

            it.consume() // Consume used event
        }
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangeLabelHandler)
        cTransArea.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangeLabelHandler)
        Logger.info("Transformed Ctrl + Up/Down", LOGSRC_CONTROLLER)

        // Transform Ctrl + Enter to Ctrl + Down / Right (+Shift -> back)
        val enterKeyTransformerHandler = EventHandler<KeyEvent> {
            if (!(it.isControlOrMetaDown && it.code == KeyCode.ENTER)) return@EventHandler

            val backward = it.isShiftDown
            val selectedItemIndex = cTreeView.selectionModel.selectedIndex
            val nextLabelItemIndex = getNextLabelItemIndex(selectedItemIndex, if (backward) -1 else 1)

            val code = if (nextLabelItemIndex == NOT_FOUND) {
                // Met the bounds, consider change picture
                if (backward) KeyCode.LEFT else KeyCode.RIGHT
            } else {
                // Got next label, still in this picture
                if (backward) KeyCode.UP else KeyCode.DOWN
            }

            cLabelPane.fireEvent(keyEvent(it, code = code, character = "\u0000", text = ""))
            when (code) {
                KeyCode.LEFT  -> cLabelPane.fireEvent(keyEvent(it, code = KeyCode.UP, character = "\u0000", text = ""))
                KeyCode.RIGHT -> cLabelPane.fireEvent(keyEvent(it, code = KeyCode.DOWN, character = "\u0000", text = "" ))
                else -> doNothing()
            }

            it.consume() // Consume used event
        }
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED, enterKeyTransformerHandler)
        cTransArea.addEventHandler(KeyEvent.KEY_PRESSED, enterKeyTransformerHandler)
        Logger.info("Transformed Ctrl + Enter", LOGSRC_CONTROLLER)
    }

    // ----- Controller Methods ----- //

    /**
     * Specify pictures of current translation file
     * @return true if completed; false if not; null if cancel
     */
    fun specifyPicFiles(): Boolean? {
        dialogSpecify.owner ?: dialogSpecify.initOwner(state.stage)

        val picFiles = dialogSpecify.specify()

        // Closed or Cancelled
        if (picFiles.isEmpty()) return null

        val picCount = state.transFile.picCount
        val picNames = state.transFile.sortedPicNames
        var completed = true
        for (i in 0 until picCount) {
            val picFile = picFiles[i]
            if (picFile.notExists()) {
                completed = false
                continue
            }
            state.transFile.setFile(picNames[i], picFile!!)
        }
        return completed
    }

    /**
     * Whether stay here or not
     */
    fun stay(): Boolean {
        // Not open
        if (!state.isOpened) return false
        // Opened but saved
        if (!state.isChanged) return false

        // Opened but not saved
        val result = showAlert(state.stage, null, I18N["alert.not_save.content"], I18N["common.exit"])
        // Dialog present
        if (result.isPresent) when (result.get()) {
            ButtonType.YES -> {
                save(state.translationFile, silent = true)
                return false
            }
            ButtonType.NO -> return false
            ButtonType.CANCEL -> return true
        }
        // Dialog closed
        return true
    }

    /**
     * Create a new TransFile file and its FileSystem file.
     * @param file Which file the TransFile will write to
     * @param type Which type the Translation file will be
     * @return ProjectFolder if success, null if fail
     */
    fun new(file: File, type: FileType = FileType.getFileType(file)): File? {
        Logger.info("Newing $type to ${file.path}", LOGSRC_CONTROLLER)

        // Choose Pics
        var projectFolder = file.parentFile
        val potentialPics = ArrayList<String>()
        val selectedPics  = ArrayList<String>()
        while (potentialPics.isEmpty()) {
            // Find pictures
            projectFolder.listFiles()?.forEach {
                if (it.isFile && EXTENSIONS_PIC.contains(it.extension)) {
                    potentialPics.add(it.name)
                }
            }

            if (potentialPics.isEmpty()) {
                // Find nothing, this folder isn't project folder, confirm to use another folder
                val result = showConfirm(state.stage, I18N["confirm.project_folder_invalid"])
                if (result.isPresent && result.get() == ButtonType.YES) {
                    // Specify project folder
                    val newFolder = DirectoryChooser().apply { initialDirectory = projectFolder }.showDialog(state.stage)
                    if (newFolder != null) projectFolder = newFolder
                } else {
                    // Do not specify, cancel
                    Logger.info("Cancel (project folder has no pictures)", LOGSRC_CONTROLLER)
                    showInfo(state.stage, I18N["common.cancel"])
                    return null
                }
            } else {
                // Find some pics, continue procedure
                Logger.info("Project folder set to ${projectFolder.path}", LOGSRC_CONTROLLER)
            }
        }
        val result = showChoiceList(state.stage, potentialPics)
        if (result.isPresent) {
            if (result.get().isEmpty()) {
                Logger.info("Cancel (selected none)", LOGSRC_CONTROLLER)
                showInfo(state.stage, I18N["info.required_at_least_1_pic"])
                return null
            }
            selectedPics.addAll(result.get())
        } else {
            Logger.info("Cancel (didn't do the selection)", LOGSRC_CONTROLLER)
            showInfo(state.stage, I18N["common.cancel"])
            return null
        }
        Logger.info("Chose pictures", LOGSRC_CONTROLLER)

        // Prepare new TransFile
        val groupNameList = Settings.defaultGroupNameList
        val groupColorList = Settings.defaultGroupColorHexList
        val groupCreateList = Settings.isGroupCreateOnNewTransList
        val groupList = ArrayList<TransGroup>()
        for (i in groupNameList.indices)
            if (groupCreateList[i]) groupList.add(TransGroup(groupNameList[i], groupColorList[i]))
        val transMap = LinkedHashMap<String, MutableList<TransLabel>>()
        for (pic in selectedPics)
            transMap[pic] = ArrayList()
        val transFile = TransFile(TransFile.DEFAULT_VERSION, TransFile.DEFAULT_COMMENT, groupList, transMap)
        Logger.info("Built TransFile", LOGSRC_CONTROLLER)

        // Export to file
        try {
            export(file, type, transFile)
        } catch (e: IOException) {
            Logger.error("New failed", LOGSRC_CONTROLLER)
            Logger.exception(e)
            showError(state.stage, I18N["error.new_failed"])
            showException(state.stage, e)
            return null
        }
        Logger.info("Newed TransFile", LOGSRC_CONTROLLER)

        return projectFolder
    }
    /**
     * Open a translation file
     * @param file Which file will be open
     * @param type Which type the file is
     * @param projectFolder Which folder the pictures locate in; translation file's folder by default
     */
    fun open(file: File, type: FileType = FileType.getFileType(file), projectFolder: File = file.parentFile) {
        Logger.info("Opening TransFile: ${file.path}", LOGSRC_CONTROLLER)

        // Load File
        val transFile: TransFile
        try {
            transFile = load(file, type)
            transFile.projectFolder = projectFolder
        } catch (e: IOException) {
            Logger.error("Open failed", LOGSRC_CONTROLLER)
            Logger.exception(e)
            showError(state.stage, I18N["error.open_failed"])
            showException(state.stage, e)
            return
        }
        Logger.info("Loaded TransFile", LOGSRC_CONTROLLER)

        // Opened, update state
        state.isOpened = true
        state.transFile = transFile
        state.translationFile = file

        // Show info if comment not in default list
        // Should do this before update RecentFiles
        if (!RecentFiles.recentFiles.contains(file)) {
            val comment = transFile.comment.trim()
            if (!TransFile.DEFAULT_COMMENT_LIST.contains(comment)) {
                Logger.info("Showed modified comment", LOGSRC_CONTROLLER)
                showInfo(state.stage, I18N["m.comment.dialog.content"], comment, I18N["common.info"])
            }
        }

        // Update recent files
        RecentFiles.add(file)

        // Auto backup
        backupManager.clear()
        val bakDir = state.getBakFolder()!!
        if ((bakDir.exists() && bakDir.isDirectory) || bakDir.mkdir()) {
            backupManager.schedule()
            Logger.info("Scheduled auto-backup", LOGSRC_CONTROLLER)
        } else {
            Logger.warning("Auto-backup unavailable", LOGSRC_CONTROLLER)
            showWarning(state.stage, I18N["warning.auto_backup_unavailable"])
        }

        // Check lost
        if (state.transFile.checkLost().isNotEmpty()) {
            // Specify now?
            showConfirm(state.stage, I18N["specify.confirm.lost_pictures"]).ifPresent {
                if (it == ButtonType.YES) {
                    val completed = specifyPicFiles()
                    if (completed == null) showInfo(state.stage, I18N["specify.info.cancelled"])
                    else if (!completed) showInfo(state.stage, I18N["specify.info.incomplete"])
                }
            }
        }

        // Initialize workspace
        val (picIndex, labelIndex) = RecentFiles.getProgressOf(file.path)
        state.currentGroupId = 0
        state.currentPicName = state.transFile.sortedPicNames[picIndex.takeIf { it in 0 until state.transFile.picCount } ?: 0]
        state.currentLabelIndex = labelIndex.takeIf { state.transFile.getTransList(state.currentPicName).contains { l -> l.index == it } } ?: NOT_FOUND
        if (labelIndex != NOT_FOUND) cLabelPane.moveToLabel(labelIndex)

        // Accumulator
        accumulatorManager.clear()
        accumulatorManager.schedule()

        // Change title
        state.stage.title = INFO["application.name"] + " - " + file.name
        Logger.info("Opened TransFile", LOGSRC_CONTROLLER)
    }
    /**
     * Save a TransFile
     * @param file Which file will the TransFile write to
     * @param type Which type will the translation file be
     * @param silent Whether the save procedure is done in silence or not
     */
    fun save(file: File, type: FileType = FileType.getFileType(file), silent: Boolean = false) {
        // Whether overwriting existing file
        val overwrite = file.exists()

        Logger.info("Saving to ${file.path}, silent:$silent, overwrite:$overwrite", LOGSRC_CONTROLLER)

        // Check folder
        if (!silent) if (file.parentFile != state.transFile.projectFolder) {
            val confirm = showConfirm(state.stage, I18N["confirm.save_to_another_place"])
            if (!(confirm.isPresent && confirm.get() == ButtonType.YES)) return
        }

        // Use temp if overwrite
        val exportDest = if (overwrite) File.createTempFile(file.path, "temp").apply(File::deleteOnExit) else file

        // Export
        try {
            export(exportDest, type, state.transFile)
        } catch (e: IOException) {
            Logger.error("Export translation failed", LOGSRC_CONTROLLER)
            Logger.exception(e)
            showError(state.stage, I18N["error.save_failed"])
            showException(state.stage, e)

            Logger.info("Save failed", LOGSRC_CONTROLLER)
            return
        }
        Logger.info("Exported translation", LOGSRC_CONTROLLER)

        // Transfer to origin file if overwrite
        if (overwrite) {
            try {
                transfer(exportDest, file)
            } catch (e: Exception) {
                Logger.error("Transfer temp file failed", LOGSRC_CONTROLLER)
                Logger.exception(e)
                showError(state.stage, I18N["error.save_temp_transfer_failed"])
                showException(state.stage, e)

                Logger.info("Save failed", LOGSRC_CONTROLLER)
                return
            }
            Logger.info("Transferred temp file", LOGSRC_CONTROLLER)
        }

        // Update state
        state.translationFile = file
        state.isChanged = false

        // Change title
        state.stage.title = INFO["application.name"] + " - " + file.name

        if (!silent) showInfo(state.stage, I18N["info.saved_successfully"])

        Logger.info("Saved TransFile", LOGSRC_CONTROLLER)
    }
    /**
     * Recover from backup file
     * @param from The backup file
     * @param to Which file will the backup recover to
     */
    fun recovery(from: File, to: File, type: FileType = FileType.getFileType(to)) {
        Logger.info("Recovering from ${from.path}", LOGSRC_CONTROLLER)

        try {
            val tempFile = File.createTempFile("temp", type.name).apply(File::deleteOnExit)
            val transFile = load(from, FileType.MeoFile)

            export(tempFile, type, transFile)
            transfer(tempFile, to)
        } catch (e: Exception) {
            Logger.error("Recover failed", LOGSRC_CONTROLLER)
            Logger.exception(e)
            showError(state.stage, I18N["error.recovery_failed"])
            showException(state.stage, e)
        }
        Logger.info("Recovered to ${to.path}", LOGSRC_CONTROLLER)

        open(to, type)
    }
    /**
     * Export a TransFile in specific type
     * @param file Which file will the TransFile write to
     * @param type Which type will the translation file be
     */
    fun export(file: File, type: FileType = FileType.getFileType(file)) {
        Logger.info("Exporting to ${file.path}", LOGSRC_CONTROLLER)

        try {
            export(file, type, state.transFile)
        } catch (e: IOException) {
            Logger.error("Export failed", LOGSRC_CONTROLLER)
            Logger.exception(e)
            showError(state.stage, I18N["error.export_failed"])
            showException(state.stage, e)
        }

        showInfo(state.stage, I18N["info.exported_successful"])
    }
    /**
     * Generate a zip file with translation file and picture files
     * @param file Which file will the zip file write to
     */
    fun pack(file: File) {
        Logger.info("Packing to ${file.path}", LOGSRC_CONTROLLER)

        try {
            pack(file, state.transFile)
        } catch (e : IOException) {
            Logger.error("Pack failed", LOGSRC_CONTROLLER)
            Logger.exception(e)
            showError(state.stage, I18N["error.export_failed"])
            showException(state.stage, e)
        }

        showInfo(state.stage, I18N["info.exported_successful"])
    }

    fun reset() {
        backupManager.clear()
        accumulatorManager.clear()

        lBackup.text = I18N["stats.not_backed"]
        cTransArea.unbindBidirectional()

        state.stage.title = INFO["application.name"]
    }

    // ----- Global Methods ----- //

    fun requestUpdatePane() {
        cLabelPane.requestRemoveLabels()
        imageBinding.invalidate()
        cLabelPane.requestShowImage()
        cLabelPane.requestCreateLabels()
    }
    fun requestUpdateTree() {
        cTreeView.requestUpdate()
    }

    /**
     * Start a new LPFX task to check and show update info
     * @param showWhenUpdated If true, show info if already updated
     */
    fun checkUpdate(showWhenUpdated: Boolean = false) {
        val release = "https://github.com/Meodinger/LabelPlusFX/releases"
        val delay = 1000 * 60 * 24 * 30L

        val time = Date().time
        val last = Preference.lastUpdateNotice
        if (time - last < delay) {
            Logger.info("Check suppressed, last notice time is $last", LOGSRC_CONTROLLER)
            return
        }

        LPFXTask.createTask<Unit> {
            Logger.info("Fetching latest version...", LOGSRC_CONTROLLER)
            val version = fetchUpdateSync()
            if (version != Version.V0) Logger.info("Got latest version: $version (current $V)", LOGSRC_CONTROLLER)

            if (version > V) Platform.runLater {
                val suppressNoticeButtonType = ButtonType(I18N["update.dialog.suppress"], ButtonBar.ButtonData.OK_DONE)

                val dialog = Dialog<ButtonType>()
                dialog.initOwner(this@Controller.state.stage)
                dialog.title = I18N["update.dialog.title"]
                dialog.graphic = infoImageView
                dialog.dialogPane.buttonTypes.addAll(suppressNoticeButtonType, ButtonType.CLOSE)
                dialog.withContent(VBox()) {
                    add(Label(String.format(I18N["update.dialog.content.s"], version)))
                    add(Separator()) {
                        padding = Insets(COMMON_GAP / 2, 0.0, COMMON_GAP / 2, 0.0)
                    }
                    add(Hyperlink(I18N["update.dialog.link"])) {
                        padding = Insets(0.0)
                        setOnAction { this@Controller.state.application.hostServices.showDocument(release) }
                    }
                }

                val suppressButton = dialog.dialogPane.lookupButton(suppressNoticeButtonType)
                ButtonBar.setButtonUniformSize(suppressButton, false)

                dialog.showAndWait().ifPresent { type ->
                    if (type == suppressNoticeButtonType) {
                        Preference.lastUpdateNotice = time
                        Logger.info("Check suppressed, next notice time is ${time + delay}",
                            LOGSRC_CONTROLLER
                        )
                    }
                }
            } else if (showWhenUpdated) Platform.runLater {
                showInfo(this@Controller.state.stage, I18N["update.info.updated"])
            }
        }()
    }
    private fun fetchUpdateSync(): Version {
        val api = "https://api.github.com/repos/Meodinger/LabelPlusFX/releases"
        try {
            val proxy = ProxySelector.getDefault().select(URI(api))[0].also {
                if (it.type() != Proxy.Type.DIRECT) Logger.info("Using proxy $it", LOGSRC_CONTROLLER)
            }
            val connection = URL(api).openConnection(proxy).apply { connect() } as HttpsURLConnection
            if (connection.responseCode != 200) throw ConnectException("Response code ${connection.responseCode}")

            return ObjectMapper().readTree(connection.inputStream).let {
                if (it.isArray) Version.of(it[0]["name"].asText())
                else throw IOException("Should get an array, but not")
            }
        } catch (e: NoRouteToHostException) {
            Logger.warning("No network connection", LOGSRC_CONTROLLER)
        } catch (e: SocketException) {
            Logger.warning("Socket failed: ${e.message}", LOGSRC_CONTROLLER)
        } catch (e: SocketTimeoutException) {
            Logger.warning("Connect timeout", LOGSRC_CONTROLLER)
        } catch (e: ConnectException) {
            Logger.warning("Connect failed: ${e.message}", LOGSRC_CONTROLLER)
        } catch (e: IOException) {
            Logger.warning("Fetch I/O failed", LOGSRC_CONTROLLER)
            Logger.exception(e)
        }
        return Version.V0
    }


}
