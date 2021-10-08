package info.meodinger.lpfx

import info.meodinger.lpfx.component.*
import info.meodinger.lpfx.component.common.CComboBox
import info.meodinger.lpfx.component.common.CFileChooser
import info.meodinger.lpfx.component.common.CTextSlider
import info.meodinger.lpfx.component.common.CLigatureArea
import info.meodinger.lpfx.component.singleton.AMenuBar
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
import info.meodinger.lpfx.util.component.expandAll
import info.meodinger.lpfx.util.doNothing

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Insets
import javafx.scene.Cursor
import javafx.scene.control.*
import javafx.scene.input.*
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import java.io.*
import java.net.URL
import java.util.*


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

        // Warp cPicBox
        cPicBox.isWrapped = true

        // Warp text
        cTransArea.isWrapText = true

        // Load rules
        updateLigatureRules()

        // lInfo padding
        lInfo.padding = Insets(4.0, 8.0, 4.0, 8.0)

        // Set comp disabled
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
            if (State.currentGroupId == NOT_FOUND) return@EventHandler

            val transLabel = TransLabel(
                it.labelIndex,
                State.currentGroupId, it.labelX, it.labelY, ""
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

            // Edit data
            State.removeTransLabel(State.currentPicName, it.labelIndex)
            for (label in State.transFile.getTransList(State.currentPicName)) {
                if (label.index > it.labelIndex) {
                    State.setTransLabelIndex(State.currentPicName, label.index, label.index - 1)
                }
            }
            // Update view
            cLabelPane.removeLabel(it.labelIndex)
            removeLabelItem(it.labelIndex)
            // Mark change
            State.isChanged = true
        }
        cLabelPane.onLabelPointed = EventHandler {
            val transLabel = State.transFile.getTransLabel(State.currentPicName, it.labelIndex)

            // Text display
            cLabelPane.removeText()
            cLabelPane.createText(transLabel.text, Color.BLACK, it.displayX, it.displayY)
        }
        cLabelPane.onLabelClicked = EventHandler {
            if (State.workMode != WorkMode.InputMode) return@EventHandler

            if (it.source.clickCount > 1) cLabelPane.moveToLabel(it.labelIndex)

            cTreeView.select(it.labelIndex)
        }
        cLabelPane.onLabelOther = EventHandler {
            if (State.workMode != WorkMode.LabelMode) return@EventHandler
            if (State.currentGroupId == NOT_FOUND) return@EventHandler

            val transGroup = State.transFile.getTransGroup(State.currentGroupId)

            cLabelPane.removeText()
            cLabelPane.createText(transGroup.name, Color.web(transGroup.colorHex), it.displayX, it.displayY)
        }
        cLabelPane.onLabelMove = EventHandler {
            State.isChanged = true
        }

        // Preferences
        cTransArea.font = Font.font("SimSun", Preference[Preference.TEXTAREA_FONT_SIZE].asDouble())
        pMain.setDividerPositions(Preference[Preference.MAIN_DIVIDER].asDouble())
        pRight.setDividerPositions(Preference[Preference.RIGHT_DIVIDER].asDouble())

        // View Mode
        val viewModes = Settings[Settings.ViewModePreference].asStringList()
        State.viewMode = ViewMode.getMode(viewModes[WorkMode.InputMode.ordinal])
        cTreeView.viewMode = State.viewMode
        bSwitchWorkMode.text = I18N["mode.work.input"]
        bSwitchViewMode.text = State.viewMode.description

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
        cTransArea.textProperty().addListener { _, _, _ ->
            if (cTransArea.isBound) State.isChanged = true
        }

        // currentPicName
        cPicBox.valueProperty.addListener { _, _, newValue ->
            State.currentPicName = newValue ?: ""
        }

        // currentGroupId
        cGroupBox.indexProperty.addListener { _, _, newValue ->
            State.currentGroupId = newValue as Int
        }
        cTreeView.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            // Bind selected group with clicked GroupTreeItem
            if (newValue != null && newValue.parent != null && newValue !is CTreeLabelItem) {
                State.currentGroupId = State.transFile.getGroupIdByName(newValue.value)
            }
        }

        // currentLabelIndex
        State.currentPicNameProperty.addListener { _ , _, _ ->
            // Clear selected when change pic
            State.currentLabelIndex = NOT_FOUND
        }
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
        State.currentPicNameProperty.addListener { _, oldValue, newValue ->
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

            renderTreeView()

            labelInfo("Switch view mode to $newMode")
        }

        // Bind Ctrl/Alt/Meta + Scroll with font size change
        cTransArea.addEventFilter(ScrollEvent.SCROLL) {
            if (!(isControlDown(it) || isAltDown(it))) return@addEventFilter

            val newSize = cTransArea.font.size + it.deltaY / 40

            if (newSize < 12) return@addEventFilter

            cTransArea.font = Font.font("SimSun", newSize) // Song
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

    private fun exitApplication() {
        State.stage.close()
        Logger.info("App exit", "Application")

        Options.save()
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
        val result = showChoiceList(State.stage, potentialPics)
        if (result.isPresent) {
            if (result.get().isEmpty()) {
                Logger.info("Chose none, Cancel", "Controller")
                showInfo(I18N["alert.required_at_least_1_ic"])
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
        AMenuBar.updateOpenRecent()

        // Auto backup
        taskManager.refresh()
        val bakDir = File(State.getBakFolder())
        if ((bakDir.exists() && bakDir.isDirectory) || bakDir.mkdir()) {
            timer.schedule(taskManager.getTimerTask(), AUTO_SAVE_DELAY, AUTO_SAVE_PERIOD)
            Logger.info("Scheduled auto-backup", "Controller")
        } else {
            Logger.warning("Auto-backup unavailable", "Controller")
            showError(I18N["error.auto_backup_unavailable"])
        }

        State.isOpened = true

        // Initialize workspace
        State.stage.title = INFO["application.name"] + " - " + file.name

        updateLabelColorList()
        renderGroupBox()
        renderGroupBar()
        updatePicList()

        Logger.info("Opened TransFile", "Controller")
    }
    fun save(file: File, type: FileType, isSilent: Boolean) {
        Logger.info("Saving to ${file.path}, isSilent:$isSilent", "Controller")

        // Check folder
        if (!isSilent) if (file.parent != State.getFileFolder()) {
            val result = showConfirm(I18N["alert.save_to_another_place.content"])
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

    fun exit() {
        if (!State.isChanged) {
            exitApplication()
            return
        }

        showAlert(I18N["common.exit"], null, I18N["alert.not_save.content"]).ifPresent {
            when (it) {
                ButtonType.YES -> {
                    save(File(State.transPath), FileType.getType(State.transPath), false)
                    exitApplication()
                }
                ButtonType.NO -> {
                    exitApplication()
                }
                ButtonType.CANCEL -> {
                    return@ifPresent
                }
            }
        }
    }
    fun reset() {
        // cMenuBar
        cGroupBar.reset()
        cLabelPane.reset()
        // cSlider
        cPicBox.reset()
        cGroupBox.reset()
        cTreeView.reset()
        cTransArea.reset()
    }

    // ----- Re-init component ----- //
    fun updateLigatureRules() {
        cTransArea.ligatureRules = FXCollections.observableList(Settings[Settings.LigatureRules].asPairList())
    }

    // ----- Picture Display ----- //
    fun updatePicList() {
        val pics = State.transFile.sortedPicNames
        cPicBox.setList(pics)

        Logger.info("Picture list updated", "Controller")
        Logger.debug("List is", pics, "Controller")
    }

    // ----- Group Display ----- //
    fun renderGroupBox() {
        cGroupBox.reset()
        cGroupBox.setList(State.transFile.groupNames)
        cGroupBox.moveTo(if (State.currentGroupId == NOT_FOUND) 0 else State.currentGroupId)

        Logger.info("Group box updated", "Controller")
        Logger.debug("List is", State.transFile.groupNames, "Controller")
    }
    fun renderGroupBar() {
        cGroupBar.reset()
        cGroupBar.render(State.transFile.groups)
        cGroupBar.select(if (State.currentGroupId == NOT_FOUND) 0 else State.currentGroupId)

        Logger.info("Group bar updated", "Controller")
        Logger.debug("List is", lazy {
            List(State.transFile.groupCount) {
                TransGroup(State.transFile.groupNames[it], State.transFile.groupColors[it])
            }
        }, "Controller")
    }

    fun addGroupBar(transGroup: TransGroup) {
        cGroupBar.addGroup(transGroup)
    }
    fun removeGroupBar(targetName: String) {
        cGroupBar.removeGroup(targetName)
    }

    // ----- LabelPane ----- //
    fun renderLabelPane() {
        if (!File(State.getPicPathNow()).exists()) {
            cLabelPane.clear()

            Logger.error("Picture `${State.currentPicName}` not exists", "Controller")
            showError(String.format(I18N["error.picture_not_exists.format.s"], State.currentPicName))
            return
        }

        cLabelPane.render(
            State.getPicPathNow(),
            State.transFile.groupCount,
            State.transFile.getTransList(State.currentPicName)
        )

        when (Settings[Settings.ScaleOnNewPicture].asInteger()) {
            0 -> cLabelPane.scale = 1.0 // 100%
            1 -> cLabelPane.fitToPane() // Fit
            2 -> doNothing() // Last
        }

        cLabelPane.moveToZero()
        Logger.info("LabelPane updated", "Controller")
    }
    fun updateLabelColorList() {
        cLabelPane.colorHexList = FXCollections.observableList(State.transFile.groupColors)

        Logger.info("LabelPane color list updated", "Controller")
        Logger.debug("List is", State.transFile.groupColors, "Controller")
    }
    fun updateLabelColor(groupId: Int, hex: String) {
        cLabelPane.updateColor(groupId, hex)
    }

    fun addLabelLayer() {
        cLabelPane.createLabelLayer()

        Logger.info("Added label layer", "Controller")
    }
    fun removeLabelLayer(groupId: Int) {
        cLabelPane.removeLabelLayer(groupId)

        Logger.info("Removed label layer", "Controller")
    }

    // ----- TreeView ----- //
    fun renderTreeView() {
        cTreeView.render(
            State.viewMode,
            State.currentPicName,
            State.transFile.groups,
            State.transFile.getTransList(State.currentPicName)
        )

        Logger.info("TreeView updated", "Controller")
    }

    fun addGroupItem(transGroup: TransGroup) {
        cTreeView.addGroupItem(transGroup)

        Logger.info("Added group item @ $transGroup", "Controller")
    }
    fun removeGroupItem(groupName: String) {
        cTreeView.removeGroupItem(groupName)

        Logger.info("Removed group item @ $groupName", "Controller")
    }

    fun addLabelItem(transLabel: TransLabel) {
        cTreeView.addLabelItem(transLabel)

        Logger.info("Added label item @ $transLabel", "Controller")
    }
    fun removeLabelItem(labelIndex: Int) {
        cTreeView.removeLabelItem(labelIndex)

        Logger.info("Removed label item @ $labelIndex", "Controller")
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

}