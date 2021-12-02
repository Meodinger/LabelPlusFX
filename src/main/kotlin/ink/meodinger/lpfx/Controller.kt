package ink.meodinger.lpfx

import ink.meodinger.lpfx.component.*
import ink.meodinger.lpfx.component.common.*
import ink.meodinger.lpfx.component.singleton.AMenuBar
import ink.meodinger.lpfx.component.singleton.ASpecifyDialog
import ink.meodinger.lpfx.io.export
import ink.meodinger.lpfx.io.load
import ink.meodinger.lpfx.io.pack
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Preference
import ink.meodinger.lpfx.options.RecentFiles
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.timer.TimerTaskManager
import ink.meodinger.lpfx.util.accelerator.isAltDown
import ink.meodinger.lpfx.util.accelerator.isControlDown
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.dialog.*
import ink.meodinger.lpfx.util.doNothing
import ink.meodinger.lpfx.util.file.transfer
import ink.meodinger.lpfx.util.media.playOggList
import ink.meodinger.lpfx.util.platform.TextFont
import ink.meodinger.lpfx.util.property.onChange
import ink.meodinger.lpfx.util.resource.*

import javafx.application.Platform
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.control.*
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
 * Main controller for lpfx
 */
class Controller(val root: View) {

    companion object {
        /**
         * Auto-save
         */
        private const val AUTO_SAVE_DELAY = 5 * 60 * 1000L
        private const val AUTO_SAVE_PERIOD = 3 * 60 * 1000L
    }

    private val bSwitchViewMode: Button      = root.bSwitchViewMode
    private val bSwitchWorkMode: Button      = root.bSwitchWorkMode
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
        this.bSwitchViewMode.setOnAction { switchViewMode() }
        this.bSwitchWorkMode.setOnAction { switchWorkMode() }

        init()
        listen()
        effect()
        transform()
    }

    /**
     * Component Initialize
     *
     * Some props of comps will be initialized
     */
    private fun init() {
        // Global event catch, prevent mnemonic parsing and the beep
        root.addEventHandler(KeyEvent.KEY_PRESSED) { if (it.isAltDown) it.consume() }

        // MenuBar
        root.top = AMenuBar

        // Set last used dir
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

        // Load rules
        updateLigatureRules()

        // Set components disabled
        bSwitchViewMode.disableProperty().bind(!State.isOpenedProperty)
        bSwitchWorkMode.disableProperty().bind(!State.isOpenedProperty)
        cTransArea.disableProperty().bind(!State.isOpenedProperty)
        cTreeView.disableProperty().bind(!State.isOpenedProperty)
        cPicBox.disableProperty().bind(!State.isOpenedProperty)
        cGroupBox.disableProperty().bind(!State.isOpenedProperty)
        cSlider.disableProperty().bind(!State.isOpenedProperty)
        cLabelPane.disableProperty().bind(!State.isOpenedProperty)

        // cSlider - cLabelPane#scale
        cSlider.initScaleProperty().bindBidirectional(cLabelPane.initScaleProperty())
        cSlider.minScaleProperty().bindBidirectional(cLabelPane.minScaleProperty())
        cSlider.maxScaleProperty().bindBidirectional(cLabelPane.maxScaleProperty())
        cSlider.scaleProperty().bindBidirectional(cLabelPane.scaleProperty())

        // Update OpenRecent
        AMenuBar.updateOpenRecent()

        // Register handler
        cLabelPane.setOnLabelPlace {
            if (State.workMode != WorkMode.LabelMode) return@setOnLabelPlace
            if (State.transFile.groupCount == 0) return@setOnLabelPlace

            val transLabel = TransLabel(
                it.labelIndex,
                State.currentGroupId, it.labelX, it.labelY, ""
            )

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
                    val groupId = transLabel.groupId
                    val transGroup = State.transFile.getTransGroup(groupId)
                    cLabelPane.createText(transGroup.name, Color.web(transGroup.colorHex), it.displayX, it.displayY)
                }
            }
        }
        cLabelPane.setOnLabelClicked {
            if (State.workMode != WorkMode.InputMode) return@setOnLabelClicked

            if (it.source.clickCount > 1) cLabelPane.moveToLabel(it.labelIndex)

            cTreeView.selectLabel(it.labelIndex)
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

        // Preferences
        cTransArea.font = Font.font(TextFont, Preference[Preference.TEXTAREA_FONT_SIZE].asDouble())
        pMain.setDividerPositions(Preference[Preference.MAIN_DIVIDER].asDouble())
        pRight.setDividerPositions(Preference[Preference.RIGHT_DIVIDER].asDouble())

        // Mode
        val viewModes = Settings[Settings.ViewModePreference].asStringList()
        State.viewMode = ViewMode.getMode(viewModes[WorkMode.InputMode.ordinal])
        cTreeView.viewModeProperty().bind(State.viewModeProperty)
        // mode text
        bSwitchViewMode.text = State.viewMode.toString()
        bSwitchWorkMode.text = State.workMode.toString()

        // TreeView Root Name
        cTreeView.picNameProperty().bind(State.currentPicNameProperty)

        // PictureBox Names
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

        // GroupBox Names
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

        // LabelPane ColorList
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

        // Default image auto-centre
        cLabelPane.widthProperty().addListener(onChange {
            if (!State.isOpened || !State.getPicFileNow().exists())
                cLabelPane.moveToCenter()
        })
        cLabelPane.heightProperty().addListener(onChange {
            if (!State.isOpened || !State.getPicFileNow().exists()) cLabelPane.moveToCenter()
        })

        // Display default image
        cLabelPane.isVisible = false
        Platform.runLater {
            cLabelPane.moveToCenter()
            cLabelPane.isVisible = true
        }
    }
    /**
     * Property listen
     *
     * State & Preference will change with specific property
     */
    private fun listen() {
        // isChanged
        cTransArea.textProperty().addListener(onChange {
            if (cTransArea.isBound) State.isChanged = true
        })

        // currentPicName
        cPicBox.valueProperty().addListener { _, oldValue, newValue ->
            if (!State.isOpened) return@addListener

            // fixme: unexpected value change when remove first (when also current) picture.
            //   want the new first picture, got last picture
            // 2021/12/02 NOTE: Maybe should listen to both items and value
            val picNames = State.transFile.sortedPicNames
            val newPicName = newValue ?:
                if (picNames.contains(oldValue)) oldValue
                else picNames[0]
            State.currentPicName = newPicName
        }

        // currentGroupId
        cGroupBox.indexProperty().addListener { _, oldValue, newValue ->
            if (!State.isOpened) return@addListener

            // fixme: unexpected value change when REMOVE first (when also current) picture.
            //   want the new first group, got null
            // fixme: unexpected value change when CHANGE first (when also current) picture.
            //   want the new name displayed in GroupBox, but not

            // 2021/12/02 NOTE: Maybe should listen to both items and index
            // 2021/12/02 NOTE: Use groupId cause name may change

            val newGroupId = if ((newValue as Int) != NOT_FOUND) newValue
                else if ((oldValue as Int) < State.transFile.groupCount) oldValue
                else 0

            State.currentGroupId = newGroupId
        }

        // currentLabelIndex
        State.currentPicNameProperty.addListener(onChange {
            // Clear selected when change pic
            State.currentLabelIndex = NOT_FOUND
        })
        cTreeView.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            if (newValue != null && newValue is CTreeLabelItem) {
                State.currentLabelIndex = newValue.index
            }
        }

        // Preferences
        cTransArea.fontProperty().addListener { _, _, newValue ->
            Preference[Preference.TEXTAREA_FONT_SIZE] = newValue.size.toInt()
        }
        pMain.dividers[0].positionProperty().addListener { _, _, newValue ->
            Preference[Preference.MAIN_DIVIDER] = newValue
        }
        pRight.dividers[0].positionProperty().addListener { _, _, newValue ->
            Preference[Preference.RIGHT_DIVIDER] = newValue
        }
    }
    /**
     * Effect on view
     *
     * View will update with some props change
     */
    private fun effect() {
        // Update cTreeView & cLabelPane when pic change
        State.currentPicNameProperty.addListener { _, _, newValue ->
            if (!State.isOpened) return@addListener
            if (newValue == null) return@addListener

            cPicBox.select(newValue)

            renderTreeView()
            renderLabelPane()

            labelInfo("Change picture to $newValue")
        }

        // Clear text layer & re-select CGroup when group change
        State.currentGroupIdProperty.addListener { _, _, newGroupId ->
            if (!State.isOpened) return@addListener

            // Remove text
            cLabelPane.removeText()

            if (newGroupId as Int == NOT_FOUND) return@addListener

            // Select CGroup, GroupBox, CTreeView
            cGroupBox.select(newGroupId)
            cGroupBar.select(newGroupId)
            if (State.viewMode != ViewMode.IndexMode) cTreeView.selectGroup(State.transFile.getTransGroup(newGroupId).name)

            labelInfo("Change Group to ${cGroupBox.value}")
        }

        // Update text area when label change
        State.currentLabelIndexProperty.addListener { _, _, newIndex ->
            if (!State.isOpened) return@addListener

            // unbind TextArea
            cTransArea.unbindBidirectional()

            if (newIndex == NOT_FOUND) return@addListener

            // bind new property
            val transLabels = State.transFile.getTransList(State.currentPicName)
            val newLabel = transLabels.find { it.index == newIndex }
            if (newLabel != null) cTransArea.bindBidirectional(newLabel.textProperty)

            labelInfo("Selected label $newIndex")
        }

        // Update cLabelPane default cursor when work mode change
        State.workModeProperty.addListener { _, _, newMode ->
            if (!State.isOpened) return@addListener

            bSwitchWorkMode.text = when (newMode!!) {
                WorkMode.InputMode -> I18N["mode.work.input"]
                WorkMode.LabelMode -> I18N["mode.work.label"]
            }

            when (newMode) {
                WorkMode.LabelMode -> cLabelPane.defaultCursor = Cursor.CROSSHAIR
                WorkMode.InputMode -> cLabelPane.defaultCursor = Cursor.DEFAULT
            }

            labelInfo("Switch work mode to $newMode")
        }

        // Update CTreeView when view mode change
        State.viewModeProperty.addListener { _, _, newMode ->
            if (!State.isOpened) return@addListener

            bSwitchViewMode.text = when (newMode!!) {
                ViewMode.IndexMode -> I18N["mode.view.index"]
                ViewMode.GroupMode -> I18N["mode.view.group"]
            }

            // renderTreeView()

            labelInfo("Switch view mode to $newMode")
        }

        // Bind Ctrl/Alt/Meta + Scroll with font size change
        cTransArea.addEventFilter(ScrollEvent.SCROLL) {
            if (!(isControlDown(it) || isAltDown(it))) return@addEventFilter

            val newSize = ((cTransArea.font.size + it.deltaY / SCROLL_DELTA).toInt())
                .coerceAtLeast(12)
                .coerceAtMost(64)

            cTransArea.font = Font.font(TextFont, newSize.toDouble())
            cTransArea.positionCaret(0)
            it.consume()

            labelInfo("Text font size set to $newSize")
        }

        // Bind Label and Tree
        cTreeView.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            if (it.button != MouseButton.PRIMARY) return@addEventHandler
            if (it.clickCount < 2) return@addEventHandler

            val item = cTreeView.selectionModel.selectedItem
            if (item != null && item is CTreeLabelItem) {
                cLabelPane.moveToLabel(item.index)
            }
        }
        cTreeView.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (!it.code.isArrowKey) return@addEventHandler

            val item = cTreeView.getTreeItem(
                cTreeView.selectionModel.selectedIndex + when (it.code) {
                    KeyCode.UP -> -1
                    KeyCode.DOWN -> 1
                    else -> 0
                }
            )
            if (item != null && item is CTreeLabelItem) {
                cLabelPane.moveToLabel(item.index)
            }
        }
    }
    /**
     * Transformation
     *
     * Some actions will transform to others
     */
    private fun transform() {
        // CTreeView selectionModal -> select Group
        cTreeView.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            if (newValue != null && newValue is CTreeGroupItem)
                cGroupBox.select(State.transFile.getGroupIdByName(newValue.name))
        }

        // Transform tab pressed in CTreeView to ViewModeBtn clicked
        cTreeView.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (it.code != KeyCode.TAB) return@addEventHandler

            switchViewMode()
            it.consume()
        }

        // Transform tab pressed in CLabelPane to WorkModeBtn clicked
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (it.code != KeyCode.TAB) return@addEventHandler

            cLabelPane.removeText()
            switchWorkMode()
            it.consume()
        }

        // Transform number key pressed to CGroupBox selected
        root.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (!it.code.isDigitKey) return@addEventHandler

            val index = it.text.toInt() - 1
            if (index < 0 || index >= cGroupBox.items.size) return@addEventHandler
            cGroupBox.select(it.text.toInt() - 1)
        }

        // Transform CGroup select to CGroupBox selected
        cGroupBar.setOnGroupSelect {
            cGroupBox.select(it)
        }

        // Transform Ctrl + Left/Right KeyEvent to CPicBox button clicked
        val arrowKeyChangePicHandler = EventHandler<KeyEvent> {
            if (!(isControlDown(it) && it.code.isArrowKey)) return@EventHandler

            when (it.code) {
                KeyCode.LEFT -> cPicBox.back()
                KeyCode.RIGHT -> cPicBox.next()
                else -> return@EventHandler
            }

            it.consume()
        }
        root.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangePicHandler)
        cTransArea.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangePicHandler)

        // Transform Ctrl + Up/Down KeyEvent to CTreeView selected
        fun getNextLabelItemIndex(from: Int, direction: Int): Int {
            var index = from
            var item: TreeItem<String>?
            do {
                index += direction
                item = cTreeView.getTreeItem(index)

                if (item != null) {
                    item.expandAll()
                } else {
                    return -1
                }
            } while (item !is CTreeLabelItem)

            return index
        }
        val arrowKeyChangeLabelHandler = EventHandler<KeyEvent> {
            if (!(isControlDown(it) && it.code.isArrowKey)) return@EventHandler
            if (it.code == KeyCode.LEFT || it.code == KeyCode.RIGHT) return@EventHandler

            var selectedIndex = cTreeView.selectionModel.selectedIndex
            cTreeView.selectionModel.clearSelection()

            if (selectedIndex == -1) selectedIndex = when (it.code) {
                KeyCode.UP -> cTreeView.expandedItemCount
                KeyCode.DOWN -> 0
                else -> return@EventHandler
            }

            val shift = if (it.code == KeyCode.UP) -1 else 1
            var labelItemIndex = selectedIndex + shift

            val item = cTreeView.getTreeItem(labelItemIndex)
            if (item == null) labelItemIndex = getNextLabelItemIndex(labelItemIndex, -shift)
            if (item !is CTreeLabelItem) labelItemIndex = getNextLabelItemIndex(labelItemIndex, shift)

            if (labelItemIndex == -1) return@EventHandler

            cLabelPane.moveToLabel((cTreeView.getTreeItem(labelItemIndex) as CTreeLabelItem).index)
            cTreeView.scrollTo(labelItemIndex)
            cTreeView.selectionModel.select(labelItemIndex)

            it.consume()
        }
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangeLabelHandler)
        cTransArea.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangeLabelHandler)

        // Transform Ctrl + Enter to Ctrl + Down
        val enterKeyTransformerHandler = EventHandler<KeyEvent> {
            if (isControlDown(it) && it.code == KeyCode.ENTER) {
                cLabelPane.fireEvent(KeyEvent(
                    it.source, it.target, it.eventType,
                    "\u0000", "", KeyCode.DOWN,
                    it.isShiftDown, it.isControlDown, it.isAltDown, it.isMetaDown
                ))
            }
        }
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED, enterKeyTransformerHandler)
        cTransArea.addEventHandler(KeyEvent.KEY_PRESSED, enterKeyTransformerHandler)
    }

    fun specifyPicFiles() {
        val picFiles = ASpecifyDialog.specify()
        if (picFiles.isEmpty()) showInfo(I18N["specify.info.incomplete"], State.stage)
        else {
            val picCount = State.transFile.picCount
            val picNames = State.transFile.sortedPicNames
            var uncomplete = false
            for (i in 0 until picCount) {
                val picName = picNames[i]
                val picFile = picFiles[i]
                if (!picFile.exists()) {
                    uncomplete = true
                    continue
                }
                State.transFile.setFile(picName, picFile)
            }
            if (uncomplete) showInfo(I18N["specify.info.incomplete"], State.stage)
        }
        if (State.isOpened) {
            if (State.currentPicName.isNotEmpty()) // in case of open but not set currentPicName
                if (State.transFile.getFile(State.currentPicName).exists())
                    renderLabelPane() // Re-render picture
        }
    }

    fun stay(): Boolean {
        // Not open
        if (!State.isOpened) return false
        // Opened but saved
        if (!State.isChanged) return false

        // Opened but not saved
        val result = showAlert(I18N["common.exit"], null, I18N["alert.not_save.content"], State.stage)
        // Dialog present
        if (result.isPresent) when (result.get()) {
            ButtonType.YES -> {
                save(State.translationFile, FileType.getType(State.translationFile), true)
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
    fun new(file: File, type: FileType): File? {
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
                val result = showConfirm(I18N["confirm.project_folder_invalid"], State.stage)
                if (result.isPresent && result.get() == ButtonType.YES) {
                    // Specify project folder
                    val newFolder = DirectoryChooser().also { it.initialDirectory = projectFolder }.showDialog(State.stage)
                    if (newFolder != null) projectFolder = newFolder
                } else {
                    // Do not specify, cancel
                    Logger.info("Cancel (project folder invalid)", LOGSRC_CONTROLLER)
                    showInfo(I18N["common.cancel"], State.stage)
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
                showInfo(I18N["info.required_at_least_1_pic"], State.stage)
                return null
            }
            selectedPics.addAll(result.get())
        } else {
            Logger.info("Cancel (no selected)", LOGSRC_CONTROLLER)
            showInfo(I18N["common.cancel"], State.stage)
            return null
        }

        Logger.debug("Chose pics:", selectedPics, LOGSRC_CONTROLLER)

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

        Logger.debug("Created TransFile: $transFile", LOGSRC_CONTROLLER)

        // Export to file
        try {
            export(file, type, transFile)
        } catch (e: IOException) {
            Logger.error("New failed", LOGSRC_CONTROLLER)
            Logger.exception(e)
            showError(I18N["error.new_failed"], State.stage)
            showException(e, State.stage)
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
    fun open(file: File, type: FileType, projectFolder: File = file.parentFile) {
        Logger.info("Opening ${file.path}", LOGSRC_CONTROLLER)

        // Read File
        val transFile: TransFile
        try {
            transFile = load(file, type)
            // Setup PicFiles
            for (picName in transFile.picNames) {
                transFile.addFile(picName, projectFolder.resolve(picName))
            }
        } catch (e: IOException) {
            Logger.error("Open failed", LOGSRC_CONTROLLER)
            Logger.exception(e)
            showError(I18N["error.open_failed"], State.stage)
            showException(e, State.stage)
            return
        }
        Logger.debug("Read TransFile: $transFile", LOGSRC_CONTROLLER)

        // Update State
        State.transFile = transFile
        State.translationFile = file
        State.projectFolder = projectFolder
        State.isOpened = true

        // Show info if comment not in default list
        if (!RecentFiles.getAll().contains(file.path)) {
            val comment = transFile.comment.trim()
            var isModified = true
            for (defaultComment in TransFile.DEFAULT_COMMENT_LIST) {
                if (comment == defaultComment) {
                    isModified = false
                    break
                }
            }
            if (isModified) {
                Logger.info("Showed modified comment", LOGSRC_CONTROLLER)
                showInfo(I18N["common.info"], I18N["m.comment.dialog.content"], comment, State.stage)
            }
        }

        // Update recent files
        CFileChooser.lastDirectory = file.parentFile
        RecentFiles.add(file.path)
        AMenuBar.updateOpenRecent()

        // Auto backup
        backupManager.clear()
        val bakDir = State.getBakFolder()
        if ((bakDir.exists() && bakDir.isDirectory) || bakDir.mkdir()) {
            backupManager.refresh()
            backupManager.schedule()
            Logger.info("Scheduled auto-backup", LOGSRC_CONTROLLER)
        } else {
            Logger.warning("Auto-backup unavailable", LOGSRC_CONTROLLER)
            showError(I18N["error.auto_backup_unavailable"], State.stage)
        }

        // Change title
        State.stage.title = INFO["application.name"] + " - " + file.name

        // Check pic for pic render
        if (State.transFile.checkLost().isNotEmpty()) {
            // Specify now?
            showConfirm(I18N["specify.confirm.lost_pictures"], State.stage).ifPresent {
                if (it == ButtonType.YES) specifyPicFiles()
            }
        }

        // Initialize workspace
        renderGroupBar()

        cPicBox.select(0)
        cGroupBox.select(0)

        Logger.info("Opened TransFile", LOGSRC_CONTROLLER)
    }
    /**
     * Save a TransFile
     * @param file Which file will the TransFile write to
     * @param type Which type will the translation file be
     * @param silent Whether the save procedure is done in silence or not
     */
    fun save(file: File, type: FileType, silent: Boolean) {
        val overwrite = State.translationFile == file

        Logger.info("Saving to ${file.path}, isSilent:$silent, isOverwrite:$overwrite", LOGSRC_CONTROLLER)

        // Check folder
        if (!silent) if (file.parent != State.getFileFolder().path) {
            val confirm = showConfirm(I18N["confirm.save_to_another_place"], State.stage)
            if (!(confirm.isPresent && confirm.get() == ButtonType.YES)) return
        }

        // Use temp if overwrite
        val exportDest = if (overwrite) File("${State.translationFile.path}.temp") else file
        fun removeTemp() {
            try {
                Files.delete(exportDest.toPath())
                Logger.info("Temp file removed", LOGSRC_CONTROLLER)
            } catch (e: Exception) {
                Logger.error("Temp file remove failed", LOGSRC_CONTROLLER)
                if (!silent) showError(String.format(I18N["error.save_temp_remove_failed.s"], exportDest.path), State.stage)
            }
        }

        // Export
        try {
            export(exportDest, type, State.transFile)
            if (!silent) showInfo(I18N["info.saved_successfully"], State.stage)
            Logger.info("Exported translation")
        } catch (e: IOException) {
            Logger.error("Export translation failed", LOGSRC_CONTROLLER)
            Logger.exception(e)
            showError(I18N["error.save_failed"], State.stage)
            showException(e, State.stage)

            // Delete the temp file
            if (overwrite) removeTemp()

            Logger.info("Save failed")
            return
        }

        if (overwrite) {
            // Transfer to original file if export success
            try {
                transfer(exportDest, file)
                Logger.info("Transferred temp file")
            } catch (e: Exception) {
                Logger.error("Transfer temp file failed", LOGSRC_CONTROLLER)
                Logger.exception(e)
                showError(I18N["error.save_temp_transfer_failed"], State.stage)
                showException(e, State.stage)

                Logger.info("Save failed")
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
    fun recovery(from: File, to: File) {
        Logger.info("Recovering from ${from.path}", LOGSRC_CONTROLLER)

        try {
            transfer(from, to)

            Logger.info("Recovered to ${to.path}", LOGSRC_CONTROLLER)
        } catch (e: Exception) {
            Logger.error("Recover failed", LOGSRC_CONTROLLER)
            Logger.exception(e)
            showError(I18N["error.recovery_failed"], State.stage)
            showException(e, State.stage)
        }

        open(to, FileType.getType(to))
    }
    /**
     * Export a TransFile in specific type
     * @param file Which file will the TransFile write to
     * @param type Which type will the translation file be
     */
    fun export(file: File, type: FileType) {
        Logger.info("Exporting to ${file.path}", LOGSRC_CONTROLLER)

        try {
            export(file, type, State.transFile)

            Logger.info("Exported to ${file.path}", LOGSRC_CONTROLLER)
            showInfo(I18N["info.exported_successful"], State.stage)
        } catch (e: IOException) {
            Logger.error("Export failed", LOGSRC_CONTROLLER)
            Logger.exception(e)
            showError(I18N["error.export_failed"], State.stage)
            showException(e, State.stage)
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
            showInfo(I18N["info.exported_successful"], State.stage)
        } catch (e : IOException) {
            Logger.error("Pack failed", LOGSRC_CONTROLLER)
            Logger.exception(e)
            showError(I18N["error.export_failed"], State.stage)
            showException(e, State.stage)
        }
    }

    fun exit() {
        if (!State.isChanged) {
            State.application.stop()
            return
        }

        showAlert(I18N["common.exit"], null, I18N["alert.not_save.content"], State.stage).ifPresent {
            when (it) {
                ButtonType.YES -> {
                    save(State.translationFile, FileType.getType(State.translationFile), false)
                    State.application.stop()
                }
                ButtonType.NO -> {
                    State.application.stop()
                }
                ButtonType.CANCEL -> {
                    return@ifPresent
                }
            }
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

        labelInfo("Reset")
    }

    // ----- Update component properties ----- //
    fun updateLigatureRules() {
        cTransArea.ligatureRules = FXCollections.observableList(Settings[Settings.LigatureRules].asPairList())
    }

    // ----- GroupBar ----- //
    fun renderGroupBar() {
        cGroupBar.reset()
        cGroupBar.render(State.transFile.groupList)
        cGroupBar.select(if (State.currentGroupId == NOT_FOUND) 0 else State.currentGroupId)

        Logger.info("Group bar rendered", LOGSRC_CONTROLLER)
        Logger.debug("List is", lazy {
            List(State.transFile.groupCount) {
                TransGroup(State.transFile.groupNames[it], State.transFile.groupColors[it])
            }
        }, LOGSRC_CONTROLLER)
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
            cLabelPane.render(
                State.getPicFileNow(),
                State.transFile.groupCount,
                State.transFile.getTransList(State.currentPicName)
            )
        } catch (e: CLabelPane.LabelPaneException) {
            Logger.error("Picture `${State.currentPicName}` not exists", LOGSRC_CONTROLLER)
            showError(String.format(I18N["error.picture_not_exists.s"], State.currentPicName), State.stage)
            return
        } catch (e: IOException) {
            Logger.error("LabelPane update failed", LOGSRC_CONTROLLER)
            Logger.exception(e)
            showException(e, State.stage)
            return
        }

        when (Settings[Settings.ScaleOnNewPicture].asInteger()) {
            Settings.NEW_PIC_SCALE_100  -> cLabelPane.scale = 1.0 // 100%
            Settings.NEW_PIC_SCALE_FIT  -> cLabelPane.fitToPane() // Fit
            Settings.NEW_PIC_SCALE_LAST -> doNothing() // Last
        }

        cLabelPane.moveToZero()
        Logger.info("LabelPane rendered", LOGSRC_CONTROLLER)
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
        cTreeView.render(
            // State.currentPicName,
            transGroups = State.transFile.groupList,
            transLabels = State.transFile.getTransList(State.currentPicName),
            // State.viewMode
        )

        Logger.info("TreeView rendered", LOGSRC_CONTROLLER)
    }

    fun registerGroup(transGroup: TransGroup) {
        cTreeView.registerGroup(transGroup)

        Logger.info("Registered group item @ $transGroup", LOGSRC_CONTROLLER)
    }
    fun unregisterGroup(groupName: String) {
        cTreeView.unregisterGroup(groupName)

        Logger.info("Unregistered group item @ $groupName", LOGSRC_CONTROLLER)
    }
    fun createGroupTreeItem(transGroup: TransGroup) {
        cTreeView.createGroupItem(transGroup)

        Logger.info("Created group item @ $transGroup", LOGSRC_CONTROLLER)
    }
    fun removeGroupTreeItem(groupName: String) {
        cTreeView.removeGroupItem(groupName)

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
            WorkMode.InputMode -> ViewMode.getMode(Settings[Settings.ViewModePreference].asStringList()[0])
            WorkMode.LabelMode -> ViewMode.getMode(Settings[Settings.ViewModePreference].asStringList()[1])
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
            save(State.translationFile, FileType.getType(State.translationFile), true)

            val monika = State.getFileFolder().resolve("monika.json")
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
        State.isOpenedProperty.addListener { _, _, newValue ->
            // Restore default formatter
            if (!newValue) cTransArea.resetFormatter()
        }

        State.application.addShutdownHook("JustMonika") {
            playOggList(MONIKA_VOICE, MONIKA_SONG, callback = it)
        }
    }

}