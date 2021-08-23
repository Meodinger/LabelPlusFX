package info.meodinger.lpfx

import info.meodinger.lpfx.component.*
import info.meodinger.lpfx.component.CLabelPane.Companion.NOT_FOUND
import info.meodinger.lpfx.io.exportLP
import info.meodinger.lpfx.io.exportMeo
import info.meodinger.lpfx.io.loadLP
import info.meodinger.lpfx.io.loadMeo
import info.meodinger.lpfx.options.*
import info.meodinger.lpfx.type.*
import info.meodinger.lpfx.util.accelerator.isControlDown
import info.meodinger.lpfx.util.dialog.*
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.INFO
import info.meodinger.lpfx.util.resource.get
import info.meodinger.lpfx.util.tree.expandAll
import info.meodinger.lpfx.util.using

import javafx.application.Platform
import javafx.beans.binding.ListBinding
import javafx.beans.binding.StringBinding
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.input.*
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.util.Callback
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
    @FXML private lateinit var pText: AnchorPane
    @FXML private lateinit var cMenuBar: CMenuBar
    @FXML private lateinit var cLabelPane: CLabelPane
    @FXML private lateinit var cSlider: CTextSlider
    @FXML private lateinit var cPicBox: CComboBox<String>
    @FXML private lateinit var cGroupBox: CComboBox<String>
    @FXML private lateinit var cTreeView: CTreeView
    @FXML private lateinit var cTransArea: CTransArea

    private inner class BackupTaskManager {

        var task: TimerTask = getNewTask()

        private fun getNewTask(): TimerTask {
            return object : TimerTask() {
                override fun run() {
                    if (State.isChanged) {
                        val bak = File(State.getBakFolder() + File.separator + Date().time + EXTENSION_BAK)
                        try {
                            exportMeo(bak, State.transFile)
                        } catch (e: IOException) {
                            using {
                                val writer = PrintWriter(
                                    BufferedWriter(
                                        FileWriter(
                                            Options.errorLog.resolve(Date().toString()).toFile()
                                        )
                                    )
                                ).autoClose()
                                e.printStackTrace(writer)
                            } catch { ex: Exception ->
                                ex.printStackTrace()
                            } finally {

                            }
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

    override fun initialize(location: URL?, resources: ResourceBundle?) {

        // ----- Component Initialize ----- //

        // Set last used dir
        val lastFilePath = RecentFiles.getLastOpenFile()
        if (lastFilePath != null) CFileChooser.lastDirectory = File(lastFilePath).parentFile

        // Update OpenRecent
        cMenuBar.updateOpenRecent()

        // Set menu disabled
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

        // ----- Property bindings ----- //

        // cPicBox - currentPicName
        cPicBox.valueProperty.addListener { _, _, newValue ->
            if (!State.isOpened) return@addListener

            State.currentPicName = newValue
        }
        // cGroupBox - currentGroupId
        cGroupBox.valueProperty.addListener { _, _, newValue ->
            if (!State.isOpened) return@addListener

            State.transFile.groupList.forEachIndexed { index, transGroup ->
                if (transGroup.name == cGroupBox.valueProperty.value)
                    State.currentGroupId = index
            }
        }

        // tTransText - transLabel.text
        cTransArea.textProperty().bind(object : StringBinding() {
            init {
                bind(State.currentLabelIndexProperty)
            }

            override fun computeValue(): String {
                if (!State.isOpened) return ""

                val index = State.currentLabelIndex
                val transLabels = State.transFile.transMap[State.currentPicName]!!
                val transLabel = transLabels.find { it.index == index }!!
                return transLabel.text
            }

        })

        // cSlider - cLabelPane#scale
        cSlider.scaleProperty.bindBidirectional(cLabelPane.scaleProperty)

        // ----- Listeners & Handlers ----- //

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
            cLabelPane.placeLabel(transLabel)
            updateTreeView()
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
            // Pane will update through bind
            updateTreeView()
            // Mark change
            State.isChanged = true
        }
        cLabelPane.onLabelPointed = EventHandler {
            if (State.workMode != WorkMode.InputMode) return@EventHandler
            val transLabel = State.transFile.getTransLabelAt(State.currentPicName, it.labelIndex)

            cLabelPane.removeText()
            cLabelPane.placeText(transLabel.text, Color.BLACK, it.rootX, it.rootY * cLabelPane.imageHeight)
        }
        cLabelPane.onLabelClicked = EventHandler {
            if (State.workMode != WorkMode.InputMode) return@EventHandler
            val transLabel = State.transFile.getTransLabelAt(State.currentPicName, it.labelIndex)

            cTransArea.textProperty().unbind()
            cTransArea.textProperty().bindBidirectional(transLabel.textProperty)
        }
        cLabelPane.onLabelOther = EventHandler {
            if (State.workMode != WorkMode.LabelMode) return@EventHandler
            val transGroup = State.transFile.getTransGroupAt(State.currentGroupId)
            cLabelPane.removeText()
            cLabelPane.placeText(transGroup.name, Color.web(transGroup.color), it.rootX, it.rootY)
        }

        // Update config
        pMain.dividers[0].positionProperty().addListener { _, _, newValue ->
            Config[Config.MAIN_DIVIDER].value = newValue.toString()
        }
        pRight.dividers[0].positionProperty().addListener { _, _, newValue ->
            Config[Config.RIGHT_DIVIDER].value = newValue.toString()
        }

        // Fix dividers when resize
        val geometryListener = ChangeListener<Number> { _, _, _ ->
            // Now only god knows its efffect
            val debounce = { input: Double -> (input * 100 + 0.2) / 100 }
            pMain.setDividerPositions(debounce(pMain.dividerPositions[0]))
            pRight.setDividerPositions(debounce(pRight.dividerPositions[0]))
        }
        State.stage.widthProperty().addListener(geometryListener)
        State.stage.heightProperty().addListener(geometryListener)

        // TreeView update
        State.isChangedProperty.addListener { _, _, _ ->
            if (!State.isOpened) return@addListener

            updateTreeView()
        }

        // GroupBox update
        State.isChangedProperty.addListener { _, _, _ ->
            if (!State.isOpened) return@addListener

            updateGroupList()
        }

        // Update vTree & cLabelPane when change pic
        State.currentPicNameProperty.addListener { _, _, newValue ->
            if (!State.isOpened) return@addListener

            val transLabels = State.transFile.transMap[newValue]!!

            updateTreeView()
            cLabelPane.update(
                State.getPicPathNow(),
                State.transFile.groupList.size,
                transLabels
            )
            cLabelPane.moveToZero()

            if (false) {
                if (transLabels.size > 0) {
                    val item = findLabelItemByIndex(transLabels[0].index)
                    cTreeView.selectionModel.select(item)
                    cLabelPane.moveToLabel(item.meta)
                    cLabelPane.selectedLabelIndex = item.index
                    State.currentLabelIndex = item.index
                }
            }
        }

        // Clear text layer when change group
        State.currentGroupIdProperty.addListener { _, _, _ ->
            if (!State.isOpened) return@addListener

            cLabelPane.removeText()
        }

        // Update selected group when clicked GroupTreeItem
        cTreeView.selectionModel.selectedItemProperty().addListener { _, _, item ->
            if (item != null) if (item.parent != null && item !is CTreeItem) {
                cGroupBox.moveTo(State.getGroupIdByName(item.value))
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
        // Bind Arrow KeyEvent with Label change and Pic change
        cTransArea.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyListener)
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyListener)

    }

    private fun createColorHexBinding(): ListBinding<String> {
        return object : ListBinding<String>() {
            init {
                for (i in 0 until State.transFile.groupList.size) {
                    bind(State.transFile.groupList[i].colorProperty)
                }
            }

            override fun computeValue(): ObservableList<String> {
                val list = ArrayList<String>()
                State.transFile.groupList.forEach {
                    list.add(it.color)
                }
                return FXCollections.observableList(list)
            }
        }
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
        val result = showAlert(I18N["common.exit"], null, I18N["dialog.exit_save_alert.content"])
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
        val groupNameList = Settings[Settings.DefaultGroupList].asList()
        val groupColorList = Settings[Settings.DefaultColorList].asList()
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
            showException(e)
            showError(I18N["error.new_failed"])
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
            showException(e)
            showError(I18N["error.open_failed"])
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

        // Initialize workspace
        State.controller.reset()
        State.stage.title = INFO["application.name"] + " - " + file.name

        State.controller.updateLabelColorList()
        State.controller.updatePicList()
        State.controller.updateGroupList()

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

            using {
                val input = FileInputStream(State.transPath).channel.autoClose()
                val output = FileInputStream(bak!!).channel.autoClose()

                output.transferFrom(input, 0, input.size())
            } catch { e: Exception ->
                showException(IOException(I18N["error.backup_failed"], e))
                bak = null
            } finally {

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
            showException(e)
            if (bak != null) {
                showError(String.format(I18N["error.save_failed_backed.format"], bak!!.path))
            } else {
                showError(I18N["error.save_failed"])
            }
            return
        }

        // Remove Backup
        if (!(bak != null && bak!!.delete())) {
            showError(I18N["error.backup_clear_failed"])
        }

        State.transPath = file.path
        State.isChanged = false
    }

    fun close() {
        if (!State.isChanged) exitProcess(0)

        showAlert(I18N["common.exit"], null, I18N["dialog.exit_save_alert.content"]).ifPresent {
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
        cTreeView.root = null
        bSwitchWorkMode.text = I18N["mode.work.input"]
        bSwitchViewMode.text = I18N["mode.view.index"]

        cPicBox.reset()
        cGroupBox.reset()
        cLabelPane.reset()
        // cSlider will reset with cImagePane through bind
    }
    fun addLabelLayer() {
        cLabelPane.placeLabelLayer()
    }
    fun delLabelLayer(groupId: Int) {
        cLabelPane.removeLabelLayer(groupId)
    }
    fun updateLabelColorList() {
        cLabelPane.colorListProperty.bind(createColorHexBinding())
    }
    fun updatePicList() {
        cPicBox.setList(TransFile.getSortedPicList(State.transFile))
    }
    fun updateGroupList() {
        val list = ArrayList<String>()
        for (transGroup in State.transFile.groupList)
            list.add(transGroup.name)
        cGroupBox.setList(list)
        cGroupBox.moveTo(State.currentGroupId)
    }
    fun updateTreeView() {
        when (State.viewMode) {
            ViewMode.GroupMode -> updateTreeViewByGroup()
            ViewMode.IndexMode -> updateTreeViewByIndex()
        }
        cTreeView.root.expandAll()
    }
    private fun updateTreeViewByGroup() {
        val transLabels = State.transFile.getTransLabelListOf(State.currentPicName)
        val rootItem = TreeItem(State.currentPicName)
        val groupItems = ArrayList<TreeItem<String>>()

        for (transGroup in State.transFile.groupList) {
            val circle = Circle(GRAPHICS_CIRCLE_RADIUS, Color.web(transGroup.color))
            val groupItem = TreeItem(transGroup.name, circle)
            groupItems.add(groupItem)
            rootItem.children.add(groupItem)
        }
        for (transLabel in transLabels) {
            groupItems[transLabel.groupId].children.add(CTreeItem(transLabel))
        }

        cTreeView.root = rootItem
    }
    private fun updateTreeViewByIndex() {
        val transLabels = State.transFile.getTransLabelListOf(State.currentPicName)
        val rootItem = TreeItem(State.currentPicName)

        for (transLabel in transLabels) {
            val transGroup = State.transFile.groupList[transLabel.groupId]
            val circle = Circle(GRAPHICS_CIRCLE_RADIUS, Color.web(transGroup.color))
            rootItem.children.add(CTreeItem(transLabel, circle))
        }

        cTreeView.root = rootItem
    }

    @FXML fun switchViewMode() {
        val now = ViewMode.values().indexOf(State.viewMode)
        val all = ViewMode.values().size
        State.viewMode = ViewMode.values()[(now + 1) % all]

        when (State.viewMode) {
            ViewMode.IndexMode -> bSwitchViewMode.text = I18N["mode.view.index"]
            ViewMode.GroupMode -> bSwitchViewMode.text = I18N["mode.view.group"]
        }
        updateTreeView()
    }
    @FXML fun switchWorkMode() {
        val now = WorkMode.values().indexOf(State.workMode)
        val all = WorkMode.values().size
        State.workMode = WorkMode.values()[(now + 1) % all]
        when (State.workMode) {
            WorkMode.InputMode -> {
                bSwitchWorkMode.text = I18N["mode.work.input"]
                State.viewMode = ViewMode.IndexMode
            }
            WorkMode.LabelMode -> {
                bSwitchWorkMode.text = I18N["mode.work.label"]
                State.viewMode = ViewMode.GroupMode
            }
        }
    }

}