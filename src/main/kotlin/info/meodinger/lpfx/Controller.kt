package info.meodinger.lpfx

import info.meodinger.lpfx.component.*
import info.meodinger.lpfx.io.*
import info.meodinger.lpfx.options.*
import info.meodinger.lpfx.type.*
import info.meodinger.lpfx.util.accelerator.isControlDown
import info.meodinger.lpfx.util.dialog.*
import info.meodinger.lpfx.util.file.transfer
import info.meodinger.lpfx.util.printExceptionToErrorLog
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.INFO
import info.meodinger.lpfx.util.resource.get
import info.meodinger.lpfx.util.tree.expandAll
import info.meodinger.lpfx.util.using

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Cursor
import javafx.scene.control.*
import javafx.scene.input.*
import javafx.scene.paint.Color
import java.io.*
import java.net.URL
import java.util.*
import kotlin.system.exitProcess

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx
 */
class Controller : Initializable {

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

    private class BackupTaskManager {

        var task: TimerTask = getNewTask()

        private fun getNewTask(): TimerTask {
            return object : TimerTask() {
                override fun run() {
                    if (State.isChanged) {
                        val bak = File(State.getBakFolder() + File.separator + Date().time + EXTENSION_BAK)
                        try {
                            exportMeo(bak, State.transFile)
                        } catch (e: IOException) {
                            printExceptionToErrorLog(e)
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

        // Display dividers
        pMain.setDividerPositions(Config[Config.MAIN_DIVIDER].asDouble())
        pRight.setDividerPositions(Config[Config.RIGHT_DIVIDER].asDouble())

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

            State.transFile.groupList.forEachIndexed { index, transGroup ->
                if (transGroup.name == newValue)
                    State.currentGroupId = index
            }
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
        // Update cTreeView & cLabelPane when change pic
        State.currentPicNameProperty.addListener { _, _, _ ->
            if (!State.isOpened) return@addListener

            updateTreeView()
            updateLabelPane()
            cLabelPane.moveToZero()
        }

        // Clear text layer when change group
        State.currentGroupIdProperty.addListener { _, _, _ ->
            if (!State.isOpened) return@addListener

            cLabelPane.removeText()
        }

        // Update text when label change
        State.currentLabelIndexProperty.addListener { _, oldValue, newValue ->
            if (!State.isOpened) return@addListener

            val transLabels = State.transFile.transMap[State.currentPicName]!!

            if (oldValue != NOT_FOUND) {
                val oldLabel = transLabels.find { it.index == oldValue }
                if (oldLabel == null) {
                    cTransArea.textProperty().unbind()
                    return@addListener
                }
                cTransArea.textProperty().unbindBidirectional(oldLabel.textProperty)
                cTransArea.isBound = false
            }
            if (newValue != NOT_FOUND) {
                val newLabel = transLabels.find { it.index == newValue }
                if (newLabel == null) {
                    cTransArea.textProperty().unbind()
                    return@addListener
                }
                cTransArea.textProperty().bindBidirectional(newLabel.textProperty)
                cTransArea.isBound = true
            }
        }

        // Update cLabelPane default cursor
        State.workModeProperty.addListener { _, _, newValue ->
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
            State.transFile.getTransLabelListOf(State.currentPicName).add(transLabel)
            // Update view
            cLabelPane.createLabel(transLabel)
            cTreeView.addLabelItem(transLabel)
            // Mark change
            State.isChanged = true
        }
        cLabelPane.onLabelRemove = EventHandler {
            if (State.workMode != WorkMode.LabelMode) return@EventHandler

            val transLabel = State.transFile.getTransLabelAt(State.currentPicName, it.labelIndex)

            // Edit data
            State.transFile.getTransLabelListOf(State.currentPicName).remove(transLabel)
            for (l in State.transFile.getTransLabelListOf(State.currentPicName)) {
                if (l.index > transLabel.index) {
                    l.index -= 1
                }
            }
            // Update view
            cLabelPane.removeLabel(transLabel)
            cTreeView.removeLabelItem(transLabel)
            // Mark change
            State.isChanged = true
        }
        cLabelPane.onLabelPointed = EventHandler {} // Text display already set on Label
        cLabelPane.onLabelClicked = EventHandler {
            if (State.workMode != WorkMode.InputMode) return@EventHandler

            val transLabel = State.transFile.getTransLabelAt(State.currentPicName, it.labelIndex)

            if (it.source.clickCount > 1) cLabelPane.moveToLabel(transLabel)
        }
        cLabelPane.onLabelOther = EventHandler {
            if (State.workMode != WorkMode.LabelMode) return@EventHandler

            val transGroup = State.transFile.getTransGroupAt(State.currentGroupId)

            cLabelPane.removeText()
            cLabelPane.createText(transGroup.name, Color.web(transGroup.color), it.displayX, it.displayY)
        }

        // Update config
        pMain.dividers[0].positionProperty().addListener { _, _, newValue ->
            Config[Config.MAIN_DIVIDER] = newValue
        }
        pRight.dividers[0].positionProperty().addListener { _, _, newValue ->
            Config[Config.RIGHT_DIVIDER] = newValue
        }

        // Update selected group when clicked GroupTreeItem
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
            val item = cTreeView.selectionModel.selectedItem
            if (item != null && item is CTreeItem)
                State.currentLabelIndex = item.index
        }
        cTreeView.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (it.code.isArrowKey) {

                // shift
                val shift = if (it.code == KeyCode.UP) -1 else if (it.code == KeyCode.DOWN) 1 else 0
                val item = cTreeView.getTreeItem(cTreeView.selectionModel.selectedIndex + shift)

                if (item != null && item is CTreeItem)
                    State.currentLabelIndex = item.index
            }
        }

        // Bind Label and Tree
        cLabelPane.selectedLabelIndexProperty.addListener { _, _, index ->
            if (index == NOT_FOUND) return@addListener

            val item = findLabelItemByIndex(index as Int)
            cTreeView.selectionModel.clearSelection()
            cTreeView.selectionModel.select(item)
            cTreeView.scrollTo(cTreeView.getRow(item))
        }
        cTreeView.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            if (it.clickCount < 2) return@addEventHandler

            val item = cTreeView.selectionModel.selectedItem
            if (item != null && item is CTreeItem) {
                cLabelPane.moveToLabel(State.transFile.transMap[State.currentPicName]!!.find { e -> e.index == item.index }!!)
            }
        }
        cTreeView.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (it.code.isArrowKey) {

                // shift
                val shift = if (it.code == KeyCode.UP) -1 else if (it.code == KeyCode.DOWN) 1 else 0
                val item = cTreeView.getTreeItem(cTreeView.selectionModel.selectedIndex + shift)

                if (item != null && item is CTreeItem)
                    cLabelPane.moveToLabel(State.transFile.transMap[State.currentPicName]!!.find { e -> e.index == item.index }!!)
            }
        }

        // Bind number input with group selection
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (it.code.isDigitKey)
                cGroupBox.moveTo(it.text.toInt() - 1)
        }

        // Bind tab with work mode switch
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.TAB) {
                cLabelPane.removeText()
                switchWorkMode()
                it.consume()
            }
        }

        // Bind Tab with view mode switch
        cTreeView.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.TAB) {
                switchViewMode()
                it.consume()
            }
        }

        // Bind Arrow KeyEvent with Label change and Pic change
        val arrowKeyListener = EventHandler<KeyEvent> {
            if (isControlDown(it) && it.code.isArrowKey) {
                when (it.code) {
                    KeyCode.UP, KeyCode.DOWN -> {

                        // Shift
                        val shift = if (it.code == KeyCode.UP) -1 else 1

                        var index = cTreeView.selectionModel.selectedIndex + shift
                        cTreeView.selectionModel.clearSelection()
                        // if item == null (to the end), vTree select nothing, return to top

                        var item = cTreeView.getTreeItem(index)
                        if (item != null) {
                            index = if (item is CTreeItem) {
                                // Label
                                cTreeView.selectionModel.select(index)
                                State.currentLabelIndex = item.index
                                cLabelPane.moveToLabel(item.meta)
                                return@EventHandler
                            } else if (item.parent != null) {
                                // Group
                                item.isExpanded = true
                                index + shift
                            } else {
                                // Root
                                item.expandAll()
                                if (State.viewMode === ViewMode.GroupMode) index++
                                while (cTreeView.getTreeItem(index).children.size == 0) {
                                    index++
                                    if (cTreeView.getTreeItem(index) == null) break
                                }
                                index + shift
                            }

                            cTreeView.selectionModel.select(index)
                            item = cTreeView.getTreeItem(index)
                            if (item == null) return@EventHandler

                            if (item is CTreeItem) {
                                // Label
                                State.currentLabelIndex = item.index
                                cLabelPane.moveToLabel(item.meta)
                            }
                        }
                    }
                    KeyCode.LEFT -> cPicBox.back()
                    KeyCode.RIGHT -> cPicBox.next()
                    else -> return@EventHandler
                }
            }
        }
        cTransArea.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyListener)
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyListener)
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        init()
        bind()
        reg()
    }

    private fun findLabelItemByIndex(index: Int): CTreeItem {
        val transLabels = State.transFile.transMap[State.currentPicName]!!
        val transLabel = transLabels.find { it.index == index }!!
        val whereToSearch = when (State.viewMode) {
            ViewMode.GroupMode -> cTreeView.root.children[transLabel.groupId]
            ViewMode.IndexMode -> cTreeView.root
        }
        val item = whereToSearch.children.find { (it as CTreeItem).meta == transLabel }!!
        return item as CTreeItem
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
                save(File(State.transPath), getFileType(State.transPath))
            }
            return false
        }
        // Closed
        return true
    }
    fun new(file: File, type: FileType) {
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
            pics.addAll(result.get())
        } else {
            return
        }

        // Prepare new TransFile
        val groupList = ArrayList<TransGroup>()
        val groupNameList = Settings[Settings.DefaultGroupList].asStringList()
        val groupColorList = Settings[Settings.DefaultColorList].asStringList()
        for (i in groupNameList.indices) groupList.add(TransGroup(groupNameList[i], groupColorList[i]))

        val transMap = HashMap<String, MutableList<TransLabel>>()
        for (pic in pics) transMap[pic] = ArrayList()

        val transFile = TransFile()
        transFile.version = TransFile.DEFAULT_VERSION
        transFile.comment = TransFile.DEFAULT_COMMENT
        transFile.groupList = groupList
        transFile.transMap = transMap

        // Export to file
        try {
            when (type) {
                FileType.LPFile -> exportLP(file, transFile)
                FileType.MeoFile -> exportMeo(file, transFile)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            showError(I18N["error.new_failed"])
            showException(e)
        }
    }
    fun open(file: File, type: FileType) {
        val transFile: TransFile
        try {
            transFile = when (type) {
                FileType.LPFile -> loadLP(file)
                FileType.MeoFile -> loadMeo(file)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            showError(I18N["error.open_failed"])
            showException(e)
            return
        }

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
        } else {
            showError(I18N["error.auto_backup_unavailable"])
        }

        State.isOpened = true

        // Initialize workspace
        State.stage.title = INFO["application.name"] + " - " + file.name

        updateLabelColorList()
        updateGroupList()
        updatePicList()
    }
    fun save(file: File, type: FileType) {

        // Check folder
        if (file.parent != State.getFileFolder()) {
            val result = showAlert(I18N["alert.save_to_another_place.content"])
            if (!(result.isPresent && result.get() == ButtonType.YES)) return
        }

        // Backup if overwrite
        var bak: File? = null
        if (State.transPath == file.path) {
            bak = File(State.transPath + EXTENSION_BAK)

            try {
                transfer(File(State.transPath), bak)
            } catch (e: Exception) {
                bak = null
                e.printStackTrace()
                showError(I18N["error.backup_failed"])
                showException(e)
            }
        }

        // Export
        try {
            when (type) {
                FileType.LPFile -> exportLP(file, State.transFile)
                FileType.MeoFile -> exportMeo(file, State.transFile)
            }
            showInfo(I18N["info.saved_successfully"])
        } catch (e: IOException) {
            e.printStackTrace()
            if (bak != null) {
                showError(String.format(I18N["error.save_failed_backed.format"], bak.path))
            } else {
                showError(I18N["error.save_failed"])
            }
            showException(e)
            return
        }

        // Remove Backup
        if (bak != null) if (!bak.delete()) showError(I18N["error.backup_clear_failed"])

        State.transPath = file.path
        State.isChanged = false
    }

    fun close() {
        if (!State.isChanged) exitProcess(0)

        showAlert(I18N["common.exit"], null, I18N["alert.not_save.content"]).ifPresent {
            when (it) {
                ButtonType.YES -> {
                    save(File(State.transPath), getFileType(State.transPath))
                    exitProcess(0)
                }
                ButtonType.NO -> {
                    exitProcess(0)
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
        // cTransArea
    }
    fun updatePicList() {
        cPicBox.setList(TransFile.getSortedPicList(State.transFile))
    }
    fun updateGroupList() {
        val list = List(State.transFile.groupList.size) { State.transFile.groupList[it].name }
        cGroupBox.setList(list)
        cGroupBox.moveTo(State.currentGroupId)
    }
    fun updateTreeView() {
        cTreeView.update(
            State.viewMode,
            State.currentPicName,
            State.transFile.groupList,
            State.transFile.getTransLabelListOf(State.currentPicName))
    }
    fun updateLabelPane() {
        cLabelPane.update(
            State.getPicPathNow(),
            State.transFile.groupList.size,
            State.transFile.transMap[State.currentPicName]!!
        )
    }
    fun updateLabelColorList() {
        val list = List(State.transFile.groupList.size) { State.transFile.groupList[it].color }
        cLabelPane.colorList = FXCollections.observableList(list)
    }
    fun addLabelLayer() {
        cLabelPane.createLabelLayer()
    }
    fun delLabelLayer(groupId: Int) {
        cLabelPane.removeLabelLayer(groupId)
    }

    fun setViewMode(mode: ViewMode) {
        State.viewMode = mode

        bSwitchViewMode.text = when (mode) {
            ViewMode.IndexMode -> I18N["mode.view.index"]
            ViewMode.GroupMode -> I18N["mode.view.group"]
        }

        updateTreeView()
    }
    fun setWorkMode(mode: WorkMode) {
        State.workMode = mode

        bSwitchWorkMode.text = when (mode) {
            WorkMode.InputMode -> {
                setViewMode(ViewMode.IndexMode)
                I18N["mode.work.input"]
            }
            WorkMode.LabelMode -> {
                setViewMode(ViewMode.GroupMode)
                I18N["mode.work.label"]
            }
        }
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