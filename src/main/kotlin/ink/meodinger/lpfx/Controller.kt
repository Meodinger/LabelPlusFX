package ink.meodinger.lpfx

import ink.meodinger.lpfx.component.*
import ink.meodinger.lpfx.component.common.CComboBox
import ink.meodinger.lpfx.component.common.CFileChooser
import ink.meodinger.lpfx.component.common.CLigatureArea
import ink.meodinger.lpfx.component.common.CTextSlider
import ink.meodinger.lpfx.component.singleton.AMenuBar
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
import ink.meodinger.lpfx.util.accelerator.isAltDown
import ink.meodinger.lpfx.util.accelerator.isControlDown
import ink.meodinger.lpfx.util.component.does
import ink.meodinger.lpfx.util.component.expandAll
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
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Cursor
import javafx.scene.control.*
import javafx.scene.input.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.FileChooser
import java.io.File
import java.io.IOException
import java.net.URL
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
class Controller : Initializable {

    @FXML private lateinit var root: BorderPane
    @FXML private lateinit var bSwitchViewMode: Button
    @FXML private lateinit var bSwitchWorkMode: Button
    @FXML private lateinit var lInfo: Label
    @FXML private lateinit var pMain: SplitPane
    @FXML private lateinit var pRight: SplitPane
    @FXML private lateinit var cGroupBar: CGroupBar
    @FXML private lateinit var cLabelPane: CLabelPane
    @FXML private lateinit var cSlider: CTextSlider
    @FXML private lateinit var cPicBox: CComboBox<String>
    @FXML private lateinit var cGroupBox: CComboBox<String>
    @FXML private lateinit var cTreeView: CTreeView
    @FXML private lateinit var cTransArea: CLigatureArea

    @FXML private fun switchViewMode() {
        val now = ViewMode.values().indexOf(State.viewMode)
        val all = ViewMode.values().size
        setViewMode(ViewMode.values()[(now + 1) % all])
    }
    @FXML private fun switchWorkMode() {
        val now = WorkMode.values().indexOf(State.workMode)
        val all = WorkMode.values().size
        setWorkMode(WorkMode.values()[(now + 1) % all])
    }

    private class BackupTaskManager {

        private var task: TimerTask = getNewTask()

        private fun getNewTask(): TimerTask {
            return object : TimerTask() {
                override fun run() {
                    if (State.isChanged) {
                        val bak = State.getBakFolder().resolve("${Date().time}${EXTENSION_BAK}")
                        try {
                            export(bak, FileType.MeoFile, State.transFile)
                        } catch (e: IOException) {
                            Logger.error("Auto-backup failed")
                            Logger.exception(e)
                        }
                    }
                }
            }
        }

        fun refresh() {
            task.cancel()
            task = getNewTask()
        }

        fun getTimerTask(): TimerTask = task

    }
    private val taskManager = BackupTaskManager()
    private val timer = Timer()

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
            val lastDirectory = File(lastFilePath).parentFile
            if (lastDirectory.exists()) {
                CFileChooser.lastDirectory = lastDirectory
                break
            }
            lastFilePath = RecentFiles.getLastOpenFile()
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
        cSlider.initScaleProperty.bindBidirectional(cLabelPane.initScaleProperty)
        cSlider.minScaleProperty.bindBidirectional(cLabelPane.minScaleProperty)
        cSlider.maxScaleProperty.bindBidirectional(cLabelPane.maxScaleProperty)
        cSlider.scaleProperty.bindBidirectional(cLabelPane.scaleProperty)

        // Update OpenRecent
        AMenuBar.updateOpenRecent()

        // Register handler
        cLabelPane.onLabelPlace = EventHandler {
            if (State.workMode != WorkMode.LabelMode) return@EventHandler
            if (State.transFile.groupCount == 0) return@EventHandler

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
        cLabelPane.onLabelRemove = EventHandler {
            if (State.workMode != WorkMode.LabelMode) return@EventHandler

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
        cLabelPane.onLabelPointed = EventHandler {
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
        cLabelPane.onLabelClicked = EventHandler {
            if (State.workMode != WorkMode.InputMode) return@EventHandler

            if (it.source.clickCount > 1) cLabelPane.moveToLabel(it.labelIndex)

            cTreeView.select(it.labelIndex)
        }
        cLabelPane.onLabelOther = EventHandler {
            if (State.workMode != WorkMode.LabelMode) return@EventHandler
            if (State.transFile.groupCount == 0) return@EventHandler

            val transGroup = State.transFile.getTransGroup(State.currentGroupId)

            cLabelPane.removeText()
            cLabelPane.createText(transGroup.name, Color.web(transGroup.colorHex), it.displayX, it.displayY)
        }
        cLabelPane.onLabelMove = EventHandler {
            State.isChanged = true
        }

        // Preferences
        cTransArea.font = Font.font(TextFont, Preference[Preference.TEXTAREA_FONT_SIZE].asDouble())
        pMain.setDividerPositions(Preference[Preference.MAIN_DIVIDER].asDouble())
        pRight.setDividerPositions(Preference[Preference.RIGHT_DIVIDER].asDouble())

        // View Mode
        val viewModes = Settings[Settings.ViewModePreference].asStringList()
        State.viewMode = ViewMode.getMode(viewModes[WorkMode.InputMode.ordinal])
        cTreeView.viewModeProperty.bind(State.viewModeProperty)
        bSwitchWorkMode.text = I18N["mode.work.input"]
        bSwitchViewMode.text = State.viewMode.toString()

        // TreeView Root Name
        cTreeView.picNameProperty.bind(State.currentPicNameProperty)

        // PictureBox Names
        cPicBox.itemsProperty.bind(object : ObjectBinding<ObservableList<String>>() {
            private var lastMapObservable = State.transFile.transMapObservable

            init {
                bind(State.transFileProperty)
            }

            override fun computeValue(): ObservableList<String> {
                unbind(lastMapObservable)

                lastMapObservable = State.transFile.transMapObservable

                bind(State.transFile.transMapObservable)

                return FXCollections.observableList(State.transFile.sortedPicNames)
            }

        })

        // GroupBox Names
        cGroupBox.itemsProperty.bind(object : ObjectBinding<ObservableList<String>>() {
            private var lastGroupListObservable = State.transFile.groupListObservable
            private val boundGroupNameProperties = ArrayList<StringProperty>()

            init {
                bind(State.transFileProperty)
            }

            override fun computeValue(): ObservableList<String> {
                unbind(lastGroupListObservable)
                for (property in boundGroupNameProperties) unbind(property)
                boundGroupNameProperties.clear()

                lastGroupListObservable = State.transFile.groupListObservable

                bind(State.transFile.groupListObservable)
                for (group in State.transFile.groupListObservable) {
                    bind(group.nameProperty)
                    boundGroupNameProperties.add(group.nameProperty)
                }

                return FXCollections.observableArrayList(State.transFile.groupNames)
            }
        })

        // LabelPane ColorList
        cLabelPane.colorHexListProperty.bind(object : ObjectBinding<ObservableList<String>>() {
            private var lastGroupListObservable = State.transFile.groupListObservable
            private val boundGroupHexProperties = ArrayList<StringProperty>()

            init {
                bind(State.transFileProperty)
            }

            override fun computeValue(): ObservableList<String> {
                unbind(lastGroupListObservable)
                for (property in boundGroupHexProperties) unbind(property)
                boundGroupHexProperties.clear()

                lastGroupListObservable = State.transFile.groupListObservable

                bind(State.transFile.groupListObservable)
                for (group in State.transFile.groupListObservable) {
                    bind(group.colorHexProperty)
                    boundGroupHexProperties.add(group.colorHexProperty)
                }

                return FXCollections.observableArrayList(State.transFile.groupColors)
            }
        })

        // Default image auto-centre
        cLabelPane.widthProperty().addListener(onChange {
            if (!State.isOpened || !State.getPicFileNow().exists())
                cLabelPane.moveToCenter()
        })
        cLabelPane.heightProperty().addListener(onChange {
            if (!State.isOpened || !State.getPicFileNow().exists())
                cLabelPane.moveToCenter()
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
        cPicBox.valueProperty.addListener { _, oldValue, newValue ->
            State.currentPicName = newValue ?:
                if (!State.isOpened) ""
                else if (cPicBox.items.contains(oldValue)) oldValue
                else cPicBox.items[0]
        }

        // currentGroupId
        cGroupBox.indexProperty.addListener { _, oldValue, newValue ->
            State.currentGroupId = if ((newValue as Int) != -1) newValue
                else if (!State.isOpened) -1
                else if (cGroupBox.items.size > (oldValue as Int)) oldValue
                else 0
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

            cPicBox.moveTo(newValue)

            renderTreeView()
            renderLabelPane()

            labelInfo("Change picture to $newValue")
        }

        // Clear text layer & re-select CGroup when group change
        State.currentGroupIdProperty.addListener { _, _, newGroupId ->
            if (!State.isOpened) return@addListener

            // Remove text
            cLabelPane.removeText()

            // Select CGroup & GroupBox
            if ((newGroupId as Int) != NOT_FOUND) {
                cGroupBox.moveTo(newGroupId)
                cGroupBar.select(newGroupId)
            }

            labelInfo("Change Group to ${cGroupBox.value}")
        }

        // Update text area when label change
        State.currentLabelIndexProperty.addListener { _, _, newIndex ->
            if (!State.isOpened) return@addListener

            val transLabels = State.transFile.getTransList(State.currentPicName)

            cTransArea.unbindBidirectional()
            if (newIndex != NOT_FOUND) {
                val newLabel = transLabels.find { it.index == newIndex }
                if (newLabel != null) cTransArea.bindBidirectional(newLabel.textProperty)
            }

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
                cGroupBox.moveTo(State.transFile.getGroupIdByName(newValue.name))
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
            cGroupBox.moveTo(it.text.toInt() - 1)
        }

        // Transform CGroup select to CGroupBox selected
        cGroupBar.setOnGroupSelect {
            cGroupBox.moveTo(it)
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
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        init()
        listen()
        effect()
        transform()
    }

    fun stay(): Boolean {
        // Not open
        if (!State.isOpened) return false
        // Opened but saved
        if (!State.isChanged) return false

        // Opened but not saved
        val result = showAlert(I18N["common.exit"], null, I18N["alert.not_save.content"])
        // Dialog present
        if (result.isPresent) when (result.get()) {
            ButtonType.YES -> {
                save(State.translationFile, FileType.getType(State.translationFile.path), true)
                return false
            }
            ButtonType.NO -> return false
            ButtonType.CANCEL -> return true
        }
        // Dialog closed
        return true
    }
    fun new(file: File, type: FileType): Boolean {
        Logger.info("Newing $type to ${file.path}", "Controller")

        // Choose Pics
        val potentialPics = ArrayList<String>()
        val pics = ArrayList<String>()
        val dir = file.parentFile
        if (dir.isDirectory && dir.listFiles() != null) {
            val files = dir.listFiles()
            if (files != null) for (f in files) if (f.isFile) {
                for (extension in EXTENSIONS_PIC) if (f.name.endsWith(extension)) {
                    potentialPics.add(f.name)
                }
            }
        }
        // todo: use checkPic-like Dialog
        val result = showChoiceList(State.stage, potentialPics)
        if (result.isPresent) {
            if (result.get().isEmpty()) {
                Logger.info("Chose none, Cancel", "Controller")
                showInfo(I18N["info.required_at_least_1_pic"])
                return false
            }
            pics.addAll(result.get())
        } else {
            Logger.info("Cancel", "Controller")
            return false
        }

        Logger.debug("Chose pics:", pics, "Controller")

        // Prepare new TransFile
        val groupList = ArrayList<TransGroup>()
        val groupNameList = Settings[Settings.DefaultGroupNameList].asStringList()
        val groupColorList = Settings[Settings.DefaultGroupColorHexList].asStringList()
        val groupCreateList = Settings[Settings.IsGroupCreateOnNewTrans].asBooleanList()
        for (i in groupNameList.indices) if (groupCreateList[i])
            groupList.add(TransGroup(groupNameList[i], groupColorList[i]))
        val transMap = HashMap<String, MutableList<TransLabel>>()
        for (pic in pics) transMap[pic] = ArrayList()

        val transFile = TransFile(TransFile.DEFAULT_VERSION, TransFile.DEFAULT_COMMENT, groupList, transMap)

        Logger.debug("Created TransFile: $transFile", "Controller")

        // Export to file
        try {
            export(file, type, transFile)
        } catch (e: IOException) {
            Logger.error("New failed", "Controller")
            Logger.exception(e)
            showError(I18N["error.new_failed"])
            showException(e)
            return false
        }

        Logger.info("Newed TransFile", "Controller")

        return true
    }
    fun open(file: File, type: FileType) {
        Logger.info("Opening ${file.path}", "Controller")

        // Read File
        val transFile: TransFile
        try {
            transFile = load(file, type)
        } catch (e: IOException) {
            Logger.error("Open failed", "Controller")
            Logger.exception(e)
            showError(I18N["error.open_failed"])
            showException(e)
            return
        }
        Logger.debug("Read TransFile: $transFile", "Controller")

        // Update State
        State.transFile = transFile
        State.translationFile = file
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
                Logger.info("Showed modified comment", "Controller")
                showInfo(I18N["common.info"], I18N["dialog.edited_comment.content"], comment)
            }
        }

        // Update recent files
        CFileChooser.lastDirectory = file.parentFile
        RecentFiles.add(file.path)
        AMenuBar.updateOpenRecent()

        // Auto backup
        taskManager.refresh()
        val bakDir = State.getBakFolder()
        if ((bakDir.exists() && bakDir.isDirectory) || bakDir.mkdir()) {
            timer.schedule(taskManager.getTimerTask(), AUTO_SAVE_DELAY, AUTO_SAVE_PERIOD)
            Logger.info("Scheduled auto-backup", "Controller")
        } else {
            Logger.warning("Auto-backup unavailable", "Controller")
            showError(I18N["error.auto_backup_unavailable"])
        }

        // Change title
        State.stage.title = INFO["application.name"] + " - " + file.name

        // Check pic for pic render
        checkPic()

        // Initialize workspace
        renderGroupBar()

        cPicBox.moveTo(0)
        cGroupBox.moveTo(0)

        Logger.info("Opened TransFile", "Controller")
    }
    fun save(file: File, type: FileType, isSilent: Boolean) {
        Logger.info("Saving to ${file.path}, isSilent:$isSilent", "Controller")

        // Check folder
        if (!isSilent) if (file.parent != State.getFileFolder().path) {
            val confirm = showConfirm(I18N["confirm.save_to_another_place"])
            if (!(confirm.isPresent && confirm.get() == ButtonType.YES)) return
        }

        // Backup if overwrite
        var bak: File? = null
        if (State.translationFile.path == file.path) {
            bak = File(State.translationFile.path + EXTENSION_BAK)

            try {
                transfer(State.translationFile, bak)
                Logger.info("Backed TransFile to ${bak.path}", "Controller")
            } catch (e: Exception) {
                bak = null
                Logger.error("TransFile backup failed", "Controller")
                Logger.exception(e)
                if (!isSilent) {
                    showError(I18N["error.backup_failed"])
                    showException(e)
                }
            }
        }

        // Export
        try {
            export(file, type, State.transFile)
            if (!isSilent) showInfo(I18N["info.saved_successfully"])
        } catch (e: IOException) {
            Logger.error("Save failed", "Controller")
            Logger.exception(e)
            if (!isSilent) {
                if (bak != null) {
                    showError(String.format(I18N["error.save_failed_backed.format.bak"], bak.path))
                } else {
                    showError(I18N["error.save_failed"])
                }
                showException(e)
            }
            return
        }

        // Remove Backup
        if (bak != null) if (!bak.delete()) {
            if (!isSilent) showError(I18N["error.backup_clear_failed"])
            Logger.error("Backup removed failed", "Controller")
        } else {
            Logger.info("Backup removed", "Controller")
        }

        // Update state
        State.translationFile = file
        State.isChanged = false

        // Change title
        State.stage.title = INFO["application.name"] + " - " + file.name

        Logger.info("Saved", "Controller")
    }

    fun checkPic() {
        // Set TransFile.fileMap
        val lost = ArrayList<String>()
        for (picName in State.transFile.sortedPicNames) {
            val picFile = State.translationFile.parentFile.resolve(picName)
            State.transFile.addFile(picName, picFile)

            if (!picFile.exists()) lost.add(picName)
        }
        if (lost.isNotEmpty()) {
            val confirm = showConfirm(I18N["confirm.specify_lost_pictures"])
            if (confirm.isPresent && confirm.get() == ButtonType.YES) {
                val defaultFile = File("")
                val files = MutableList(lost.size) { defaultFile }
                val chooser = FileChooser().also {
                    val extensions = List(EXTENSIONS_PIC.size) { index -> "*${EXTENSIONS_PIC[index]}" }
                    val fileFilter = FileChooser.ExtensionFilter(I18N["filetype.pictures"], extensions)
                    it.extensionFilters.add(fileFilter)
                }

                val dialog = Dialog<ButtonType>().also {
                    it.dialogPane.prefWidth = 600.0
                    it.dialogPane.prefHeight = 400.0
                    it.dialogPane.buttonTypes.addAll(ButtonType.APPLY, ButtonType.CANCEL)
                    it.dialogPane.content = GridPane().also { gridPane ->
                        gridPane.add(Label(I18N["dialog.specify.pic_name"]), 0, 0)
                        gridPane.add(Label(I18N["dialog.specify.pic_path"]), 1, 0)
                        for (i in lost.indices) {
                            val lostLabel = Label(lost[i])
                            val fileLabel = Label(defaultFile.path)
                            val button = Button(I18N["dialog.specify.choose_btn"]) does {
                                val picFile = chooser.showOpenDialog(State.stage) ?: return@does
                                fileLabel.text = picFile.path
                                files[i] = picFile
                            }

                            gridPane.add(lostLabel, 0, i + 1)
                            gridPane.add(fileLabel, 1, i + 1)
                            gridPane.add(button, 2, i + 1)
                        }
                    }
                }
                val result = dialog.showAndWait()

                if (!result.isPresent || result.get() == ButtonType.CANCEL) {
                    showInfo(I18N["info.specify_incomplete"])
                } else if (result.get() == ButtonType.APPLY) {
                    var uncomplete = false
                    for (i in lost.indices) {
                        val picName = lost[i]
                        val picFile = files[i]

                        if (picFile == defaultFile) {
                            uncomplete = true
                            continue
                        }
                        State.transFile.setFile(picName, picFile)
                    }
                    if (uncomplete) showInfo(I18N["info.specify_incomplete"])
                }
            }
        }
    }

    fun recovery(from: File, to: File) {
        Logger.info("Recovering from ${from.path}", "Controller")

        try {
            transfer(from, to)

            Logger.info("Recovered to ${to.path}", "Controller")
        } catch (e: Exception) {
            Logger.error("Recover failed", "Controller")
            Logger.exception(e)
            showError(I18N["error.recovery_failed"])
            showException(e)
        }

        open(to, FileType.getType(to.path))
    }
    fun export(file: File, type: FileType) {
        Logger.info("Exporting to ${file.path}", "Controller")

        try {
            export(file, type, State.transFile)

            Logger.info("Exported to ${file.path}", "Controller")
            showInfo(I18N["info.exported_successful"])
        } catch (e: IOException) {
            Logger.error("Export failed", "Controller")
            Logger.exception(e)
            showError(I18N["error.export_failed"])
            showException(e)
        }
    }
    fun pack(file: File) {
        Logger.info("Packing to ${file.path}", "Controller")

        try {
            pack(file, State.transFile)

            Logger.info("Packed to ${file.path}", "Controller")
            showInfo(I18N["info.exported_successful"])
        } catch (e : IOException) {
            Logger.error("Pack failed", "Controller")
            Logger.exception(e)
            showError(I18N["error.export_failed"])
            showException(e)
        }
    }

    fun exit() {
        if (!State.isChanged) {
            State.application.stop()
            return
        }

        showAlert(I18N["common.exit"], null, I18N["alert.not_save.content"]).ifPresent {
            when (it) {
                ButtonType.YES -> {
                    save(State.translationFile, FileType.getType(State.translationFile.path), false)
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
        taskManager.refresh()

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

        Logger.info("Group bar rendered", "Controller")
        Logger.debug("List is", lazy {
            List(State.transFile.groupCount) {
                TransGroup(State.transFile.groupNames[it], State.transFile.groupColors[it])
            }
        }, "Controller")
    }

    fun createGroupBarItem(transGroup: TransGroup) {
        cGroupBar.createGroup(transGroup)

        Logger.info("Created CGroup @ $transGroup", "Controller")
    }
    fun removeGroupBarItem(groupName: String) {
        cGroupBar.removeGroup(groupName)

        Logger.info("Removed CGroup @ $groupName", "Controller")
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
            Logger.error("Picture `${State.currentPicName}` not exists", "Controller")
            showError(String.format(I18N["error.picture_not_exists.format.s"], State.currentPicName))
            return
        } catch (e: IOException) {
            Logger.error("LabelPane update failed", "Controller")
            Logger.exception(e)
            showException(e)
            return
        }

        when (Settings[Settings.ScaleOnNewPicture].asInteger()) {
            Settings.NEW_PIC_SCALE_100  -> cLabelPane.scale = 1.0 // 100%
            Settings.NEW_PIC_SCALE_FIT  -> cLabelPane.fitToPane() // Fit
            Settings.NEW_PIC_SCALE_LAST -> doNothing() // Last
        }

        cLabelPane.moveToZero()
        Logger.info("LabelPane rendered", "Controller")
    }

    fun createLabelLayer() {
        cLabelPane.createLabelLayer()

        Logger.info("Created label layer @ ${State.transFile.groupCount - 1}", "Controller")
    }
    fun removeLabelLayer(groupId: Int) {
        cLabelPane.removeLabelLayer(groupId)

        Logger.info("Removed label layer @ $groupId", "Controller")
    }
    fun createLabel(transLabel: TransLabel) {
        cLabelPane.createLabel(transLabel)

        Logger.info("Created label @ $transLabel", "Controller")
    }
    fun removeLabel(labelIndex: Int) {
        cLabelPane.removeLabel(labelIndex)

        Logger.info("Removed label @ $labelIndex", "Controller")
    }

    // ----- TreeView ----- //
    fun renderTreeView() {
        cTreeView.render(
            // State.currentPicName,
            transGroups = State.transFile.groupList,
            transLabels = State.transFile.getTransList(State.currentPicName),
            // State.viewMode
        )

        Logger.info("TreeView rendered", "Controller")
    }

    fun createGroupTreeItem(transGroup: TransGroup) {
        cTreeView.createGroupItem(transGroup)

        Logger.info("Created group item @ $transGroup", "Controller")
    }
    fun removeGroupTreeItem(groupName: String) {
        cTreeView.removeGroupItem(groupName)

        Logger.info("Removed group item @ $groupName", "Controller")
    }
    fun createLabelTreeItem(transLabel: TransLabel) {
        cTreeView.createLabelItem(transLabel)

        Logger.info("Created label item @ $transLabel", "Controller")
    }
    fun removeLabelTreeItem(labelIndex: Int) {
        cTreeView.removeLabelItem(labelIndex)

        Logger.info("Removed label item @ $labelIndex", "Controller")
    }
    fun moveLabelTreeItem(labelIndex: Int, from: Int, to: Int) {
        cTreeView.moveLabelItem(labelIndex, from, to)

        Logger.info("Moved label item @ $labelIndex @ from=$from, to=$to", "Controller")
    }

    // ----- Mode ----- //
    fun setViewMode(mode: ViewMode) {
        State.viewMode = mode

        Logger.info("Switched view mode to $mode", "Controller")
    }
    fun setWorkMode(mode: WorkMode) {
        State.workMode = mode

        setViewMode(when (mode) {
            WorkMode.InputMode -> ViewMode.getMode(Settings[Settings.ViewModePreference].asStringList()[0])
            WorkMode.LabelMode -> ViewMode.getMode(Settings[Settings.ViewModePreference].asStringList()[1])
        })

        Logger.info("Switched work mode to $mode", "Controller")
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
            save(State.translationFile, FileType.getType(State.translationFile.path), true)

            val monika = State.getFileFolder().resolve("monika.json")
            save(monika, FileType.MeoFile, true)
        }
        if (State.isOpened) loveYouForever()

        // Force all text change to "JUST MONIKA"
        fun textReformat() {
            val monika = "JUST MONIKA "
            for (picName in State.transFile.sortedPicNames)
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