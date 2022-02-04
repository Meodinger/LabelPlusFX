package ink.meodinger.lpfx

import ink.meodinger.lpfx.component.*
import ink.meodinger.lpfx.component.common.*
import ink.meodinger.lpfx.component.singleton.AMenuBar
import ink.meodinger.lpfx.component.singleton.ADialogSpecify
import ink.meodinger.lpfx.io.export
import ink.meodinger.lpfx.io.load
import ink.meodinger.lpfx.io.pack
import ink.meodinger.lpfx.options.*
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.dialog.*
import ink.meodinger.lpfx.util.doNothing
import ink.meodinger.lpfx.util.event.*
import ink.meodinger.lpfx.util.file.transfer
import ink.meodinger.lpfx.util.media.playOggList
import ink.meodinger.lpfx.util.platform.TextFont
import ink.meodinger.lpfx.util.property.RuledGenericBidirectionalBinding
import ink.meodinger.lpfx.util.property.onChange
import ink.meodinger.lpfx.util.property.onNew
import ink.meodinger.lpfx.util.property.once
import ink.meodinger.lpfx.util.resource.*
import ink.meodinger.lpfx.util.timer.TimerTaskManager

import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.input.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.DirectoryChooser
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.*
import kotlin.collections.ArrayList


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * Main controller
 */
class Controller(private val root: View) {

    companion object {
        /**
         * Auto-save
         */
        private const val AUTO_SAVE_DELAY = 5 * 60 * 1000L
        private const val AUTO_SAVE_PERIOD = 3 * 60 * 1000L
    }

    private val bSwitchViewMode: Button      = root.bSwitchViewMode does { switchViewMode() }
    private val bSwitchWorkMode: Button      = root.bSwitchWorkMode does { switchWorkMode() }
    private val lInfo: Label                 = root.lInfo
    private val pMain: SplitPane             = root.pMain
    private val pRight: SplitPane            = root.pRight
    private val cGroupBar: CGroupBar         = root.cGroupBar
    private val cLabelPane: CLabelPane       = root.cLabelPane
    private val cSlider: CTextSlider         = root.cSlider
    private val cPicBox: CComboBox<String>   = root.cPicBox
    private val cGroupBox: CComboBox<String> = root.cGroupBox
    private val cTreeView: CTreeView         = root.cTreeView
    private val cTransArea: CLigatureArea    = root.cTransArea

    private val backupManager = TimerTaskManager(AUTO_SAVE_DELAY, AUTO_SAVE_PERIOD) {
        if (State.isChanged) {
            val bak = State.getBakFolder().resolve("${Date().time}.${EXTENSION_BAK}")
            try {
                export(bak, FileType.MeoFile, State.transFile)
            } catch (e: IOException) {
                Logger.error("Auto-backup failed", LOGSRC_CONTROLLER)
                Logger.exception(e)
            }
        }
    }

    private fun switchViewMode() {
        val now = ViewMode.values().indexOf(State.viewMode)
        val all = ViewMode.values().size
        setViewMode(ViewMode.values()[(now + 1) % all])
    }
    private fun switchWorkMode() {
        val now = WorkMode.values().indexOf(State.workMode)
        val all = WorkMode.values().size
        setWorkMode(WorkMode.values()[(now + 1) % all])
    }

    init {
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

        // Drag and Drop
        root.setOnDragOver {
            if (it.dragboard.hasFiles()) it.acceptTransferModes(TransferMode.COPY)
            it.consume() // Consume used event
        }
        root.setOnDragDropped {
            if (stay()) return@setOnDragDropped

            State.reset()

            val board = it.dragboard
            if (board.hasFiles()) {
                val file = board.files.firstOrNull { f -> EXTENSIONS_FILE.contains(f.extension) } ?: return@setOnDragDropped

                // To avoid exception cannot be caught
                Platform.runLater { open(file) }
                it.isDropCompleted = true
            }
            it.consume() // Consume used event
        }
        Logger.info("Enabled Drag and Drop", LOGSRC_CONTROLLER)

        // Global event catch, prevent mnemonic parsing and the beep
        root.addEventHandler(KeyEvent.KEY_PRESSED) { if (it.isAltDown) it.consume() }
        Logger.info("Prevented Alt-Key mnemonic", LOGSRC_CONTROLLER)

        // MenuBar
        root.top = AMenuBar
        Logger.info("Added MenuBar", LOGSRC_CONTROLLER)

        // Last directory
        var lastFilePath = RecentFiles.getLastOpenFile()
        while (lastFilePath != null) {
            val file = File(lastFilePath)
            if (file.exists() && file.parentFile.exists()) {
                CFileChooser.lastDirectory = file.parentFile
                break
            } else {
                RecentFiles.remove(lastFilePath)
                lastFilePath = RecentFiles.getLastOpenFile()
            }
        }
        Logger.info("Set CFileChooser lastDirectory: ${CFileChooser.lastDirectory}", LOGSRC_CONTROLLER)

        // RecentFiles
        AMenuBar.updateRecentFiles()
        Logger.info("Updated RecentFiles Menu", LOGSRC_CONTROLLER)

        // Preferences
        cTransArea.font = Font.font(TextFont, Preference[Preference.TEXTAREA_FONT_SIZE].asDouble())
        pMain.setDividerPositions(Preference[Preference.MAIN_DIVIDER].asDouble())
        pRight.setDividerPositions(Preference[Preference.RIGHT_DIVIDER].asDouble())
        Logger.info("Applied Preferences", LOGSRC_CONTROLLER)

        // Settings
        val viewModes = Settings[Settings.ViewModePreference].asStringList()
        State.viewMode = ViewMode.getViewMode(viewModes[WorkMode.InputMode.ordinal])
        updateLigatureRules()
        Logger.info("Applied Settings", LOGSRC_CONTROLLER)

        // Register handler
        cLabelPane.setOnLabelPlace {
            if (State.workMode != WorkMode.LabelMode) return@setOnLabelPlace
            if (State.transFile.groupCount == 0) return@setOnLabelPlace

            val transLabel = TransLabel(it.labelIndex, State.currentGroupId, it.labelX, it.labelY, "")

            // Edit data
            State.addTransLabel(State.currentPicName, transLabel)
            // Update view
            createLabel(transLabel)
            createLabelTreeItem(transLabel)
            // Mark change
            State.isChanged = true
        }
        cLabelPane.setOnLabelRemove {
            if (State.workMode != WorkMode.LabelMode) return@setOnLabelRemove

            // Edit data
            State.removeTransLabel(State.currentPicName, it.labelIndex)
            for (label in State.transFile.getTransList(State.currentPicName)) {
                if (label.index > it.labelIndex) {
                    State.setTransLabelIndex(State.currentPicName, label.index, label.index - 1)
                }
            }
            // Update view
            removeLabel(it.labelIndex)
            removeLabelTreeItem(it.labelIndex)
            // Mark change
            State.isChanged = true
        }
        cLabelPane.setOnLabelPointed {
            val transLabel = State.transFile.getTransLabel(State.currentPicName, it.labelIndex)

            // Text display
            cLabelPane.removeText()
            when (State.workMode) {
                WorkMode.InputMode -> cLabelPane.createText(transLabel.text, Color.BLACK, it.displayX, it.displayY)
                WorkMode.LabelMode -> {
                    val transGroup = State.transFile.getTransGroup(transLabel.groupId)
                    cLabelPane.createText(transGroup.name, Color.web(transGroup.colorHex), it.displayX, it.displayY)
                }
            }
        }
        cLabelPane.setOnLabelClicked {
            if (State.workMode != WorkMode.InputMode) return@setOnLabelClicked

            if (it.source.isDoubleClick) cLabelPane.moveToLabel(it.labelIndex)

            cTreeView.selectLabel(it.labelIndex, true)
        }
        cLabelPane.setOnLabelMove {
            State.isChanged = true
        }
        cLabelPane.setOnLabelOther {
            if (State.workMode != WorkMode.LabelMode) return@setOnLabelOther
            if (State.transFile.groupCount == 0) return@setOnLabelOther

            val transGroup = State.transFile.getTransGroup(State.currentGroupId)

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

        // Set components disabled
        bSwitchViewMode.disableProperty().bind(!State.isOpenedProperty)
        bSwitchWorkMode.disableProperty().bind(!State.isOpenedProperty)
        cTransArea.disableProperty().bind(!State.isOpenedProperty)
        cTreeView.disableProperty().bind(!State.isOpenedProperty)
        cPicBox.disableProperty().bind(!State.isOpenedProperty)
        cGroupBox.disableProperty().bind(!State.isOpenedProperty)
        cSlider.disableProperty().bind(!State.isOpenedProperty)
        cLabelPane.disableProperty().bind(!State.isOpenedProperty)
        Logger.info("Bound disabled", LOGSRC_CONTROLLER)

        // CSlider - CLabelPane#scale
        cSlider.initScaleProperty().bindBidirectional(cLabelPane.initScaleProperty())
        cSlider.minScaleProperty().bindBidirectional(cLabelPane.minScaleProperty())
        cSlider.maxScaleProperty().bindBidirectional(cLabelPane.maxScaleProperty())
        cSlider.scaleProperty().bindBidirectional(cLabelPane.scaleProperty())
        Logger.info("Bound scale", LOGSRC_CONTROLLER)

        // Switch Button text
        bSwitchWorkMode.textProperty().bind(Bindings.createStringBinding(binding@{
            labelInfo("Switched work mode to ${State.viewMode}")
            return@binding when (State.workMode) {
                WorkMode.InputMode -> I18N["mode.work.input"]
                WorkMode.LabelMode -> I18N["mode.work.label"]
            }
        }, State.workModeProperty))
        bSwitchViewMode.textProperty().bind(Bindings.createStringBinding(binding@{
            labelInfo("Switched view mode to ${State.viewMode}")
            return@binding when (State.viewMode) {
                ViewMode.IndexMode -> I18N["mode.view.index"]
                ViewMode.GroupMode -> I18N["mode.view.group"]
            }
        }, State.viewModeProperty))
        Logger.info("Bound switch button text", LOGSRC_CONTROLLER)

        // PictureBox - CurrentPicName
        cPicBox.itemsProperty().bind(object : ObjectBinding<ObservableList<String>>() {
            private var lastMapObservable = State.transFile.transMapObservable

            init {
                // When switch to new TransFile, update
                bind(State.transFileProperty)
            }

            override fun computeValue(): ObservableList<String> {
                // Abandon the ObservableValue of last TransFile
                unbind(lastMapObservable)

                // Get new ObservableValue
                lastMapObservable = State.transFile.transMapObservable

                // Bind to it
                bind(State.transFile.transMapObservable)

                return FXCollections.observableList(State.transFile.sortedPicNames)
            }

        })
        RuledGenericBidirectionalBinding.bind(
            cPicBox.valueProperty(), a@{ observable, _, newValue, _ ->
                val a = newValue ?: if (State.isOpened) State.transFile.sortedPicNames[0] else ""

                // Indicate current item was removed
                // Use run later to avoid Issue#5 (Reason unclear).
                // Check opened to avoid accidentally set "Close time empty str" to "Open time pic"
                if (State.isOpened && newValue == null) Platform.runLater { observable.value = a }

                return@a a
            },
            State.currentPicNameProperty, { _, _, newValue, _ -> newValue!! }
        )
        Logger.info("Bound PicBox & CurrentPicName", LOGSRC_CONTROLLER)

        // GroupBox - CurrentGroupId
        cGroupBox.itemsProperty().bind(object : ObjectBinding<ObservableList<String>>() {
            private var lastGroupListObservable = State.transFile.groupListObservable
            private val boundGroupNameProperties = ArrayList<StringProperty>()

            init {
                // When switch to new TransFile, update
                bind(State.transFileProperty)
            }

            override fun computeValue(): ObservableList<String> {
                // Abandon the ObservableValue of last TransFile
                unbind(lastGroupListObservable)
                for (property in boundGroupNameProperties) unbind(property)
                boundGroupNameProperties.clear()

                // Get new ObservableValue
                lastGroupListObservable = State.transFile.groupListObservable
                for (group in State.transFile.groupListObservable) boundGroupNameProperties.add(group.nameProperty)

                // Bind to it
                bind(State.transFile.groupListObservable)
                for (property in boundGroupNameProperties) bind(property)

                return FXCollections.observableArrayList(State.transFile.groupNames)
            }
        })
        RuledGenericBidirectionalBinding.bind(
            cGroupBox.indexProperty(), { observable, _, newValue, _ ->
                val n = newValue as Int
                val a = if (n != NOT_FOUND) n else if (State.isOpened) 0 else NOT_FOUND

                // Indicate current item was removed
                // Use run later to avoid Issue#5 (Reason unclear).
                // Check opened to avoid accidentally set "Close time -1" to "Open time index"
                if (State.isOpened && n == NOT_FOUND) Platform.runLater { observable.value = a }

                a
            },
            State.currentGroupIdProperty, { _, _, newValue, _ -> newValue!! }
        )
        Logger.info("Bound GroupBox & CurrentGroupId", LOGSRC_CONTROLLER)

        // TreeView
        cTreeView.picNameProperty().bind(State.currentPicNameProperty)
        cTreeView.viewModeProperty().bind(State.viewModeProperty)
        Logger.info("Bound CTreeView properties", LOGSRC_CONTROLLER)

        // LabelPane
        cLabelPane.colorHexListProperty().bind(object : ObjectBinding<ObservableList<String>>() {
            private var lastGroupListObservable = State.transFile.groupListObservable
            private val boundGroupHexProperties = ArrayList<StringProperty>()

            init {
                // When switch to new TransFile, update
                bind(State.transFileProperty)
            }

            override fun computeValue(): ObservableList<String> {
                // Abandon the ObservableValue of last TransFile
                unbind(lastGroupListObservable)
                for (property in boundGroupHexProperties) unbind(property)
                boundGroupHexProperties.clear()

                // Get new ObservableValue
                lastGroupListObservable = State.transFile.groupListObservable
                for (group in State.transFile.groupListObservable) boundGroupHexProperties.add(group.colorHexProperty)

                // Bind to it
                bind(State.transFile.groupListObservable)
                for (property in boundGroupHexProperties) bind(property)

                return FXCollections.observableArrayList(State.transFile.groupColors)
            }
        })
        cLabelPane.defaultCursorProperty().bind(Bindings.createObjectBinding(binding@{
            return@binding when (State.workMode) {
                WorkMode.LabelMode -> Cursor.CROSSHAIR
                WorkMode.InputMode -> Cursor.DEFAULT
            }
        }, State.workModeProperty))
        Logger.info("Bound CLabelPane properties", LOGSRC_CONTROLLER)
    }
    /**
     * Properties' listeners (for unbindable)
     */
    private fun listen() {
        Logger.info("Attaching Listeners...", LOGSRC_CONTROLLER)

        // Default image auto-center
        cLabelPane.widthProperty().addListener(onChange {
            if (!State.isOpened || !State.getPicFileNow().exists()) cLabelPane.moveToCenter()
        })
        cLabelPane.heightProperty().addListener(onChange {
            if (!State.isOpened || !State.getPicFileNow().exists()) cLabelPane.moveToCenter()
        })
        Logger.info("Listened for default image location", LOGSRC_CONTROLLER)

        // currentLabelIndex
        State.currentPicNameProperty.addListener(onChange {
            // Clear selected when change pic
            State.currentLabelIndex = NOT_FOUND
        })
        cTreeView.selectionModel.selectedItemProperty().addListener(onNew {
            if (it != null && it is CTreeLabelItem) State.currentLabelIndex = it.index
        })
        Logger.info("Listened for CurrentLabelIndex", LOGSRC_CONTROLLER)

        // isChanged
        cTransArea.textProperty().addListener(onChange {
            if (cTransArea.isBound) State.isChanged = true
        })
        Logger.info("Listened for isChanged", LOGSRC_CONTROLLER)

        // Preferences
        cTransArea.fontProperty().addListener(onNew { Preference[Preference.TEXTAREA_FONT_SIZE] = it.size.toInt() })
        pMain.dividers[0].positionProperty().addListener(onNew(Preference[Preference.MAIN_DIVIDER]::set))
        pRight.dividers[0].positionProperty().addListener(onNew(Preference[Preference.RIGHT_DIVIDER]::set))
        Logger.info("Listened for Preferences", LOGSRC_CONTROLLER)
    }
    /**
     * Properties' effect on view
     */
    private fun effect() {
        Logger.info("Applying Affections...", LOGSRC_CONTROLLER)

        // Update cTreeView & cLabelPane when pic change
        State.currentPicNameProperty.addListener(onNew {
            if (!State.isOpened || it.isEmpty()) return@onNew

            renderTreeView()
            renderLabelPane()

            labelInfo("Changed picture to $it")
        })
        Logger.info("Added effect on CurrentPicName change", LOGSRC_CONTROLLER)

        // Clear text layer & re-select CGroup when group change
        State.currentGroupIdProperty.addListener(onNew<Number, Int> {
            if (!State.isOpened) return@onNew

            // Remove text
            cLabelPane.removeText()

            if (it == NOT_FOUND) return@onNew

            // Select GroupBar, CTreeView
            val name = State.transFile.getTransGroup(it).name
            if (State.viewMode != ViewMode.IndexMode) cTreeView.selectGroup(name, false)
            cGroupBar.select(name)

            labelInfo("Selected group to $name")
        })
        Logger.info("Added effect on CurrentGroupId change", LOGSRC_CONTROLLER)

        // Update text area when label change
        State.currentLabelIndexProperty.addListener(onNew<Number, Int> {
            if (!State.isOpened) return@onNew

            // unbind TextArea
            cTransArea.unbindBidirectional()

            if (it == NOT_FOUND) return@onNew

            // bind new text property
            val transLabels = State.transFile.getTransList(State.currentPicName)
            val newLabel = transLabels.find { label -> label.index == it }
            if (newLabel != null) cTransArea.bindBidirectional(newLabel.textProperty)

            labelInfo("Selected label $it")
        })
        Logger.info("Added effect on CurrentLabelIndex change", LOGSRC_CONTROLLER)

        // Bind Ctrl/Alt/Meta + Scroll with font size change
        cTransArea.addEventHandler(ScrollEvent.SCROLL) {
            if (!(it.isControlOrMetaDown || it.isAltDown)) return@addEventHandler

            val newSize = (cTransArea.font.size + it.deltaY / SCROLL_DELTA).toInt()
                .coerceAtLeast(FONT_SIZE_MIN).coerceAtMost(FONT_SIZE_MAX)

            cTransArea.font = Font.font(TextFont, newSize.toDouble())
            cTransArea.positionCaret(0)

            labelInfo("Set text font size to $newSize")
        }
        Logger.info("Added effect on Ctrl/Alt/Meta + Scroll", LOGSRC_CONTROLLER)

        // Bind Label and Tree
        cTreeView.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            if (it.button != MouseButton.PRIMARY) return@addEventHandler
            if (!it.isDoubleClick) return@addEventHandler

            val item = cTreeView.selectionModel.selectedItem
            if (item != null && item is CTreeLabelItem) cLabelPane.moveToLabel(item.index)
        }
        cTreeView.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (!it.code.isArrowKey) return@addEventHandler
            val direction = when (it.code) {
                KeyCode.UP -> -1
                KeyCode.DOWN -> 1
                else -> return@addEventHandler
            }

            val item = cTreeView.getTreeItem(cTreeView.selectionModel.selectedIndex + direction)
            if (item != null && item is CTreeLabelItem) cLabelPane.moveToLabel(item.index)
        }
        Logger.info("Added effect on CTreeLabelItem selected", LOGSRC_CONTROLLER)
    }
    /**
     * Transformations
     */
    private fun transform() {
        Logger.info("Applying Transformations...", LOGSRC_CONTROLLER)

        // Transform CTreeView group selection to CGroupBox select
        cTreeView.selectionModel.selectedItemProperty().addListener(onNew {
            if (it != null && it is CTreeGroupItem) cGroupBox.select(State.transFile.getGroupIdByName(it.name))
        })
        Logger.info("Transformed CTreeGroupItem selected", LOGSRC_CONTROLLER)

        // Transform tab press in CTreeView to ViewModeBtn click
        cTreeView.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (it.code != KeyCode.TAB) return@addEventHandler

            switchViewMode()
            it.consume() // Disable tab shift
        }
        Logger.info("Transformed Tab on CTreeView", LOGSRC_CONTROLLER)

        // Transform tab press in CLabelPane to WorkModeBtn click
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (it.code != KeyCode.TAB) return@addEventHandler

            cLabelPane.removeText()
            switchWorkMode()
            it.consume() // Disable tab shift
        }
        Logger.info("Transformed Tab on CLabelPane", LOGSRC_CONTROLLER)

        // Transform number key press to CGroupBox select
        root.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (!it.code.isDigitKey) return@addEventHandler

            val index = it.text.toInt() - 1
            if (index < 0 || index >= cGroupBox.items.size) return@addEventHandler
            cGroupBox.select(index)
        }
        Logger.info("Transformed num-key pressed", LOGSRC_CONTROLLER)

        // Transform CGroup select to CGroupBox select
        cGroupBar.setOnGroupSelect { cGroupBox.select(it) }
        Logger.info("Transformed CGroupBar selected", LOGSRC_CONTROLLER)

        // Transform Ctrl + Left/Right KeyEvent to CPicBox button click
        val arrowKeyChangePicHandler = EventHandler<KeyEvent> {
            if (!(it.isControlOrMetaDown && it.code.isArrowKey)) return@EventHandler

            when (it.code) {
                KeyCode.LEFT -> cPicBox.back()
                KeyCode.RIGHT -> cPicBox.next()
                else -> return@EventHandler
            }

            it.consume() // Consume used event
        }
        root.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangePicHandler)
        cTransArea.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangePicHandler)
        Logger.info("Transformed Ctrl + Left/Right", LOGSRC_CONTROLLER)

        // Transform Ctrl + Up/Down KeyEvent to CTreeView select
        /**
         * Find next LabelItem as int index.
         * @return NOT_FOUND when have no next
         */
        fun getNextLabelItemIndex(from: Int, direction: Int): Int {
            // Make sure we have items to select
            cTreeView.getTreeItem(from).apply { this?.expandAll() }

            var index = from
            var item: TreeItem<String>?
            do {
                index += direction
                item = cTreeView.getTreeItem(index)
                item?.expandAll()

                if (item == null) return NOT_FOUND
            } while (item !is CTreeLabelItem)

            return index
        }
        val arrowKeyChangeLabelHandler = EventHandler<KeyEvent> {
            if (!(it.isControlOrMetaDown && it.code.isArrowKey)) return@EventHandler

            // Make sure we'll not get into endless LabelItem find loop
            if (State.transFile.getTransList(State.currentPicName).isEmpty()) return@EventHandler

            val shift = when (it.code) {
                KeyCode.UP -> -1
                KeyCode.DOWN -> 1
                else -> return@EventHandler
            }

            val selectedIndex = cTreeView.selectionModel.selectedIndex
            var labelItemIndex = when (it.code) {
                // if not selected, return first or last;
                KeyCode.UP   -> shift + if (selectedIndex != NOT_FOUND) selectedIndex else cTreeView.expandedItemCount
                KeyCode.DOWN -> shift + if (selectedIndex != NOT_FOUND) selectedIndex else 0
                else -> throw IllegalStateException("Should not be here")
            }

            var item: TreeItem<String>? = cTreeView.getTreeItem(labelItemIndex)
            while (item !is CTreeLabelItem) {
                // if selected first and try getting previous, return last;
                // if selected last and try getting next, return first;
                labelItemIndex = getNextLabelItemIndex(when (it.code) {
                    KeyCode.UP   -> if (labelItemIndex != NOT_FOUND) labelItemIndex else cTreeView.expandedItemCount
                    KeyCode.DOWN -> if (labelItemIndex != NOT_FOUND) labelItemIndex else 0
                    else -> throw IllegalStateException("Should not be here")
                }, shift)
                item = cTreeView.getTreeItem(labelItemIndex)
            }

            if (labelItemIndex == NOT_FOUND) return@EventHandler

            cLabelPane.moveToLabel((cTreeView.getTreeItem(labelItemIndex) as CTreeLabelItem).index)
            cTreeView.selectionModel.clearSelection()
            cTreeView.selectionModel.select(labelItemIndex)
            cTreeView.scrollTo(labelItemIndex)

            it.consume() // Consume used event
        }
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangeLabelHandler)
        cTransArea.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangeLabelHandler)
        Logger.info("Transformed Ctrl + Up/Down", LOGSRC_CONTROLLER)

        // Transform Ctrl + Enter to Ctrl + Down / Right (+Shift -> back)
        val enterKeyTransformerHandler = EventHandler<KeyEvent> {
            if (!(it.isControlOrMetaDown && it.code == KeyCode.ENTER)) return@EventHandler

            val isShiftDown = it.isShiftDown
            val selectedItemIndex = cTreeView.selectionModel.selectedIndex
            val nextLabelItemIndex = getNextLabelItemIndex(selectedItemIndex, if (isShiftDown) -1 else 1)

            val code = if (nextLabelItemIndex == NOT_FOUND) {
                if (isShiftDown) KeyCode.LEFT else KeyCode.RIGHT
            } else {
                if (isShiftDown) KeyCode.UP else KeyCode.DOWN
            }

            cLabelPane.fireEvent(keyEvent(it, character = "\u0000", text = "", code = code))
            when (code) {
                KeyCode.LEFT  -> cLabelPane.fireEvent(keyEvent(it, character = "\u0000", text = "", code = KeyCode.UP))
                KeyCode.RIGHT -> cLabelPane.fireEvent(keyEvent(it, character = "\u0000", text = "", code = KeyCode.DOWN))
                else -> doNothing()
            }

            it.consume() // Consume used event
        }
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED, enterKeyTransformerHandler)
        cTransArea.addEventHandler(KeyEvent.KEY_PRESSED, enterKeyTransformerHandler)
        Logger.info("Transformed Ctrl + Enter", LOGSRC_CONTROLLER)
    }

    /**
     * Specify pictures of current translation file
     * @return True if submitted, false if cancelled
     */
    fun specifyPicFiles(): Boolean {
        val picFiles = ADialogSpecify.specify()

        // Closed or Cancelled
        if (picFiles.isEmpty()) {
            showInfo(State.stage, I18N["specify.info.cancelled"])
            return false
        }

        val picCount = State.transFile.picCount
        val picNames = State.transFile.sortedPicNames
        var uncompleted = false
        for (i in 0 until picCount) {
            val picFile = picFiles[i]
            if (!picFile.exists()) {
                uncompleted = true
                continue
            }
            State.transFile.setFile(picNames[i], picFile)
        }
        if (uncompleted) showInfo(State.stage, I18N["specify.info.incomplete"])

        // Re-render picture
        if (State.isOpened) if (State.getPicFileNow().exists()) renderLabelPane()
        return true
    }

    /**
     * Whether stay here or not
     */
    fun stay(): Boolean {
        // Not open
        if (!State.isOpened) return false
        // Opened but saved
        if (!State.isChanged) return false

        // Opened but not saved
        val result = showAlert(State.stage, null, I18N["alert.not_save.content"], I18N["common.exit"])
        // Dialog present
        if (result.isPresent) when (result.get()) {
            ButtonType.YES -> {
                save(State.translationFile, silent = true)
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
        val selectedPics = ArrayList<String>()
        while (potentialPics.isEmpty()) {
            // Find pictures
            val files = projectFolder.listFiles()
            if (files != null) for (f in files) if (f.isFile) {
                for (extension in EXTENSIONS_PIC) if (f.extension == extension) {
                    potentialPics.add(f.name)
                }
            }
            if (potentialPics.isEmpty()) {
                // Find nothing, this folder isn't project folder, confirm to ues another folder
                val result = showConfirm(State.stage, I18N["confirm.project_folder_invalid"])
                if (result.isPresent && result.get() == ButtonType.YES) {
                    // Specify project folder
                    val newFolder = DirectoryChooser().also { it.initialDirectory = projectFolder }.showDialog(State.stage)
                    if (newFolder != null) projectFolder = newFolder
                } else {
                    // Do not specify, cancel
                    Logger.info("Cancel (project folder invalid)", LOGSRC_CONTROLLER)
                    showInfo(State.stage, I18N["common.cancel"])
                    return null
                }
            } else {
                // Find some pics, continue procedure
                State.projectFolder = projectFolder
                Logger.info("Project folder set to ${projectFolder.path}", LOGSRC_CONTROLLER)
            }
        }
        val result = showChoiceList(State.stage, potentialPics)
        if (result.isPresent) {
            if (result.get().isEmpty()) {
                Logger.info("Cancel (choose none)", LOGSRC_CONTROLLER)
                showInfo(State.stage, I18N["info.required_at_least_1_pic"])
                return null
            }
            selectedPics.addAll(result.get())
        } else {
            Logger.info("Cancel (no selected)", LOGSRC_CONTROLLER)
            showInfo(State.stage, I18N["common.cancel"])
            return null
        }

        // Prepare new TransFile
        val groupList = ArrayList<TransGroup>()
        val groupNameList = Settings[Settings.DefaultGroupNameList].asStringList()
        val groupColorList = Settings[Settings.DefaultGroupColorHexList].asStringList()
        val groupCreateList = Settings[Settings.IsGroupCreateOnNewTrans].asBooleanList()
        for (i in groupNameList.indices) if (groupCreateList[i])
            groupList.add(TransGroup(groupNameList[i], groupColorList[i]))
        val transMap = HashMap<String, MutableList<TransLabel>>()
        for (pic in selectedPics) transMap[pic] = ArrayList()

        val transFile = TransFile(TransFile.DEFAULT_VERSION, TransFile.DEFAULT_COMMENT, groupList, transMap)

        // Export to file
        try {
            export(file, type, transFile)
        } catch (e: IOException) {
            Logger.error("New failed", LOGSRC_CONTROLLER)
            Logger.exception(e)
            showError(State.stage, I18N["error.new_failed"])
            showException(State.stage, e)
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
        Logger.info("Opening ${file.path}", LOGSRC_CONTROLLER)

        // Load File
        val transFile: TransFile
        try {
            transFile = load(file, type)
            // We assume that all pics are in the project folder.
            // If not, TransFile.checkLost() will find them out.
            for (picName in transFile.picNames) {
                transFile.setFile(picName, projectFolder.resolve(picName))
            }
        } catch (e: IOException) {
            Logger.error("Open failed", LOGSRC_CONTROLLER)
            Logger.exception(e)
            showError(State.stage, I18N["error.open_failed"])
            showException(State.stage, e)
            return
        }

        // Opened, update State
        State.transFile = transFile
        State.translationFile = file
        State.projectFolder = projectFolder
        State.isOpened = true

        // Show info if comment not in default list
        // Should do this before update RecentFiles
        if (!RecentFiles.getAll().contains(file.path)) {
            var modified = true
            val comment = transFile.comment.trim()
            for (defaultComment in TransFile.DEFAULT_COMMENT_LIST) {
                if (comment == defaultComment) {
                    modified = false
                    break
                }
            }
            if (modified) {
                Logger.info("Showed modified comment", LOGSRC_CONTROLLER)
                showInfo(State.stage, I18N["m.comment.dialog.content"], comment, I18N["common.info"])
            }
        }

        // Update recent files
        CFileChooser.lastDirectory = file.parentFile
        RecentFiles.add(file.path)
        AMenuBar.updateRecentFiles()

        // Auto backup
        backupManager.clear()
        val bakDir = State.getBakFolder()
        if ((bakDir.exists() && bakDir.isDirectory) || bakDir.mkdir()) {
            backupManager.schedule()
            Logger.info("Scheduled auto-backup", LOGSRC_CONTROLLER)
        } else {
            Logger.warning("Auto-backup unavailable", LOGSRC_CONTROLLER)
            showWarning(State.stage, I18N["warning.auto_backup_unavailable"])
        }

        // Check lost
        if (State.transFile.checkLost().isNotEmpty()) {
            // Specify now?
            showConfirm(State.stage, I18N["specify.confirm.lost_pictures"]).ifPresent {
                if (it == ButtonType.YES) specifyPicFiles()
            }
        }

        // Initialize workspace
        renderGroupBar()
        State.currentPicName = State.transFile.sortedPicNames[0]
        State.currentGroupId = 0

        // Change title
        State.stage.title = INFO["application.name"] + " - " + file.name

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
        if (!silent) if (file.parentFile != State.projectFolder) {
            val confirm = showConfirm(State.stage, I18N["confirm.save_to_another_place"])
            if (!(confirm.isPresent && confirm.get() == ButtonType.YES)) return
        }

        // Use temp if overwrite
        val exportDest = if (overwrite) File("${file.path}.temp") else file
        fun removeTemp() {
            try {
                Files.delete(exportDest.toPath())
                Logger.info("Temp file removed", LOGSRC_CONTROLLER)
            } catch (e: Exception) {
                Logger.warning("Temp file remove failed", LOGSRC_CONTROLLER)
                if (!silent) showWarning(State.stage, String.format(I18N["warning.save_temp_remove_failed.s"], exportDest.path))
            }
        }

        // Export
        try {
            export(exportDest, type, State.transFile)
            if (!silent) showInfo(State.stage, I18N["info.saved_successfully"])
            Logger.info("Exported translation", LOGSRC_CONTROLLER)
        } catch (e: IOException) {
            Logger.error("Export translation failed", LOGSRC_CONTROLLER)
            Logger.exception(e)
            showError(State.stage, I18N["error.save_failed"])
            showException(State.stage, e)

            // Delete the temp file
            if (overwrite) removeTemp()

            Logger.info("Save failed", LOGSRC_CONTROLLER)
            return
        }

        if (overwrite) {
            // Transfer to origin file if export success
            try {
                transfer(exportDest, file)
                Logger.info("Transferred temp file", LOGSRC_CONTROLLER)
            } catch (e: Exception) {
                Logger.error("Transfer temp file failed", LOGSRC_CONTROLLER)
                Logger.exception(e)
                showError(State.stage, I18N["error.save_temp_transfer_failed"])
                showException(State.stage, e)

                Logger.info("Save failed", LOGSRC_CONTROLLER)
                return
            }

            // Delete the temp file
            removeTemp()
        }

        // Update state
        State.translationFile = file
        State.isChanged = false

        // Change title
        State.stage.title = INFO["application.name"] + " - " + file.name

        Logger.info("Saved to ${file.path}", LOGSRC_CONTROLLER)
    }
    /**
     * Recover from backup file
     * @param from The backup file
     * @param to Which file will the backup recover to
     */
    fun recovery(from: File, to: File, type: FileType = FileType.getFileType(to)) {
        Logger.info("Recovering from ${from.path}", LOGSRC_CONTROLLER)

        try {
            val tempFile = File.createTempFile("temp", type.name).also { it.deleteOnExit() }
            val transFile = load(from, FileType.MeoFile)

            export(tempFile, type, transFile)
            transfer(tempFile, to)

            Logger.info("Recovered to ${to.path}", LOGSRC_CONTROLLER)
        } catch (e: Exception) {
            Logger.error("Recover failed", LOGSRC_CONTROLLER)
            Logger.exception(e)
            showError(State.stage, I18N["error.recovery_failed"])
            showException(State.stage, e)
        }

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
            export(file, type, State.transFile)

            Logger.info("Exported to ${file.path}", LOGSRC_CONTROLLER)
            showInfo(State.stage, I18N["info.exported_successful"])
        } catch (e: IOException) {
            Logger.error("Export failed", LOGSRC_CONTROLLER)
            Logger.exception(e)
            showError(State.stage, I18N["error.export_failed"])
            showException(State.stage, e)
        }
    }
    /**
     * Generate a zip file with translation file and picture files
     * @param file Which file will the zip file write to
     */
    fun pack(file: File) {
        Logger.info("Packing to ${file.path}", LOGSRC_CONTROLLER)

        try {
            pack(file, State.transFile)

            Logger.info("Packed to ${file.path}", LOGSRC_CONTROLLER)
            showInfo(State.stage, I18N["info.exported_successful"])
        } catch (e : IOException) {
            Logger.error("Pack failed", LOGSRC_CONTROLLER)
            Logger.exception(e)
            showError(State.stage, I18N["error.export_failed"])
            showException(State.stage, e)
        }
    }

    fun reset() {
        backupManager.clear()

        // cSlider
        // cPicBox
        // cGroupBox
        cGroupBar.reset()
        cLabelPane.reset()
        cTreeView.reset()
        cTransArea.reset()

        State.stage.title = INFO["application.name"]

        labelInfo(I18N["common.ready"])
    }

    // ----- Update component properties ----- //
    fun updateLigatureRules() {
        cTransArea.ligatureRules = FXCollections.observableList(Settings[Settings.LigatureRules].asPairList())
    }

    // ----- GroupBar ----- //
    fun renderGroupBar() {
        cGroupBar.reset()
        cGroupBar.render(State.transFile.groupListObservable)
        cGroupBar.select(if (State.currentGroupId == NOT_FOUND) 0 else State.currentGroupId)

        Logger.info("Group bar rendered", LOGSRC_CONTROLLER)
    }

    fun createGroupBarItem(transGroup: TransGroup) {
        cGroupBar.createGroup(transGroup)

        Logger.info("Created CGroup @ $transGroup", LOGSRC_CONTROLLER)
    }
    fun removeGroupBarItem(groupName: String) {
        cGroupBar.removeGroup(groupName)

        Logger.info("Removed CGroup @ $groupName", LOGSRC_CONTROLLER)
    }

    // ----- LabelPane ----- //
    fun renderLabelPane() {
        try {
            val picFile = State.getPicFileNow()
            if (picFile.exists()) {
                val image = Image(picFile.toURI().toURL().toString())
                cLabelPane.render(
                    image,
                    State.transFile.groupCount,
                    State.transFile.getTransList(State.currentPicName)
                )
            } else {
                cLabelPane.scale = cLabelPane.initScale
                cLabelPane.render(null, 0, emptyList())
                cLabelPane.moveToCenter()

                Logger.error("Picture `${State.currentPicName}` not exists", LOGSRC_CONTROLLER)
                showError(State.stage, String.format(I18N["error.picture_not_exists.s"], State.currentPicName))
                return
            }
        } catch (e: IOException) {
            Logger.error("LabelPane render failed", LOGSRC_CONTROLLER)
            Logger.exception(e)
            showException(State.stage, e)
            return
        }

        when (Settings[Settings.ScaleOnNewPicture].asInteger()) {
            Settings.NEW_PIC_SCALE_100  -> cLabelPane.scale = 1.0 // 100%
            Settings.NEW_PIC_SCALE_FIT  -> cLabelPane.fitToPane() // Fit
            Settings.NEW_PIC_SCALE_LAST -> doNothing() // Last
        }

        cLabelPane.moveToZero()
        Logger.info("LabelPane rendered pic: ${State.currentPicName}", LOGSRC_CONTROLLER)
    }

    fun createLabelLayer() {
        cLabelPane.createLabelLayer()

        Logger.info("Created label layer @ ${State.transFile.groupCount - 1}", LOGSRC_CONTROLLER)
    }
    fun removeLabelLayer(groupId: Int) {
        cLabelPane.removeLabelLayer(groupId)

        Logger.info("Removed label layer @ $groupId", LOGSRC_CONTROLLER)
    }
    fun createLabel(transLabel: TransLabel) {
        cLabelPane.createLabel(transLabel)

        Logger.info("Created label @ $transLabel", LOGSRC_CONTROLLER)
    }
    fun removeLabel(labelIndex: Int) {
        cLabelPane.removeLabel(labelIndex)

        Logger.info("Removed label @ $labelIndex", LOGSRC_CONTROLLER)
    }

    // ----- TreeView ----- //
    fun renderTreeView() {
        // PicName and ViewMode were bound
        cTreeView.render(
            transGroups = State.transFile.groupListObservable,
            transLabels = State.transFile.getTransList(State.currentPicName),
        )

        Logger.info("TreeView rendered pic: ${State.currentPicName}", LOGSRC_CONTROLLER)
    }

    fun createGroupTreeItem(transGroup: TransGroup) {
        when (State.viewMode) {
            ViewMode.IndexMode -> cTreeView.registerGroup(transGroup)
            ViewMode.GroupMode -> cTreeView.createGroupItem(transGroup)
        }

        Logger.info("Created group item @ $transGroup", LOGSRC_CONTROLLER)
    }
    fun removeGroupTreeItem(groupName: String) {
        when (State.viewMode) {
            ViewMode.IndexMode -> cTreeView.unregisterGroup(groupName)
            ViewMode.GroupMode -> cTreeView.removeGroupItem(groupName)
        }

        Logger.info("Removed group item @ $groupName", LOGSRC_CONTROLLER)
    }
    fun createLabelTreeItem(transLabel: TransLabel) {
        cTreeView.createLabelItem(transLabel)

        Logger.info("Created label item @ $transLabel", LOGSRC_CONTROLLER)
    }
    fun removeLabelTreeItem(labelIndex: Int) {
        cTreeView.removeLabelItem(labelIndex)

        Logger.info("Removed label item @ $labelIndex", LOGSRC_CONTROLLER)
    }
    fun moveLabelTreeItem(labelIndex: Int, from: Int, to: Int) {
        cTreeView.moveLabelItem(labelIndex, from, to)

        Logger.info("Moved label item @ $labelIndex @ from=$from, to=$to", LOGSRC_CONTROLLER)
    }

    // ----- Mode ----- //
    fun setViewMode(mode: ViewMode) {
        State.viewMode = mode

        Logger.info("Switched view mode to $mode", LOGSRC_CONTROLLER)
    }
    fun setWorkMode(mode: WorkMode) {
        State.workMode = mode

        setViewMode(when (mode) {
            WorkMode.InputMode -> ViewMode.getViewMode(Settings[Settings.ViewModePreference].asStringList()[0])
            WorkMode.LabelMode -> ViewMode.getViewMode(Settings[Settings.ViewModePreference].asStringList()[1])
        })

        Logger.info("Switched work mode to $mode", LOGSRC_CONTROLLER)
    }

    // ----- Info ----- //
    fun labelInfo(info: String) {
        lInfo.text = info
    }

    // ----- EXTRA ----- //
    fun justMonika() {
        // Write "love you" to comment, once a time
        fun loveYouForever() {
            State.transFile.comment = "I Love You Forever  --Yours, Monika"
            save(State.translationFile, silent = true)

            val monika = Options.profileDir.resolve("monika.json").toFile()
            save(monika, FileType.MeoFile, true)
        }
        if (State.isOpened) loveYouForever()

        // Force all text change to "JUST MONIKA"
        fun textReformat() {
            val monika = "JUST MONIKA "
            for (picName in State.transFile.picNames)
                for (transLabel in State.transFile.getTransList(picName))
                    transLabel.text = monika

            val chars = monika.toCharArray()
            cTransArea.textFormatter = TextFormatter<String> {
                if (!(it.control as CLigatureArea).isBound) return@TextFormatter it

                val end = cTransArea.text.length
                val last = it.controlText.last()
                val index = if (last == ' ') {
                    if (it.controlText[it.controlText.length - 2] == 'A') 0 else 5
                } else {
                    chars.indexOf(last) + 1
                }

                it.setRange(end, end)
                it.text = chars[(index) % chars.size].toString()
                it.anchor = end + 1
                it.caretPosition = end + 1

                it
            }
        }
        if (State.isOpened) textReformat()

        // Restore default formatter if closed (State reset)
        State.isOpenedProperty.once(onNew { if (!it) cTransArea.reset() })

        State.application.addShutdownHook("JustMonika") { playOggList(MONIKA_VOICE, MONIKA_SONG, callback = it) }
    }

}
