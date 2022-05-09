package ink.meodinger.lpfx

import ink.meodinger.lpfx.action.*
import ink.meodinger.lpfx.component.*
import ink.meodinger.lpfx.component.common.*
import ink.meodinger.lpfx.io.*
import ink.meodinger.lpfx.options.*
import ink.meodinger.lpfx.type.*
import ink.meodinger.lpfx.util.Version
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.doNothing
import ink.meodinger.lpfx.util.event.*
import ink.meodinger.lpfx.util.file.*
import ink.meodinger.lpfx.util.image.*
import ink.meodinger.lpfx.util.property.*
import ink.meodinger.lpfx.util.string.emptyString
import ink.meodinger.lpfx.util.string.sortByDigit
import ink.meodinger.lpfx.util.timer.TimerTaskManager

import com.fasterxml.jackson.databind.ObjectMapper
import ink.meodinger.lpfx.component.dialog.*
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.binding.ObjectBinding
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.SetChangeListener
import javafx.embed.swing.SwingFXUtils
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Cursor
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
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
import java.util.Date
import javax.imageio.ImageIO
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

    // region View Components

    private val view            = state.view
    private val bSwitchViewMode = view.bSwitchViewMode does { switchViewMode() }
    private val bSwitchWorkMode = view.bSwitchWorkMode does { switchWorkMode() }
    private val lBackup         = view.lBackup
    private val lLocation       = view.lLocation
    private val lAccEditTime    = view.lAccEditTime
    private val cPicBox         = view.cPicBox
    private val cGroupBox       = view.cGroupBox
    private val cGroupBar       = view.cGroupBar
    private val cLabelPane      = view.cLabelPane
    private val cTreeView       = view.cTreeView
    private val cTransArea      = view.cTransArea

    // endregion

    // region TimerManagers

    private val bakTimeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT)
    private val bakFileFormatter = SimpleDateFormat("yyyy-HH-mm")
    private val backupManager = TimerTaskManager(AUTO_SAVE_DELAY, AUTO_SAVE_PERIOD) {
        if (state.isChanged) {
            val time = Date()
            val bak = state.getBakFolder()!!.resolve("${bakFileFormatter.format(time)}.$EXTENSION_BAK")
            try {
                export(bak, state.transFile)
                Platform.runLater {
                    lBackup.text = String.format(I18N["stats.last_backup.s"], bakTimeFormatter.format(time))
                }
                Logger.info("Backed TransFile", "Controller")
            } catch (e: IOException) {
                Logger.error("Auto-backup failed", "Controller")
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

    // endregion

    // region Global Bindings

    // Following Bindings should be created in order to avoid unexpected exceptions.
    // ----> note @ 2022/4/28 Meodinger: Now only god knows why this order.
    // Note that when ObjectProperty changes, its value will temporarily set to null.
    // So an elvis expression is needed to handle the null value.

    private val groupsBinding: ObjectBinding<ObservableList<TransGroup>> = Bindings.createObjectBinding(
        {
            state.transFileProperty().get()?.groupListObservable ?: FXCollections.emptyObservableList()
        }, state.transFileProperty()
    )
    private val picNamesBinding: ObjectBinding<ObservableList<String>> = Bindings.createObjectBinding(
        {
            state.transFileProperty().get()?.sortedPicNamesObservable ?: FXCollections.emptyObservableList()
        }, state.transFileProperty()
    )
    private val imageBinding: ObjectBinding<Image> = Bindings.createObjectBinding(
        {
            val file = state.getPicFileNow()
            if (file == null) {
                INIT_IMAGE
            } else {
                if (file.exists()) {
                    val imageByFX = Image(file.toURI().toURL().toString())

                    if (!imageByFX.isError) {
                        imageByFX
                    } else {
                        Logger.warning("Load `$file` as FXImage failed", "Controller")
                        Logger.exception(imageByFX.exception)

                        try {
                            val imageByIO = ImageIO.read(FileInputStream(file))?.let { SwingFXUtils.toFXImage(it, null) }
                            if (imageByIO != null) {
                                imageByIO
                            } else {
                                Logger.error("Load `$file` as AWTImage failed: Unsupported", "Controller")
                                showError(state.stage, I18N["error.picture_type_unsupported"])
                                INIT_IMAGE
                            }
                        } catch (e: IOException) {
                            Logger.error("Load `$file` as AWTImage failed: Exception", "Controller")
                            Logger.exception(e)
                            showError(state.stage, String.format(I18N["error.picture_load_failed.s"], file.name))
                            showException(state.stage, e)
                            INIT_IMAGE
                        }
                    }
                } else {
                    Logger.error("Picture `${file.path}` not exists", "Controller")
                    showError(state.stage, String.format(I18N["error.picture_not_exists.s"], file.path))

                    INIT_IMAGE
                }
            }
        }, state.currentPicNameProperty()
    )
    private val labelsBinding: ObjectBinding<ObservableList<TransLabel>> = Bindings.createObjectBinding(
        {
            state.transFileProperty().get()?.transMapObservable?.get(state.currentPicName) ?: FXCollections.emptyObservableList()
        }, state.currentPicNameProperty()
    )

    // endregion

    private fun switchViewMode() {
        state.viewMode = ViewMode.values()[(state.viewMode.ordinal + 1) % ViewMode.values().size]
    }
    private fun switchWorkMode() {
        state.workMode = WorkMode.values()[(state.workMode.ordinal + 1) % WorkMode.values().size]
        state.viewMode = Settings.viewModes[state.workMode.ordinal]
    }

    init {
        state.controller = this

        Logger.info("Controller initializing...", "Controller")

        init()
        bind()
        listen()
        effect()
        transform()

        Logger.info("Controller initialized", "Controller")

        // Display default image
        cLabelPane.isVisible = false
        Platform.runLater {
            // re-locate after the initial rendering
            cLabelPane.moveToCenter()
            cLabelPane.isVisible = true
        }
    }

    /**
     * Components Initialize
     */
    private fun init() {
        Logger.info("Initializing components...", "Controller")

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
        Logger.info("Set CFileChooser lastDirectory: ${CFileChooser.lastDirectory}", "Controller")

        // Settings
        state.viewMode = Settings.viewModes[state.workMode.ordinal]
        Logger.info("Applied Settings @ ViewMode", "Controller")

        // Drag and Drop
        view.setOnDragOver {
            if (it.dragboard.hasFiles()) it.acceptTransferModes(TransferMode.COPY)
            it.consume() // Consume used event
        }
        view.setOnDragDropped {
            if (stay()) return@setOnDragDropped

            val board = it.dragboard
            if (board.hasFiles()) {
                val file = board.files.first()

                state.reset()
                open(file, file.parentFile)

                it.isDropCompleted = true
            }
            it.consume() // Consume used event
        }
        Logger.info("Enabled Drag and Drop", "Controller")

        // Disable mnemonic parsing in TransArea
        cTransArea.addEventFilter(KeyEvent.ANY) {
            if (it.code == KeyCode.ALT) it.consume()
        }
        Logger.info("Registered CTransArea mnemonic parsing", "Controller")

        // Register Alias & Global redo/undo in TransArea
        cTransArea.addEventFilter(KeyEvent.KEY_PRESSED) {
            if ((it.isControlDown || it.isMetaDown) && it.code == KeyCode.Z) {
                if (!it.isShiftDown) {
                    if (cTransArea.isUndoable) cTransArea.undo() else if (state.undoable) state.undo()
                } else {
                    if (cTransArea.isRedoable) cTransArea.redo() else if (state.redoable) state.redo()
                }
                it.consume() // disable default undo/redo
            }
        }
        Logger.info("Registered CTransArea Alias & Global undo/redo", "Controller")

        // Register Ctrl/Alt/Meta + Scroll with font size change in TransArea
        cTransArea.addEventHandler(ScrollEvent.SCROLL) {
            if (!(it.isControlDown || it.isAltDown || it.isMetaDown)) return@addEventHandler

            val newSize = (cTransArea.font.size + if (it.deltaY > 0) 1 else -1).roundToInt()
                .coerceAtLeast(12).coerceAtMost(64)

            cTransArea.font = cTransArea.font.s(newSize.toDouble())
            cTransArea.positionCaret(0)

            it.consume()
        }
        Logger.info("Registered TransArea font size change", "Controller")

        // Register CGroupBar handler
        cGroupBar.setOnGroupCreate { (cTreeView.contextMenu as CTreeMenu).triggerGroupCreate() }
        Logger.info("Registered CGroupBar Add Handler", "Controller")

        // Register CLabelPane handler
        cLabelPane.setOnLabelCreate {
            if (state.workMode != WorkMode.LabelMode) return@setOnLabelCreate
            if (state.currentGroupId == NOT_FOUND) return@setOnLabelCreate

            val newIndex =
                if (state.currentLabelIndex != NOT_FOUND) state.currentLabelIndex + 1
                else state.transFile.getTransList(state.currentPicName).size + 1

            state.doAction(LabelAction(
                ActionType.ADD, state,
                state.currentPicName,
                TransLabel(newIndex, state.currentGroupId, it.labelX, it.labelY, "")
            ))
            // Update selection
            cTreeView.selectLabel(newIndex, clear = true, scrollTo = true)
            // If instant translate
            if (Settings.instantTranslate) cTransArea.requestFocus()
        }
        cLabelPane.setOnLabelRemove {
            if (state.workMode != WorkMode.LabelMode) return@setOnLabelRemove

            // Clear selection if current label will be removed
            if (it.labelIndex == state.currentLabelIndex) {
                state.currentLabelIndex = NOT_FOUND
            }
            state.doAction(LabelAction(
                ActionType.REMOVE, state,
                state.currentPicName,
                state.transFile.getTransLabel(state.currentPicName, it.labelIndex)
            ))
        }
        cLabelPane.setOnLabelHover {
            val transLabel = state.transFile.getTransLabel(state.currentPicName, it.labelIndex)

            // Text display
            cLabelPane.clearText()
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

            // Update selection
            cTreeView.selectLabel(it.labelIndex, clear = true, scrollTo = true)
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
            if (state.currentGroupId == NOT_FOUND) return@setOnLabelOther

            val transGroup = state.transFile.getTransGroup(state.currentGroupId)

            cLabelPane.clearText()
            cLabelPane.createText(transGroup.name, Color.web(transGroup.colorHex), it.displayX, it.displayY)
        }
        Logger.info("Registered CLabelPane Handler", "Controller")
    }
    /**
     * Properties' bindings
     */
    private fun bind() {
        Logger.info("Binding properties...", "Controller")

        // Preferences
        cTransArea.fontProperty().bindBidirectional(Preference.textAreaFontProperty())
        Logger.info("Bound TransArea font", "Controller")

        // CLigatureTextArea - rules
        cTransArea.ligatureRulesProperty().bind(Settings.ligatureRulesProperty())
        Logger.info("Bound ligature rules", "Controller")

        // Set components disabled
        bSwitchViewMode.disableProperty().bind(!state.openedProperty())
        bSwitchWorkMode.disableProperty().bind(!state.openedProperty())
        cTransArea.disableProperty().bind(!state.openedProperty())
        cTreeView.disableProperty().bind(!state.openedProperty())
        cPicBox.disableProperty().bind(!state.openedProperty())
        cGroupBox.disableProperty().bind(!state.openedProperty())
        cLabelPane.disableProperty().bind(!state.openedProperty())
        Logger.info("Bound disabled", "Controller")

        // Switch Button text
        bSwitchWorkMode.textProperty().bind(state.workModeProperty().asString())
        bSwitchViewMode.textProperty().bind(state.viewModeProperty().asString())
        Logger.info("Bound switch button text", "Controller")

        val groupIndexListener = onNew<Number, Int> {
            if (state.viewMode == ViewMode.GroupMode) {
                if (it != NOT_FOUND) {
                    if (cTreeView.isFocused) {
                        // if the change is result of CTreeView selection, add
                        cTreeView.selectGroup(state.transFile.getTransGroup(it).name, clear = false, scrollTo = false)
                    } else {
                        // if the change is result of GroupBar/Box selection, set
                        cTreeView.selectGroup(state.transFile.getTransGroup(it).name, clear = true, scrollTo = true)
                    }
                }
            } else {
                // In other modes, CurrentGroupId is set by CGroupBox/CGroupBar
                state.currentGroupId = it
            }
        }

        // GroupBar
        cGroupBar.groupsProperty().bind(groupsBinding)
        cGroupBar.indexProperty().addListener(groupIndexListener)
        state.currentGroupIdProperty().addListener(onNew<Number, Int>(cGroupBar.indexProperty()::set))
        Logger.info("Bound GroupBar & CurrentGroupId", "Controller")

        // GroupBox
        cGroupBox.itemsProperty().bind(groupsBinding)
        cGroupBox.indexProperty().addListener(groupIndexListener)
        state.currentGroupIdProperty().addListener(onNew<Number, Int>(cGroupBox.indexProperty()::set))
        Logger.info("Bound GroupBox & CurrentGroupId", "Controller")

        // PictureBox
        cPicBox.itemsProperty().bind(picNamesBinding)
        cPicBox.indexProperty().addListener(onNew<Number, Int> {
            state.currentPicName = state.transFile.sortedPicNames.getOrElse(it) { emptyString() }
        })
        state.currentPicNameProperty().addListener(onNew {
            cPicBox.index = state.transFile.sortedPicNames.indexOf(it)
        })
        Logger.info("Bound PicBox & CurrentPicName", "Controller")

        // TreeView
        cTreeView.groupsProperty().bind(groupsBinding)
        cTreeView.labelsProperty().bind(labelsBinding)
        cTreeView.rootNameProperty().bind(state.currentPicNameProperty())
        cTreeView.viewModeProperty().bind(state.viewModeProperty())
        Logger.info("Bound CTreeView properties", "Controller")

        // LabelPane
        cLabelPane.groupsProperty().bind(groupsBinding)
        cLabelPane.imageProperty().bind(imageBinding)
        cLabelPane.labelsProperty().bind(labelsBinding)
        cLabelPane.labelRadiusProperty().bind(Settings.labelRadiusProperty())
        cLabelPane.labelColorOpacityProperty().bind(Settings.labelColorOpacityProperty())
        cLabelPane.labelTextOpaqueProperty().bind(Settings.labelTextOpaqueProperty())
        cLabelPane.newPictureScaleProperty().bind(Settings.newPictureScaleProperty())
        cLabelPane.commonCursorProperty().bind(state.workModeProperty().transform {
            when (it!!) {
                WorkMode.LabelMode -> Cursor.CROSSHAIR
                WorkMode.InputMode -> Cursor.DEFAULT
            }
        })
        Logger.info("Bound CLabelPane properties", "Controller")
    }
    /**
     * Properties' listeners (for unbindable)
     */
    private fun listen() {
        Logger.info("Attaching Listeners...", "Controller")

        // Bind Tree and Current
        cTreeView.selectedGroupProperty().addListener(onNew<Number, Int> {
            if (it != NOT_FOUND) state.currentGroupId = it
        })
        cTreeView.selectedLabelProperty().addListener(onNew<Number, Int> {
            if (it != NOT_FOUND) state.currentLabelIndex = it
        })
        Logger.info("Listened for selectedGroup/Label", "Controller")

        // isChanged
        cTransArea.textProperty().addListener(onChange {
            if (cTransArea.isBound) state.isChanged = true
        })
        Logger.info("Listened for isChanged", "Controller")
    }
    /**
     * Properties' effect on view
     */
    private fun effect() {
        Logger.info("Applying Affections...", "Controller")

        // Default image auto-center
        cLabelPane.widthProperty().addListener(onChange {
            if (!state.isOpened || !state.getPicFileNow().exists()) cLabelPane.moveToCenter()
        })
        cLabelPane.heightProperty().addListener(onChange {
            if (!state.isOpened || !state.getPicFileNow().exists()) cLabelPane.moveToCenter()
        })
        Logger.info("Added effect: default image auto-center", "Controller")

        // Update StatsBar
        state.currentPicNameProperty().addListener(onNew {
            lLocation.text = String.format("%s : --", it.ifEmpty { "--" })
        })
        state.currentLabelIndexProperty().addListener(onNew<Number, Int> {
            if (it == NOT_FOUND) {
                lLocation.text = String.format("%s : --", state.currentPicName.ifEmpty { "--" })
            } else {
                lLocation.text = String.format("%s : %02d", state.currentPicName, it)
            }
        })
        Logger.info("Added effect: show info on InfoLabel", "Controller")

        // Clear text when some state change
        val clearTextListener = onChange<Any> { cLabelPane.clearText() }
        state.currentGroupIdProperty().addListener(clearTextListener)
        state.workModeProperty().addListener(clearTextListener)
        Logger.info("Added effect: clear text when some state change", "Controller")

        // Set current-label-index to unbind TextArea & clear CTreeView focus
        state.currentPicNameProperty().addListener(onChange {
            state.currentLabelIndex = NOT_FOUND
            // Use run-later to clear after items rendering
            Platform.runLater { cTreeView.selectionModel.clearSelection() }
        })

        // Update text area when label change
        state.currentLabelIndexProperty().addListener(onNew<Number, Int> {
            if (!state.isOpened) return@onNew

            // unbind TextArea
            cTransArea.unbindText()

            if (it == NOT_FOUND) return@onNew

            // bind new text property
            cTransArea.bindText(state.transFile.getTransLabel(state.currentPicName, it).textProperty)
        })
        Logger.info("Added effect: bind text property on CurrentLabelIndex change", "Controller")

        // Bind Tree and LabelPane
        cTreeView.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            if (it.button != MouseButton.PRIMARY || !it.isDoubleClick) return@addEventHandler

            val item = cTreeView.selectionModel.selectedItem
            if (item is CTreeLabelItem) cLabelPane.moveToLabel(item.index)
        }
        cTreeView.addEventHandler(KeyEvent.KEY_PRESSED) {
            val direction = when (it.code) {
                KeyCode.UP -> -1
                KeyCode.DOWN -> 1
                else -> return@addEventHandler
            }

            val item = cTreeView.getTreeItem(cTreeView.selectionModel.selectedIndex + direction)
            if (item is CTreeLabelItem) cLabelPane.moveToLabel(item.index)
        }
        Logger.info("Added effect: move to label on CTreeLabelItem select", "Controller")

        // When LabelPane Box Selection
        cLabelPane.selectedLabelsProperty().addListener(SetChangeListener {
            cTreeView.selectLabels(it.set, clear = true, scrollTo = true)
        })
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (cLabelPane.selectedLabels.isEmpty()) return@addEventHandler

            if (it.code == KeyCode.DELETE) {
                val indices = cLabelPane.selectedLabels.toSortedSet().reversed()

                // Clear selection if current label will be removed
                if (indices.contains(state.currentLabelIndex)) {
                    state.currentLabelIndex = NOT_FOUND
                }

                state.doAction(ComplexAction(indices.map { index ->
                    LabelAction(
                        ActionType.REMOVE, state,
                        state.currentPicName,
                        state.transFile.getTransLabel(state.currentPicName, index),
                    )
                }))
            }
        }
        Logger.info("Added effect: CLabelPane box-selection to CTreeView select & delete", "Controller")
    }
    /**
     * Transformations
     */
    private fun transform() {
        Logger.info("Applying Transformations...", "Controller")

        // Transform tab press in CTreeView to ViewModeBtn click
        cTreeView.addEventFilter(KeyEvent.KEY_PRESSED) {
            if (it.code != KeyCode.TAB) return@addEventFilter

            bSwitchViewMode.fire()
            it.consume() // Disable tab shift
        }
        Logger.info("Transformed Tab on CTreeView", "Controller")

        // Transform tab press in CLabelPane to WorkModeBtn click
        cLabelPane.addEventFilter(KeyEvent.KEY_PRESSED) {
            if (it.code != KeyCode.TAB) return@addEventFilter

            bSwitchWorkMode.fire()
            it.consume() // Disable tab shift
        }
        Logger.info("Transformed Tab on CLabelPane", "Controller")

        // Transform number key press to CTreeView select
        view.addEventHandler(KeyEvent.KEY_PRESSED) {
            // TODO: 012 -> index 12

            if (!it.code.isDigitKey) return@addEventHandler
            val id = (it.text.toInt() - 1).takeIf { i -> i in 0 until state.transFile.groupCount } ?: return@addEventHandler

            if (state.viewMode == ViewMode.GroupMode) {
                cTreeView.selectGroup(state.transFile.getTransGroup(id).name, clear = true, scrollTo = false)
            } else {
                state.currentGroupId = id
            }

            it.consume() // Consume used event
        }
        Logger.info("Transformed num-key pressed", "Controller")

        // Transform Ctrl + Left/Right KeyEvent to CPicBox button click
        val arrowKeyChangePicHandler = EventHandler<KeyEvent> {
            if (!(it.isControlDown || it.isMetaDown)) return@EventHandler

            when (it.code) {
                KeyCode.LEFT  -> cPicBox.back()
                KeyCode.RIGHT -> cPicBox.next()
                else -> return@EventHandler
            }

            it.consume() // Consume used event
        }
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangePicHandler)
        cTransArea.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangePicHandler)
        cTreeView.addEventHandler(KeyEvent.KEY_PRESSED) handler@{
            when (it.code) {
                KeyCode.LEFT  -> cPicBox.back()
                KeyCode.RIGHT -> cPicBox.next()
                else -> return@handler
            }

            it.consume() // Consume used event
        }
        Logger.info("Transformed Ctrl + Left/Right", "Controller")

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

        // Transform Ctrl + Up/Down KeyEvent to CTreeView select (and have effect: move to label)
        val arrowKeyChangeLabelHandler = EventHandler<KeyEvent> {
            if (!((it.isControlDown || it.isMetaDown) && it.code.isArrowKey)) return@EventHandler
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
            cTreeView.selectionModel.select(item)
            cTreeView.scrollTo(labelItemIndex)
            cLabelPane.moveToLabel(state.currentLabelIndex)

            it.consume() // Consume used event
        }
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangeLabelHandler)
        cTransArea.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangeLabelHandler)
        Logger.info("Transformed Ctrl + Up/Down", "Controller")

        // Transform Ctrl + Enter to Ctrl + Down / Right (+Shift -> back)
        val enterKeyTransformerHandler = EventHandler<KeyEvent> {
            if (!((it.isControlDown || it.isMetaDown) && it.code == KeyCode.ENTER)) return@EventHandler

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
        Logger.info("Transformed Ctrl + Enter", "Controller")
    }

    // Controller Methods

    /**
     * Whether stay here or not
     */
    fun stay(): Boolean {
        // Not open
        if (!state.isOpened) return false
        // Opened but saved
        if (!state.isChanged) return false

        // Opened but not saved
        val result = showConfirm(state.stage, null, I18N["alert.not_save.content"], I18N["common.exit"])
        // Dialog present
        if (result.isPresent) when (result.get()) {
            ButtonType.YES -> {
                save(state.translationFile, true)
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
     * File save type is based on the extension of the file.
     * @param file Which file the TransFile will write to
     * @return ProjectFolder if success, null if fail
     */
    fun new(file: File): File? {
        Logger.info("Newing to ${file.path}", "Controller")

        // Choose Pics
        var projectFolder = file.parentFile
        val potentialPics = ArrayList<String>()
        val selectedPics  = ArrayList<String>()
        while (potentialPics.isEmpty()) {
            // Find pictures
            projectFolder.listFiles()?.forEach {
                if (it.isFile && EXTENSIONS_PIC.contains(it.extension.lowercase())) {
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
                    Logger.info("Cancel (project folder has no pictures)", "Controller")
                    showInfo(state.stage, I18N["common.cancel"])
                    return null
                }
            } else {
                // Find some pics, continue procedure
                Logger.info("Project folder set to ${projectFolder.path}", "Controller")
            }
        }
        val result = showChoiceList(state.stage, potentialPics.sortByDigit(), emptyList())
        if (result.isPresent) {
            if (result.get().isEmpty()) {
                Logger.info("Cancel (selected none)", "Controller")
                showInfo(state.stage, I18N["info.required_at_least_1_pic"])
                return null
            }
            selectedPics.addAll(result.get())
        } else {
            Logger.info("Cancel (didn't do the selection)", "Controller")
            showInfo(state.stage, I18N["common.cancel"])
            return null
        }
        Logger.info("Chose pictures", "Controller")

        // Prepare new TransFile
        val transFile = TransFile(
            groupList = Settings.defaultGroupNameList
                .mapIndexed { index, name -> TransGroup(name, Settings.defaultGroupColorHexList[index]) }
                .filterIndexed { index, _ -> Settings.isGroupCreateOnNewTransList[index] }
                .let { if (FileType.getFileType(file) == FileType.LPFile) it.subList(0, 9) else it }
                .toMutableList(),
            transMap = selectedPics.associateWithTo(HashMap()) { ArrayList() }
        )
        Logger.info("Built TransFile", "Controller")

        // Export to file
        try {
            export(file, transFile)
        } catch (e: IOException) {
            Logger.error("New failed", "Controller")
            Logger.exception(e)
            showError(state.stage, I18N["error.new_failed"])
            showException(state.stage, e)
            return null
        }
        Logger.info("Newed TransFile", "Controller")

        return projectFolder
    }
    /**
     * Open a translation file.
     * File save type is based on the extension of the file.
     * @param file Which file will be open
     * @param projectFolder Which folder the pictures locate in
     */
    fun open(file: File, projectFolder: File) {
        Logger.info("Opening TransFile: ${file.path}", "Controller")

        // Load File
        val transFile: TransFile
        try {
            transFile = load(file)
            transFile.projectFolder = projectFolder
        } catch (e: IOException) {
            Logger.error("Open failed", "Controller")
            Logger.exception(e)
            showError(state.stage, I18N["error.open_failed"])
            showException(state.stage, e, file)
            return
        }
        Logger.info("Loaded TransFile", "Controller")

        // Opened, update state
        state.translationFile = file
        state.transFile = transFile
        state.isOpened = true

        // Show info if comment not in default list
        // Should do this before update RecentFiles
        if (!RecentFiles.recentFiles.contains(file)) {
            val comment = transFile.comment.trim().replace(Regex("\n(\\s)+"), "\n")
            if (!TransFile.DEFAULT_COMMENT_LIST.contains(comment)) {
                Logger.info("Showed modified comment", "Controller")
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
            Logger.info("Scheduled auto-backup", "Controller")
        } else {
            Logger.warning("Auto-backup unavailable", "Controller")
            showWarning(state.stage, I18N["warning.auto_backup_unavailable"])
        }

        // Check lost
        if (state.transFile.checkLost().isNotEmpty()) {
            // Specify now?
            val result = showConfirm(state.stage, I18N["specify.confirm.lost_pictures"])
            if (result.isPresent && result.get() == ButtonType.YES) {
                val completed = state.application.dialogSpecify.specify()
                if (completed == null) showInfo(state.stage, I18N["specify.info.cancelled"])
                else if (!completed) showInfo(state.stage, I18N["specify.info.incomplete"])
            }
        }

        // Initialize workspace
        val (picIndex, labelIndex) = RecentFiles.getProgressOf(file.path)
        state.currentGroupId = 0
        state.currentPicName = state.transFile.sortedPicNames[picIndex.takeIf { it in 0 until state.transFile.picCount } ?: 0]
        state.currentLabelIndex = labelIndex.takeIf { state.transFile.getTransList(state.currentPicName).any { l -> l.index == it } } ?: NOT_FOUND
        if (labelIndex != NOT_FOUND) cLabelPane.moveToLabel(labelIndex)

        // Accumulator
        accumulatorManager.clear()
        accumulatorManager.schedule()

        // Change title
        state.stage.title = INFO["application.name"] + " - " + file.name
        Logger.info("Opened TransFile", "Controller")
    }
    /**
     * Save a TransFile.
     * File save type is based on the extension of the file.
     * @param file Which file will the TransFile write to
     * @param silent Whether the save procedure is done in silence or not
     */
    fun save(file: File, silent: Boolean = false) {
        // Whether overwriting existing file
        val overwrite = file.exists()

        Logger.info("Saving to ${file.path}, silent:$silent, overwrite:$overwrite", "Controller")

        // Check folder
        if (!silent) if (file.parentFile != state.transFile.projectFolder) {
            val confirm = showConfirm(state.stage, I18N["confirm.save_to_another_place"])
            if (!(confirm.isPresent && confirm.get() == ButtonType.YES)) return
        }

        // Use temp if overwrite
        val exportDest = if (overwrite) File.createTempFile(file.path, "temp").apply(File::deleteOnExit) else file

        // Export
        try {
            export(exportDest, state.transFile)
        } catch (e: IOException) {
            Logger.error("Export translation failed", "Controller")
            Logger.exception(e)
            showError(state.stage, I18N["error.save_failed"])
            showException(state.stage, e)

            Logger.info("Save failed", "Controller")
            return
        }
        Logger.info("Exported translation", "Controller")

        // Transfer to origin file if overwrite
        if (overwrite) {
            try {
                transfer(exportDest, file)
            } catch (e: Exception) {
                Logger.error("Transfer temp file failed", "Controller")
                Logger.exception(e)
                showError(state.stage, I18N["error.save_temp_transfer_failed"])
                showException(state.stage, e)

                Logger.info("Save failed", "Controller")
                return
            }
            Logger.info("Transferred temp file", "Controller")
        }

        // Update state
        state.translationFile = file
        state.isChanged = false

        // Update recent files
        RecentFiles.add(file)

        // Update work progress
        RecentFiles.setProgressOf(state.translationFile.path,
            state.transFile.sortedPicNames.indexOf(state.currentPicName) to state.currentLabelIndex
        )

        // Change title
        state.stage.title = INFO["application.name"] + " - " + file.name

        if (!silent) showInfo(state.stage, I18N["info.saved_successfully"])

        Logger.info("Saved TransFile", "Controller")
    }
    /**
     * Recover from backup file.
     * File save type is based on the extension of the file.
     * @param from The backup file, will be treat as MeoFile
     * @param to Which file will the backup recover to
     */
    fun recovery(from: File, to: File) {
        Logger.info("Recovering from ${from.path}", "Controller")

        try {
            val tempFile = File.createTempFile("LPFX", null).apply(File::deleteOnExit)
            val transFile = load(from)

            export(tempFile, transFile)
            transfer(tempFile, to)
        } catch (e: Exception) {
            Logger.error("Recover failed", "Controller")
            Logger.exception(e)
            showError(state.stage, I18N["error.recovery_failed"])
            showException(state.stage, e)
        }
        Logger.info("Recovered to ${to.path}", "Controller")

        open(to, to.parentFile)
    }
    /**
     * Export a TransFile in specific type.
     * File save type is based on the extension of the file.
     * @param file Which file will the TransFile write to
     */
    fun export(file: File) {
        Logger.info("Exporting to ${file.path}", "Controller")

        try {
            export(file, state.transFile)
        } catch (e: IOException) {
            Logger.error("Export failed", "Controller")
            Logger.exception(e)
            showError(state.stage, I18N["error.export_failed"])
            showException(state.stage, e)
        }

        showInfo(state.stage, I18N["info.exported_successful"])
    }
    /**
     * Generate a zip file with translation file and picture files
     * Translation file save type is based on the extension of the file.
     * @param file Which file will the zip file write to
     */
    fun pack(file: File) {
        Logger.info("Packing to ${file.path}", "Controller")

        try {
            pack(file, state.transFile, FileType.getFileType(state.translationFile))
        } catch (e : IOException) {
            Logger.error("Pack failed", "Controller")
            Logger.exception(e)
            showError(state.stage, I18N["error.export_failed"])
            showException(state.stage, e)
        }

        showInfo(state.stage, I18N["info.exported_successful"])
    }

    /**
     * Reset all components
     */
    fun reset() {
        backupManager.clear()
        accumulatorManager.clear()

        lBackup.text = I18N["stats.not_backed"]
        cTransArea.unbindText()

        state.stage.title = INFO["application.name"]
    }

    // Global Methods

    /**
     * Request LabelPane re-render
     */
    fun requestUpdatePane() {
        imageBinding.invalidate()
        cLabelPane.requestRemoveLabels()
        cLabelPane.requestShowImage()
        cLabelPane.requestCreateLabels()
    }
    /**
     * Request TreeView re-render
     */
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
            Logger.info("Check suppressed, last notice time is $last", "Controller")
            return
        }

        LPFXTask.createTask<Unit> {
            Logger.info("Fetching latest version...", "Controller")
            val version = fetchLatestSync()
            if (version != Version.V0) Logger.info("Got latest version: $version (current $V)", "Controller")

            if (version > V) Platform.runLater {
                val suppressNoticeButtonType = ButtonType(I18N["update.dialog.suppress"], ButtonBar.ButtonData.OK_DONE)

                val dialog = Dialog<ButtonType>()
                dialog.initOwner(this@Controller.state.stage)
                dialog.title = I18N["update.dialog.title"]
                dialog.graphic = ImageView(IMAGE_INFO.resizeByRadius(GENERAL_ICON_RADIUS))
                dialog.dialogPane.buttonTypes.addAll(suppressNoticeButtonType, ButtonType.CLOSE)
                dialog.dialogPane.withContent(VBox()) {
                    add(Label(String.format(I18N["update.dialog.content.s"], version)))
                    add(Separator()) {
                        padding = Insets(8.0, 0.0, 8.0, 0.0)
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
                            "Controller"
                        )
                    }
                }
            } else if (showWhenUpdated) Platform.runLater {
                showInfo(this@Controller.state.stage, I18N["update.info.updated"])
            }
        }()
    }
    private fun fetchLatestSync(): Version {
        val api = "https://api.github.com/repos/Meodinger/LabelPlusFX/releases"
        try {
            val proxy = ProxySelector.getDefault().select(URI(api))[0].also {
                if (it.type() != Proxy.Type.DIRECT) Logger.info("Using proxy $it", "Controller")
            }
            val connection = URL(api).openConnection(proxy).apply { connect() } as HttpsURLConnection
            if (connection.responseCode != 200) throw ConnectException("Response code ${connection.responseCode}")

            return ObjectMapper().readTree(connection.inputStream).let {
                if (it.isArray) Version.of(it[0]["name"].asText())
                else throw IOException("Should get an array, but not")
            }
        } catch (e: NoRouteToHostException) {
            Logger.warning("No network connection", "Controller")
        } catch (e: SocketException) {
            Logger.warning("Socket failed: ${e.message}", "Controller")
        } catch (e: SocketTimeoutException) {
            Logger.warning("Connect timeout", "Controller")
        } catch (e: ConnectException) {
            Logger.warning("Connect failed: ${e.message}", "Controller")
        } catch (e: IOException) {
            Logger.warning("Fetch I/O failed", "Controller")
            Logger.exception(e)
        }
        return Version.V0
    }

}
