package ink.meodinger.lpfx

import ink.meodinger.lpfx.action.*
import ink.meodinger.lpfx.component.*
import ink.meodinger.lpfx.component.common.*
import ink.meodinger.lpfx.component.dialog.*
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
import ink.meodinger.lpfx.util.string.sortByDigit
import ink.meodinger.lpfx.util.timer.TimerTaskManager

import com.fasterxml.jackson.databind.ObjectMapper
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
import javafx.stage.DirectoryChooser
import java.io.File
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
    private val bSwitchViewMode = view.bSwitchViewMode
    private val bSwitchWorkMode = view.bSwitchWorkMode
    private val lLocation       = view.lLocation
    private val lBackup         = view.lBackup
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
    private val bakFileFormatter = SimpleDateFormat("yy-MM-dd#HH-mm")
    private val backupManager = TimerTaskManager(AUTO_SAVE_DELAY, AUTO_SAVE_PERIOD) {
        if (state.isChanged) {
            val time = Date()
            val bak = state.getBakFolder().resolve("${bakFileFormatter.format(time)}.$EXTENSION_BAK")
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

    private var accumulator: Long = 16 * 60 * 60 * ONE_SECOND  // Shift to 1970/01/02 00:00 GMT+8
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
            if (!state.isOpened) {
                // Not opened
                INIT_IMAGE
            } else {
                // Opened and selected
                val file = state.getPicFileNow()
                if (file.exists()) {
                    var imageByFX = Image(file.toURI().toURL().toString())

                    //if the image is too large,limit the size of image
                    if(imageByFX.width > 5000 || imageByFX.height > 5000) {
                        Logger.info("limit the size of image because `$file` is too large  ", "Controller")
                        imageByFX = Image(file.toURI().toURL().toString(),5000.0,5000.0,true,true)
                    }

                    if (!imageByFX.isError) {
                        imageByFX
                    } else {
                        Logger.warning("Load `$file` as FXImage failed", "Controller")

                        // These exceptions are internal, so we cannot use `is`.
                        when (imageByFX.exception::class.java.simpleName) {
                            // No loader for image data (or url is null/empty, which will not happen)
                            "ImageStorageException" -> doNothing()
                            else -> Logger.exception(imageByFX.exception)
                        }

                        try {
                            val imageByIO = ImageIO.read(file)?.let { SwingFXUtils.toFXImage(it, null) }
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
        cLabelPane.moveToCenter()
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
        Logger.info("Applied CFileChooser lastDirectory: ${CFileChooser.lastDirectory}", "Controller")

        // Settings
        state.viewMode = Settings.viewModes[state.workMode.ordinal]
        Logger.info("Applied Settings @ ViewMode", "Controller")

        // Drag and Drop
        view.setOnDragOver {
            // Mark immediately when this event will be consumed
            it.consume() // stop further propagation

            if (it.dragboard.hasFiles()) it.acceptTransferModes(TransferMode.COPY)
        }
        view.setOnDragDropped {
            // Mark immediately when this event will be consumed
            it.consume() // stop further propagation

            if (stay()) {
                it.isDropCompleted = true
            } else {
                state.reset()
                if (it.dragboard.hasFiles()) {
                    val file = it.dragboard.files.first()
                    open(file, file.parentFile)
                    it.isDropCompleted = true
                }
            }
        }
        Logger.info("Registered Drag and Drop", "Controller")

        // Register Alt(Win)/Command(macOS) + X to mark/unmark Label
        val markHandler = EventHandler<KeyEvent> {
            if ((it.isAltDown || (Config.isMac && it.isControlDown)) && it.code == KeyCode.X) {
                if (state.isOpened && state.currentLabelIndex != NOT_FOUND) {
                    val transLabel = state.transFile.getTransLabel(state.currentPicName, state.currentLabelIndex)
                    transLabel.isMarked = !transLabel.isMarked
                }
            }
        }
        cTreeView.addEventHandler(KeyEvent.KEY_PRESSED, markHandler)
        cTransArea.addEventHandler(KeyEvent.KEY_PRESSED, markHandler)
        Logger.info("Registered Ctrl/Meta + X mark/unmark TransLabel", "Controller")

        // Register Alias & Global redo/undo in TransArea
        cTransArea.addEventFilter(KeyEvent.KEY_PRESSED) {
            if ((it.isControlDown || it.isMetaDown) && it.code == KeyCode.Z) {
                // Mark immediately when this event will be consumed
                it.consume() // disable default undo/redo

                if (!it.isShiftDown) {
                    if (cTransArea.isUndoable) cTransArea.undo() else if (state.isUndoable) state.undo()
                } else {
                    if (cTransArea.isRedoable) cTransArea.redo() else if (state.isRedoable) state.redo()
                }
            }
        }
        Logger.info("Registered CTransArea Alias & Global undo/redo", "Controller")

        // Register Ctrl/Alt/Meta + Scroll with font size change in TransArea
        cTransArea.addEventHandler(ScrollEvent.SCROLL) {
            if (it.isControlDown || it.isAltDown || it.isMetaDown) {
                // Mark immediately when this event will be consumed
                it.consume() // stop further propagation

                val newSize = (cTransArea.font.size + if (it.deltaY > 0) 1 else -1).roundToInt()
                    .coerceAtLeast(12).coerceAtMost(64).toDouble()

                cTransArea.font = cTransArea.font.s(newSize)
                cTransArea.positionCaret(0)
            }
        }
        Logger.info("Registered TransArea font size change", "Controller")

        // Register CLabelPane handler
        cLabelPane.addEventFilter(CLabelPane.LabelEvent.LABEL_ANY) {
            when (it.eventType) {
                CLabelPane.LabelEvent.LABEL_OTHER,
                CLabelPane.LabelEvent.LABEL_HOVER -> doNothing()
                else -> Logger.debug(it, "Controller")
            }
        }
        cLabelPane.setOnLabelCreate handler@{
            when (state.workMode) {
                WorkMode.InputMode -> doNothing()
                WorkMode.LabelMode -> {
                    if (state.currentGroupId == NOT_FOUND) return@handler

                    // Use next as new label index if current found
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
            }
        }
        cLabelPane.setOnLabelRemove handler@{
            when (state.workMode) {
                WorkMode.InputMode -> doNothing()
                WorkMode.LabelMode -> {
                    // Clear selection if current label will be removed
                    if (it.labelIndex == state.currentLabelIndex) state.currentLabelIndex = NOT_FOUND

                    state.doAction(LabelAction(
                        ActionType.REMOVE, state,
                        state.currentPicName,
                        state.transFile.getTransLabel(state.currentPicName, it.labelIndex)
                    ))
                }
            }
        }
        cLabelPane.setOnLabelHover  handler@{
            when (state.workMode) {
                WorkMode.InputMode -> {
                    cLabelPane.showLabelText(it.labelIndex, it.displayX, it.displayY)
                }
                WorkMode.LabelMode -> {
                    val transLabel = state.transFile.getTransLabel(state.currentPicName, it.labelIndex)
                    val transGroup = state.transFile.groupList[transLabel.groupId]
                    cLabelPane.showText(transGroup.name, transGroup.color, it.displayX, it.displayY)
                }
            }
        }
        cLabelPane.setOnLabelClick  handler@{
            when (state.workMode) {
                WorkMode.InputMode -> {
                    // Update selection
                    cTreeView.selectLabel(it.labelIndex, clear = true, scrollTo = true)
                    // Move to center if double-click
                    if (it.sourceEvent.isDoubleClick) cLabelPane.moveToLabel(it.labelIndex)
                }
                WorkMode.LabelMode -> doNothing()
            }
        }
        cLabelPane.setOnLabelMove   handler@{
            when (state.workMode) {
                WorkMode.InputMode, // Same as in LabelMode
                WorkMode.LabelMode -> doNothing()
            }
            state.doAction(LabelAction(
                ActionType.CHANGE, state,
                state.currentPicName, state.transFile.getTransLabel(state.currentPicName, it.labelIndex),
                newX = it.labelX,
                newY = it.labelY
            ))
        }
        cLabelPane.setOnLabelOther  handler@{
            when (state.workMode) {
                WorkMode.InputMode -> doNothing()
                WorkMode.LabelMode -> {
                    if (state.currentGroupId == NOT_FOUND) return@handler

                    val transGroup = state.transFile.groupList[state.currentGroupId]
                    cLabelPane.showText(transGroup.name, transGroup.color, it.displayX, it.displayY)
                }
            }
        }
        Logger.info("Registered CLabelPane Handler", "Controller")
    }
    /**
     * Properties' bindings
     */
    private fun bind() {
        Logger.info("Binding properties...", "Controller")

        val groupIndexListener = onNew<Number, Int> {
            if (state.viewMode == ViewMode.GroupMode) {
                if (it != NOT_FOUND) {
                    if (cTreeView.isFocused) {
                        // if the change is result of CTreeView selection, add
                        cTreeView.selectGroup(state.transFile.groupList[it].name, clear = false, scrollTo = false)
                    } else {
                        // if the change is result of GroupBar/Box selection, set
                        cTreeView.selectGroup(state.transFile.groupList[it].name, clear = true, scrollTo = true)
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
            if (state.isOpened) {
                if (it != NOT_FOUND) {
                    // PicBox index should never be -1 except when removing a picture
                    state.currentPicName = state.transFile.sortedPicNames[it]
                }
            } else {
                // Closed, do nothing. Let State set current-pic-name to empty string
            }
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
        cLabelPane.imageProperty().bind(imageBinding)
        cLabelPane.labelsProperty().bind(labelsBinding)
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

        // Switch Prism for once (only in windows)
        imageBinding.addListener(onNew {
            // The restore procedure will be registered as a shutdown-hook
            // when Config::usingSWPrism is true, here is next time LPFX starts.
            if (Config.isWin && !Config.usingSWPrism) {
                if (it != null && (it.width >= 4096 || it.height >= 4096)) {
                    val result = showConfirm(state.stage, "Switch to SW? (only once)")
                    if (result.isPresent && result.get() == ButtonType.YES) {
                        state.application.addShutdownHook("UseSWPrism", ::useSoftwarePrism)
                        state.application.stop()
                    } else {
                        showError(state.stage, "Image too Large!")
                    }
                }
            }
        })
        Logger.info("Listened for prism crash", "Controller")

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
        Logger.info("Listened for InfoLabel", "Controller")

        // Listened Tree for Current
        cTreeView.selectedGroupProperty().addListener(onNew<Number, Int> {
            if (it != NOT_FOUND) state.currentGroupId = it
        })
        cTreeView.selectedLabelProperty().addListener(onNew<Number, Int> {
            if (it != NOT_FOUND) state.currentLabelIndex = it
        })
        Logger.info("Listened for selectedGroup/Label", "Controller")

        // Clear selected label when change picture.
        // This could clear the label-index related bindings like TransArea text
        state.currentPicNameProperty().addListener(onChange {
            // If switch picture in CTreeView, the fours on TreeCell will not clear automatically
            //clear selection
            state.currentLabelIndex = NOT_FOUND
            // So we should manually clear it to make sure we start from the first label
             cTreeView.selectFirst(clear = true, scrollTo = false)
             cLabelPane.moveToLabel(cTreeView.selectedLabel)
            // Clear here, because the already happened selection may change it
//            state.currentLabelIndex = NOT_FOUND
        })
        Logger.info("Listened for current-pic-name change for clear label-index selection", "Controller")

        // TextArea Text
        state.currentLabelIndexProperty().addListener(onNew<Number, Int> {
            if (!state.isOpened) return@onNew
            // unbind TextArea
            cTransArea.unbindText()

            if (it == NOT_FOUND) return@onNew
            // bind new text property
            cTransArea.bindText(state.transFile.getTransLabel(state.currentPicName, it).textProperty())
        })
        Logger.info("Listened for label-index change for binding text property", "Controller")

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
        val autoCenterListener = onChange<Number> {
            if (!state.isOpened || !state.getPicFileNow().exists()) cLabelPane.moveToCenter()
        }
        cLabelPane.widthProperty().addListener(autoCenterListener)
        cLabelPane.heightProperty().addListener(autoCenterListener)
        Logger.info("Added effect: default image auto-center", "Controller")

        // Clear text when some state change
        val clearTextListener = onChange<Any> { cLabelPane.clearAllText() }
        state.currentGroupIdProperty().addListener(clearTextListener)
        state.workModeProperty().addListener(clearTextListener)
        Logger.info("Added effect: clear text when some state change", "Controller")

        // Bind Tree and LabelPane
        cTreeView.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            if (it.button == MouseButton.PRIMARY && it.isDoubleClick)
                if (cTreeView.selectedLabel != NOT_FOUND)
                    cLabelPane.moveToLabel(cTreeView.selectedLabel)
        }
        cTreeView.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.UP || it.code == KeyCode.DOWN)
                if (cTreeView.selectedLabel != NOT_FOUND)
                    cLabelPane.moveToLabel(cTreeView.selectedLabel)
        }
        Logger.info("Added effect: move to label on CTreeLabelItem select", "Controller")

        // When LabelPane Box Selection
        cLabelPane.selectedLabelsProperty().addListener(SetChangeListener {
            if (state.isOpened) cTreeView.selectLabels(it.set, clear = true, scrollTo = true)
        })
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED) handler@{
            if (cLabelPane.selectedLabels.isEmpty()) return@handler
            if (it.code == KeyCode.DELETE || it.code == KeyCode.BACK_SPACE) {
                val indices = cLabelPane.selectedLabels.toSortedSet().reversed()

                // Clear selection if current label will be removed
                if (state.currentLabelIndex in indices) state.currentLabelIndex = NOT_FOUND

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
            if (it.code == KeyCode.TAB) {
                // Mark immediately when this event will be consumed
                it.consume() // Disable tab shift

                bSwitchViewMode.fire()
            }
        }
        Logger.info("Transformed Tab on CTreeView", "Controller")

        // Transform tab press in CLabelPane to WorkModeBtn click
        cLabelPane.addEventFilter(KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.TAB) {
                // Mark immediately when this event will be consumed
                it.consume() // Disable tab shift

                bSwitchWorkMode.fire()
            }
        }
        Logger.info("Transformed Tab on CLabelPane", "Controller")

        val changePicHandler = EventHandler<KeyEvent> handler@{
            if (it.isControlDown || it.isMetaDown || it.isShiftDown || it.isAltDown || it.code.isDigitKey)  return@handler
            // Mark immediately when this event will be consumed
            it.consume() // stop further propagation

            when (it.code) {
                KeyCode.Q -> cPicBox.back()
                KeyCode.W -> cPicBox.next()
                else -> return@handler
            }
            cTreeView.selectFirst()
            it.consume() // Consume used event
        }
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED, changePicHandler)
        Logger.info("Transformed A/D", "Controller")

        // Transform number key press to CTreeView select
        val numberBuilder = StringBuilder()
        view.addEventHandler(KeyEvent.KEY_PRESSED) handler@{
            if (!it.code.isDigitKey) {
                numberBuilder.clear()
                return@handler
            }
            // Mark immediately when this event will be consumed
            it.consume() // stop further propagation

            val number = it.text.toInt()
            if (numberBuilder.isEmpty()) {
                // Not parsing
                if (number == 0) {
                    // Start parse
                    numberBuilder.append(0)
                } else if (state.transFileProperty().isNotNull.value && number in 1..state.transFile.groupCount) {
                    // Try select
                    val index = number - 1
                    if (state.viewMode == ViewMode.GroupMode) {
                        cTreeView.selectGroup(state.transFile.groupList[index].name, clear = true, scrollTo = false)
                    } else {
                        state.currentGroupId = index
                    }
                } else {
                    doNothing()
                }
            } else {
                // Parsing
                numberBuilder.append(number)
                val index = numberBuilder.toString().toInt() - 1
                if ( state.transFileProperty().isNotNull.value &&index in 0 until state.transFile.groupCount) {
                    // Try select
                    if (state.viewMode == ViewMode.GroupMode) {
                        cTreeView.selectGroup(state.transFile.groupList[index].name, clear = true, scrollTo = false)
                    } else {
                        state.currentGroupId = index
                    }
                } else {
                    // Reset
                    numberBuilder.clear()
                    if (number == 0) numberBuilder.append(0)
                }
            }
        }
        Logger.info("Transformed num-key pressed", "Controller")

        /**
         * Find next LabelItem as int index.
         * @return NOT_FOUND when have no next
         */
        fun getNextLabelItemIndex(from: Int, direction: Int): Int {
            // Make sure we have items to select
            cTreeView.getTreeItem(from).apply { this?.expand() }

            var index = from + direction
            while (true) {
                val item = cTreeView.getTreeItem(index) ?: return NOT_FOUND
                if (item is CTreeLabelItem) return index

                item.expand()
                index += direction
            }
        }

        fun moveCurrLabelTo(direction: Int) {
            var itemIndex = getNextLabelItemIndex(cTreeView.selectionModel.selectedIndex, direction)
            if (itemIndex == NOT_FOUND) {
                // if selected first and try getting previous, return last;
                // if selected last and try getting next, return first;
                itemIndex = getNextLabelItemIndex(if (direction == 1) 0 else cTreeView.expandedItemCount, direction)
            }
            if(itemIndex == NOT_FOUND) {
                return
            }
            val item = cTreeView.getTreeItem(itemIndex) as CTreeLabelItem

            cLabelPane.moveToLabel(item.transLabel.index)
            cTreeView.selectLabel(item.transLabel.index, clear = true, scrollTo = true)
        }

        // Transform Ctrl + Left/Right KeyEvent to CPicBox button click
        val arrowKeyChangePicHandler = EventHandler<KeyEvent> handler@{
            if (!(it.isControlDown || it.isMetaDown)) return@handler

            when (it.code) {
                KeyCode.LEFT -> cPicBox.back()
                KeyCode.RIGHT -> cPicBox.next()
                else -> return@handler
            }
            cTreeView.selectFirst()
            it.consume() // Consume used event
        }
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangePicHandler)
        cTransArea.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangePicHandler)
        cTreeView.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangePicHandler)
        Logger.info("Transformed Ctrl + Left/Right", "Controller")


        // Transform Ctrl + Up/Down KeyEvent to CTreeView select (and have effect: move to label)
        val arrowKeyChangeLabelHandler = EventHandler<KeyEvent> handler@{
            if (!((it.isControlDown || it.isMetaDown) && it.code.isArrowKey)) return@handler
            // Make sure we'll not get into endless LabelItem find loop
            if (state.transFile.getTransList(state.currentPicName).isEmpty()) return@handler
            // Direction
            val itemShift: Int = when (it.code) {
                KeyCode.UP -> -1
                KeyCode.DOWN -> 1
                else -> return@handler
            }
            // Mark immediately when this event will be consumed
            it.consume() // stop further propagation

            moveCurrLabelTo(itemShift)
        }
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangeLabelHandler)
        cTransArea.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangeLabelHandler)
        Logger.info("Transformed Ctrl + Up/Down", "Controller")

        // Transform Ctrl + Enter to Ctrl + Down / Right (+Shift -> back)
        val enterKeyTransformerHandler = EventHandler<KeyEvent> handler@{
            if (!(it.isControlDown || it.isMetaDown) || it.code != KeyCode.ENTER) return@handler
            // Mark immediately when this event will be consumed
            it.consume() // stop further propagation

            val itemIndex = getNextLabelItemIndex(cTreeView.selectionModel.selectedIndex, if (it.isShiftDown) -1 else 1)
            // transform
            if (it.isShiftDown) {
                // Met the bounds, change picture
                if (itemIndex == NOT_FOUND) {
                    cPicBox.back()
                    cTreeView.selectLast()
                } else {
                    // Go to previous label
                    moveCurrLabelTo(direction = -1)
                }
            } else {
                // Met the bounds, change picture
                if (itemIndex == NOT_FOUND) {
                    cPicBox.next()
                    cTreeView.selectFirst()
                } else {
                    // Go to previous label
                    moveCurrLabelTo(direction = 1)
                }

            }
        }
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED, enterKeyTransformerHandler)
        cTransArea.addEventHandler(KeyEvent.KEY_PRESSED, enterKeyTransformerHandler)
        Logger.info("Transformed Ctrl + Enter", "Controller")



        val copyLabelHandler = EventHandler<KeyEvent> handler@{
            if (!(it.isControlDown || it.isMetaDown)) return@handler
            when (it.code) {
                KeyCode.C -> //                    cTreeView.pasteLabelsText(selectItems.map { it.transLabel.index },state)
                {
                    @Suppress("UNCHECKED_CAST") val treeItem = cTreeView.getTreeItem(cTreeView.selectionModel.selectedIndex) as CTreeLabelItem
                    cTreeView.copyLabelText(treeItem.transLabel.index)
                }
                KeyCode.V -> {
                    @Suppress("UNCHECKED_CAST") val selectItems:Collection<CTreeLabelItem> =
                        cTreeView.selectionModel.selectedIndices.map { cTreeView.getTreeItem(it)}
                            .filter { it is CTreeLabelItem } as List<CTreeLabelItem>
                    cTreeView.pasteLabelsText(selectItems.map { it.transLabel.index },state)
                }
                else ->//                    cTreeView.pasteLabelsText(selectItems.map { it.transLabel.index },state)
                    //                    cTreeView.pasteLabelsText(selectItems.map { it.transLabel.index },state)
                {
                    return@handler
                }
            }
            it.consume() // Consume used event
        }
        cTreeView.addEventHandler(KeyEvent.KEY_PRESSED, copyLabelHandler)
        Logger.info("Transformed Ctrl + C/V", "Controller")
//


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
                if (it.isFile && it.extension.lowercase() in EXTENSIONS_PIC) {
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
                .let { if (FileType.getFileType(file) == FileType.LPFile) it.subList(0, it.size.coerceAtMost(9)) else it },
            transMap = selectedPics.associateWith { emptyList() }
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
        if (file !in RecentFiles.recentFiles) {
            val comment = transFile.comment.trim().replace(Regex("\n(\\s)+"), "\n")
            if (comment !in TransFile.DEFAULT_COMMENT_LIST) {
                Logger.info("Showed modified comment", "Controller")
                showInfo(state.stage, I18N["m.comment.dialog.content"], comment, I18N["common.info"])
            }
        }

        // Update recent files
        RecentFiles.add(file)

        // Auto backup
        backupManager.clear()
        val bakDir = state.getBakFolder()
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

        // Move to center
        if (labelIndex != NOT_FOUND) {
            // NotNow: May throw NoSuchElementException if render not complete
            cTreeView.selectLabel(labelIndex, clear = true, scrollTo = true)
            cLabelPane.moveToLabel(labelIndex)
        }

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
        val exportDest = if (overwrite) File.createTempFile("LPFX", ".${file.extension}").apply(File::deleteOnExit) else file

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
        Logger.info("Recovering from ${from.path}, to ${to.path}", "Controller")

        try {
            export(to, load(from))
        } catch (e: Exception) {
            Logger.error("Recover failed", "Controller")
            Logger.exception(e)
            showError(state.stage, I18N["error.recovery_failed"])
            showException(state.stage, e)
        }
        Logger.info("Recovered", "Controller")

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
     * Backup immediately
     */
    fun emergency(): File? {
        val bak = state.getBakFolder().resolve("emergency.$EXTENSION_BAK")
        try {
            export(bak, state.transFile)
        } catch (e: IOException) {
            return null
        }
        return bak
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

            Platform.runLater {
                if (version > V) {
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
                            Logger.info("Check suppressed, next notice time is ${time + delay}", "Controller")
                        }
                    }
                } else if (showWhenUpdated) {
                    showInfo(this@Controller.state.stage, I18N["update.info.updated"])
                }
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
