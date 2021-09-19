package info.meodinger.lpfx

import info.meodinger.lpfx.component.*
import info.meodinger.lpfx.io.*
import info.meodinger.lpfx.options.*
import info.meodinger.lpfx.type.*
import info.meodinger.lpfx.util.accelerator.isAltDown
import info.meodinger.lpfx.util.accelerator.isControlDown
import info.meodinger.lpfx.util.dialog.*
import info.meodinger.lpfx.util.file.transfer
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.INFO
import info.meodinger.lpfx.util.resource.get
import info.meodinger.lpfx.util.tree.expandAll

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Cursor
import javafx.scene.control.*
import javafx.scene.input.*
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import java.io.*
import java.net.URL
import java.util.*
import kotlin.system.exitProcess


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx
 */

/**
 * Main controller for lpfx
 */
class Controller : Initializable {

    @FXML private lateinit var root: BorderPane
    @FXML private lateinit var bSwitchViewMode: Button
    @FXML private lateinit var bSwitchWorkMode: Button
    @FXML private lateinit var pMain: SplitPane
    @FXML private lateinit var pRight: SplitPane
    @FXML private lateinit var cMenuBar: CMenuBar
    @FXML private lateinit var cLabelPane: CLabelPane
    @FXML private lateinit var cSlider: CTextSlider
    @FXML private lateinit var cPicBox: CComboBox<String>
    @FXML private lateinit var cGroupBox: CComboBox<String>
    @FXML private lateinit var cTreeView: CTreeView
    @FXML private lateinit var cTransArea: CTransArea
    @FXML private lateinit var lInfo: Label

    private class BackupTaskManager {

        var task: TimerTask = getNewTask()

        private fun getNewTask(): TimerTask {
            return object : TimerTask() {
                override fun run() {
                    if (State.isChanged) {
                        val bak = File(State.getBakFolder() + File.separator + Date().time + EXTENSION_BAK)
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
     */
    private fun init() {
        // Global event catch, prevent mnemonic parsing and the beep
        root.addEventHandler(KeyEvent.KEY_PRESSED) { if (it.isAltDown) it.consume() }

        // Text
        bSwitchWorkMode.text = I18N["mode.work.input"]
        bSwitchViewMode.text = I18N["mode.view.group"]

        // Set last used dir
        val lastFilePath = RecentFiles.getLastOpenFile()
        if (lastFilePath != null) CFileChooser.lastDirectory = File(lastFilePath).parentFile

        // Update OpenRecent
        cMenuBar.updateOpenRecent()

        // Set comp disabled
        bSwitchViewMode.disableProperty().bind(State.isOpenedProperty.not())
        bSwitchWorkMode.disableProperty().bind(State.isOpenedProperty.not())
        cTransArea.disableProperty().bind(State.isOpenedProperty.not())
        cTreeView.disableProperty().bind(State.isOpenedProperty.not())
        cPicBox.disableProperty().bind(State.isOpenedProperty.not())
        cGroupBox.disableProperty().bind(State.isOpenedProperty.not())
        cSlider.disableProperty().bind(State.isOpenedProperty.not())
        cLabelPane.disableProperty().bind(State.isOpenedProperty.not())

        // Warp cPicBox
        cPicBox.isWrapped = true

        // TextArea font size
        cTransArea.font = Font.font(Preference[Preference.TEXTAREA_FONT_SIZE].asDouble())

        // Display dividers
        pMain.setDividerPositions(Preference[Preference.MAIN_DIVIDER].asDouble())
        pRight.setDividerPositions(Preference[Preference.RIGHT_DIVIDER].asDouble())

        // Display default image
        cLabelPane.isVisible = false
        Platform.runLater {
            cLabelPane.moveToCenter()
            cLabelPane.isVisible = true
        }
    }
    /**
     * Property bindings
     */
    private fun bind() {
        // cTransArea - isChanged
        cTransArea.textProperty().addListener { _, _, _ ->
            if (cTransArea.isBound) State.isChanged = true
        }

        // cPicBox - currentPicName
        cPicBox.valueProperty.addListener { _, _, newValue ->
            if (!State.isOpened) return@addListener
            if (newValue == null) return@addListener

            State.currentPicName = newValue
        }

        // cGroupBox - currentGroupId
        cGroupBox.valueProperty.addListener { _, _, newValue ->
            if (!State.isOpened) return@addListener
            if (newValue == null) return@addListener

            State.currentGroupId = State.transFile.getGroupIdByName(newValue)
        }

        // cLabelPane - currentLabelIndex
        cLabelPane.selectedLabelIndexProperty.addListener { _, _, newValue ->
            if (!State.isOpened) return@addListener

            State.currentLabelIndex = newValue as Int
        }

        // cSlider - cLabelPane#scale
        cSlider.initScaleProperty.bindBidirectional(cLabelPane.initScaleProperty)
        cSlider.minScaleProperty.bindBidirectional(cLabelPane.minScaleProperty)
        cSlider.maxScaleProperty.bindBidirectional(cLabelPane.maxScaleProperty)
        cSlider.scaleProperty.bindBidirectional(cLabelPane.scaleProperty)

    }
    /**
     * Listeners & Handlers
     */
    private fun reg() {
        // Update cTreeView & cLabelPane when pic change
        State.currentPicNameProperty.addListener { _, _, _ ->
            if (!State.isOpened) return@addListener

            State.currentLabelIndex = NOT_FOUND

            updateTreeView()
            updateLabelPane()
        }

        // Clear text layer when group change
        State.currentGroupIdProperty.addListener { _, _, _ ->
            if (!State.isOpened) return@addListener

            cLabelPane.removeText()
        }

        // Update text area when label change
        State.currentLabelIndexProperty.addListener { _, _, newValue ->
            if (!State.isOpened) return@addListener

            val transLabels = State.transFile.getTransLabelListOf(State.currentPicName)

            cTransArea.unbindBidirectional()
            if (newValue != NOT_FOUND) {
                val newLabel = transLabels.find { it.index == newValue }
                if (newLabel != null) cTransArea.bindBidirectional(newLabel.textProperty)
            }
        }

        // Update cLabelPane default cursor when work mode change
        State.workModeProperty.addListener { _, _, newValue ->
            if (!State.isOpened) return@addListener

            when (newValue!!) {
                WorkMode.LabelMode -> cLabelPane.defaultCursor = Cursor.CROSSHAIR
                WorkMode.InputMode -> cLabelPane.defaultCursor = Cursor.DEFAULT
            }
        }

        // Register handler
        cLabelPane.onLabelPlace = EventHandler {
            if (State.workMode != WorkMode.LabelMode) return@EventHandler

            val transLabel = TransLabel(
                State.transFile.getTransLabelListOf(State.currentPicName).size + 1,
                it.labelX, it.labelY, State.currentGroupId, ""
            )

            // Edit data
            State.addTransLabel(State.currentPicName, transLabel)
            // Update view
            cLabelPane.createLabel(transLabel)
            addLabelItem(transLabel)
            // Mark change
            State.isChanged = true
        }
        cLabelPane.onLabelRemove = EventHandler {
            if (State.workMode != WorkMode.LabelMode) return@EventHandler

            val transLabel = State.transFile.getTransLabelAt(State.currentPicName, it.labelIndex)

            // Edit data
            State.removeTransLabel(State.currentPicName, transLabel)
            for (label in State.transFile.getTransLabelListOf(State.currentPicName)) {
                if (label.index > transLabel.index) {
                    State.setTransLabelIndex(State.currentPicName, label.index, label.index - 1)
                }
            }
            // Update view
            cLabelPane.removeLabel(transLabel)
            removeLabelItem(transLabel)
            // Mark change
            State.isChanged = true
        }
        cLabelPane.onLabelPointed = EventHandler {
            val transLabel = State.transFile.getTransLabelAt(State.currentPicName, it.labelIndex)

            // Text display
            cLabelPane.removeText()
            cLabelPane.createText(transLabel.text, Color.BLACK, it.displayX, it.displayY)
        }
        cLabelPane.onLabelClicked = EventHandler {
            if (State.workMode != WorkMode.InputMode) return@EventHandler

            val transLabel = State.transFile.getTransLabelAt(State.currentPicName, it.labelIndex)
            if (it.source.clickCount > 1) cLabelPane.moveToLabel(transLabel)

            val item = findLabelItemByIndex(it.labelIndex)
            cTreeView.selectionModel.clearSelection()
            cTreeView.selectionModel.select(item)
            cTreeView.scrollTo(cTreeView.getRow(item))
        }
        cLabelPane.onLabelOther = EventHandler {
            if (State.workMode != WorkMode.LabelMode) return@EventHandler

            val transGroup = State.transFile.getTransGroupAt(State.currentGroupId)

            cLabelPane.removeText()
            cLabelPane.createText(transGroup.name, Color.web(transGroup.color), it.displayX, it.displayY)
        }

        // Update preference
        cTransArea.fontProperty().addListener { _, _, newValue ->
            Preference[Preference.TEXTAREA_FONT_SIZE] = newValue.size.toInt()
        }
        pMain.dividers[0].positionProperty().addListener { _, _, newValue ->
            Preference[Preference.MAIN_DIVIDER] = newValue
        }
        pRight.dividers[0].positionProperty().addListener { _, _, newValue ->
            Preference[Preference.RIGHT_DIVIDER] = newValue
        }

        // Bind Ctrl/Alt/Meta + Scroll with font size change
        cTransArea.addEventFilter(ScrollEvent.SCROLL) {
            if (!(isControlDown(it) || isAltDown(it))) return@addEventFilter

            val newSize = cTransArea.font.size + it.deltaY / 40

            if (newSize < 12) return@addEventFilter

            cTransArea.font = Font.font("宋体", newSize)
            showInfo("Text font size set to $newSize")
            it.consume()
        }

        // Bind selected group with clicked GroupTreeItem
        cTreeView.selectionModel.selectedItemProperty().addListener { _, _, item ->
            if (item != null) if (item.parent != null && item !is CTreeItem) {
                cGroupBox.moveTo(item.value)
            }
        }

        // Bind text and Tree
        cTreeView.addEventHandler(ScrollToEvent.ANY) {
            val item = cTreeView.selectionModel.selectedItem
            if (item != null && item is CTreeItem)
                State.currentLabelIndex = item.index
        }
        cTreeView.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            if (it.button != MouseButton.PRIMARY) return@addEventHandler

            val item = cTreeView.selectionModel.selectedItem
            if (item != null && item is CTreeItem) {
                State.currentLabelIndex = item.index
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
            if (item != null && item is CTreeItem) {
                State.currentLabelIndex = item.index
            }
        }

        // Bind Label and Tree
        cTreeView.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            if (it.button != MouseButton.PRIMARY) return@addEventHandler
            if (it.clickCount < 2) return@addEventHandler

            val item = cTreeView.selectionModel.selectedItem
            if (item != null && item is CTreeItem) {
                cLabelPane.moveToLabel(State.transFile.getTransLabelAt(State.currentPicName, item.index))
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
            if (item != null && item is CTreeItem) {
                cLabelPane.moveToLabel(State.transFile.getTransLabelAt(State.currentPicName, item.index))
            }
        }

        // Bind Tab with view mode switch
        cTreeView.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (it.code != KeyCode.TAB) return@addEventHandler

            switchViewMode()
            it.consume()
        }

        // Bind tab with work mode switch
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (it.code != KeyCode.TAB) return@addEventHandler

            cLabelPane.removeText()
            switchWorkMode()
            it.consume()
        }

        // Bind number input with group selection
        root.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (!it.code.isDigitKey) return@addEventHandler

            cGroupBox.moveTo(it.text.toInt() - 1)
        }

        // Bind Ctrl + Arrow KeyEvent with Pic change
        val arrowKeyChangePicHandler = EventHandler<KeyEvent> {
            if (!(isControlDown(it) && it.code.isArrowKey)) return@EventHandler

            when (it.code) {
                KeyCode.LEFT -> cPicBox.back()
                KeyCode.RIGHT -> cPicBox.next()
                else -> return@EventHandler
            }
        }
        root.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangePicHandler)
        cTransArea.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangePicHandler)

        // Bind Ctrl + Arrow KeyEvent with Label change
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
            } while (item !is CTreeItem)

            return index
        }
        val arrowKeyChangeLabelHandler = EventHandler<KeyEvent> {
            if (!(isControlDown(it) && it.code.isArrowKey)) return@EventHandler
            if (it.code == KeyCode.LEFT || it.code == KeyCode.RIGHT) return@EventHandler

            val now = cTreeView.selectionModel.selectedIndex
            cTreeView.selectionModel.clearSelection()

            val shift = if (it.code == KeyCode.UP) -1 else 1
            var labelItemIndex = now + shift

            val item = cTreeView.getTreeItem(labelItemIndex)
            if (item == null) labelItemIndex = getNextLabelItemIndex(labelItemIndex, -shift)
            if (item !is CTreeItem) labelItemIndex = getNextLabelItemIndex(labelItemIndex, shift)

            if (labelItemIndex == -1) {
                cTreeView.selectionModel.select(now)
                return@EventHandler
            }

            val cTreeItem = cTreeView.getTreeItem(labelItemIndex) as CTreeItem

            State.currentLabelIndex = cTreeItem.index

            cTreeView.selectionModel.select(labelItemIndex)
            cTreeView.scrollTo(labelItemIndex)
            cLabelPane.moveToLabel(cTreeItem.meta)
        }
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangeLabelHandler)
        cTransArea.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyChangeLabelHandler)
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        init()
        bind()
        reg()
    }

    private fun exit() {
        Logger.info("App exit", "Application")

        Options.save()
        Logger.stop()

        exitProcess(0)
    }
    private fun findLabelItemByIndex(index: Int): CTreeItem {
        val transLabels = State.transFile.getTransLabelListOf(State.currentPicName)
        val transLabel = transLabels.find { it.index == index }!!
        val whereToSearch = when (State.viewMode) {
            ViewMode.GroupMode -> cTreeView.root.children[transLabel.groupId]
            ViewMode.IndexMode -> cTreeView.root
        }
        val item = whereToSearch.children.find { (it as CTreeItem).meta == transLabel }!!
        return item as CTreeItem
    }
    private fun setSwitchViewModeButton(viewMode: ViewMode) {
        bSwitchViewMode.text = when (viewMode) {
            ViewMode.IndexMode -> I18N["mode.view.index"]
            ViewMode.GroupMode -> I18N["mode.view.group"]
        }
    }
    private fun setSwitchWorkModeButton(workMode: WorkMode) {
        bSwitchWorkMode.text = when (workMode) {
            WorkMode.InputMode -> I18N["mode.work.input"]
            WorkMode.LabelMode -> I18N["mode.work.label"]
        }
    }

    fun stay(): Boolean {
        // Not open
        if (!State.isOpened) return false
        // Opened but saved
        if (!State.isChanged) return false

        // Not saved
        val result = showAlert(I18N["common.exit"], null, I18N["alert.not_save.content"])
        if (result.isPresent) {
            if (result.get() == ButtonType.CANCEL) {
                return true
            }
            if (result.get() == ButtonType.YES) {
                save(File(State.transPath), FileType.getType(State.transPath), true)
            }
            return false
        }
        // Closed
        return true
    }
    fun new(file: File, type: FileType) {
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
        val result = showChoiceList(State.stage, potentialPics)
        if (result.isPresent) {
            if (result.get().isEmpty()) {
                Logger.info("Chose none, Cancel", "Controller")
                showInfo(I18N["alert.required_at_least_1_ic"])
                return
            }
            pics.addAll(result.get())
        } else {
            Logger.info("Cancel", "Controller")
            return
        }

        Logger.debug("Chose pics:", pics, "Controller")

        // Prepare new TransFile
        val groupList = ArrayList<TransGroup>()
        val groupNameList = Settings[Settings.DefaultGroupNameList].asStringList()
        val groupColorList = Settings[Settings.DefaultGroupColorList].asStringList()
        val groupCreateList = Settings[Settings.IsGroupCreateOnNewTrans].asBooleanList()
        for (i in groupNameList.indices) if (groupCreateList[i])
            groupList.add(TransGroup(groupNameList[i], groupColorList[i]))
        val transMap = HashMap<String, MutableList<TransLabel>>()
        for (pic in pics) transMap[pic] = ArrayList()

        val transFile = TransFile()
        transFile.version = TransFile.DEFAULT_VERSION
        transFile.comment = TransFile.DEFAULT_COMMENT
        transFile.groupList = groupList
        transFile.transMap = transMap

        Logger.debug("Created TransFile: $transFile", "Controller")

        // Export to file
        try {
            export(file, type, transFile)
        } catch (e: IOException) {
            Logger.error("New failed", "Controller")
            Logger.exception(e)
            showError(I18N["error.new_failed"])
            showException(e)
        }

        Logger.info("Newed TransFile", "Controller")
    }
    fun open(file: File, type: FileType) {
        Logger.info("Opening ${file.path}", "Controller")

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

        // Show info if comment not in default list
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
            showConfirm(I18N["common.info"], I18N["dialog.edited_comment.content"], comment)
        }

        State.transFile = transFile
        State.transPath = file.path

        // Update recent files
        RecentFiles.add(file.path)
        cMenuBar.updateOpenRecent()

        // Auto backup
        taskManager.refresh()
        val bakDir = File(State.getBakFolder())
        if ((bakDir.exists() && bakDir.isDirectory) || bakDir.mkdir()) {
            timer.schedule(taskManager.getTimerTask(), AUTO_SAVE_DELAY, AUTO_SAVE_PERIOD)
            Logger.info("Scheduled auto-backup", "Controller")
        } else {
            Logger.warning("Auto-backup unavailable", "Controller")
            showAlert(I18N["error.auto_backup_unavailable"])
        }

        State.isOpened = true

        // Initialize workspace
        State.stage.title = INFO["application.name"] + " - " + file.name

        updateLabelColorList()
        updateGroupList()
        updatePicList()

        Logger.info("Opened TransFile", "Controller")
    }
    fun save(file: File, type: FileType, isSilent: Boolean) {
        Logger.info("Saving to ${file.path}, isSilent:$isSilent", "Controller")

        // Check folder
        if (!isSilent) if (file.parent != State.getFileFolder()) {
            val result = showAlert(I18N["alert.save_to_another_place.content"])
            if (!(result.isPresent && result.get() == ButtonType.YES)) return
        }

        // Backup if overwrite
        var bak: File? = null
        if (State.transPath == file.path) {
            bak = File(State.transPath + EXTENSION_BAK)

            try {
                transfer(File(State.transPath), bak)
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

        State.transPath = file.path
        State.isChanged = false

        Logger.info("Saved", "Controller")
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
            pack(file, State.getFileFolder(), State.transFile)

            Logger.info("Packed to ${file.path}", "Controller")
            showInfo(I18N["info.exported_successful"])
        } catch (e : IOException) {
            Logger.error("Pack failed", "Controller")
            Logger.exception(e)
            showException(e)
            showError(I18N["error.export_failed"])
        }
    }

    fun close() {
        if (!State.isChanged) exit()

        showAlert(I18N["common.exit"], null, I18N["alert.not_save.content"]).ifPresent {
            when (it) {
                ButtonType.YES -> {
                    save(File(State.transPath), FileType.getType(State.transPath), false)
                    exit()
                }
                ButtonType.NO -> {
                    exit()
                }
                ButtonType.CANCEL -> {
                    return@ifPresent
                }
            }
        }
    }
    fun reset() {
        // cMenuBar
        cLabelPane.reset()
        // cSlider
        cPicBox.reset()
        cGroupBox.reset()
        cTreeView.reset()
        cTransArea.reset()

        setSwitchViewModeButton(State.viewMode)
        setSwitchWorkModeButton(State.workMode)
    }
    fun updatePicList() {
        cPicBox.setList(TransFile.getSortedPicList(State.transFile))

        Logger.info("Picture list updated", "Controller")
    }
    fun updateGroupList() {
        val list = List(State.transFile.groupList.size) { State.transFile.groupList[it].name }
        cGroupBox.setList(list)
        cGroupBox.moveTo(State.currentGroupId)

        Logger.info("Group list updated", "Controller")
    }
    fun updateTreeView() {
        cTreeView.update(
            State.viewMode,
            State.currentPicName,
            State.transFile.groupList,
            State.transFile.getTransLabelListOf(State.currentPicName)
        )

        Logger.info("TreeView updated", "Controller")
    }
    fun updateLabelPane() {
        if (!File(State.getPicPathNow()).exists()) {
            cLabelPane.clear()

            Logger.error("Picture `${State.currentPicName}` not exists", "Controller")
            showError(String.format(I18N["error.picture_not_exists.format.s"], State.currentPicName))
            return
        }

        cLabelPane.update(
            State.getPicPathNow(),
            State.transFile.groupList.size,
            State.transFile.getTransLabelListOf(State.currentPicName)
        )
        cLabelPane.fitToPane()
        cLabelPane.moveToZero()
        Logger.info("LabelPane updated", "Controller")
    }
    fun updateLabelColorList() {
        val list = List(State.transFile.groupList.size) { State.transFile.groupList[it].color }
        cLabelPane.colorList = FXCollections.observableList(list)

        Logger.info("LabelPane color list updated", "Controller")
    }

    // Info
    fun showInfo(text: String) {
        lInfo.text = text
    }

    // LabelPane
    fun addLabelLayer() {
        cLabelPane.createLabelLayer()

        Logger.info("Added label layer", "Controller")
    }
    fun removeLabelLayer(groupId: Int) {
        cLabelPane.removeLabelLayer(groupId)

        Logger.info("Removed label layer", "Controller")
    }

    // TreeView
    fun addGroupItem(transGroup: TransGroup) {
        cTreeView.addGroupItem(transGroup)

        Logger.info("Added group item @ $transGroup", "Controller")
    }
    fun removeGroupItem(transGroup: TransGroup) {
        cTreeView.removeGroupItem(transGroup)

        Logger.info("Removed group item @ $transGroup", "Controller")
    }
    fun updateGroupItem(name: String, transGroup: TransGroup) {
        cTreeView.updateGroupItem(name, transGroup)

        Logger.info("Updated group item (name=$name) @ $transGroup", "Controller")
    }
    fun addLabelItem(transLabel: TransLabel) {
        cTreeView.addLabelItem(transLabel)

        Logger.info("Added label item @ $transLabel", "Controller")
    }
    fun removeLabelItem(transLabel: TransLabel) {
        cTreeView.removeLabelItem(transLabel)

        Logger.info("Removed label item @ $transLabel", "Controller")
    }

    // Mode
    fun setViewMode(mode: ViewMode) {
        State.viewMode = mode

        setSwitchViewModeButton(mode)

        updateTreeView()

        Logger.info("Switched view mode to $mode", "Controller")
    }
    fun setWorkMode(mode: WorkMode) {
        State.workMode = mode

        setSwitchWorkModeButton(mode)

        setViewMode(when (mode) {
            WorkMode.InputMode -> ViewMode.getMode(Settings[Settings.ViewModePreference].asStringList()[0])
            WorkMode.LabelMode -> ViewMode.getMode(Settings[Settings.ViewModePreference].asStringList()[1])
        })

        Logger.info("Switched work mode to $mode", "Controller")
    }

    @FXML fun switchViewMode() {
        val now = ViewMode.values().indexOf(State.viewMode)
        val all = ViewMode.values().size
        setViewMode(ViewMode.values()[(now + 1) % all])
    }
    @FXML fun switchWorkMode() {
        val now = WorkMode.values().indexOf(State.workMode)
        val all = WorkMode.values().size
        setWorkMode(WorkMode.values()[(now + 1) % all])
    }

}