package info.meodinger.lpfx

import info.meodinger.lpfx.component.*
import info.meodinger.lpfx.component.CLabelPane.Companion.NOT_FOUND
import info.meodinger.lpfx.io.*
import info.meodinger.lpfx.options.*
import info.meodinger.lpfx.type.*
import info.meodinger.lpfx.util.dialog.*
import info.meodinger.lpfx.util.keyboard.isControlDown
import info.meodinger.lpfx.util.platform.isMac
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
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.input.*
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.stage.FileChooser
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

    @FXML private lateinit var tTransText: CTransArea
    @FXML private lateinit var bSwitchViewMode: Button
    @FXML private lateinit var bSwitchWorkMode: Button
    @FXML private lateinit var pMain: SplitPane
    @FXML private lateinit var pRight: SplitPane
    @FXML private lateinit var pText: AnchorPane
    @FXML private lateinit var vTree: CTreeView
    @FXML private lateinit var cSlider: CTextSlider
    @FXML private lateinit var cPicBox: CComboBox<String>
    @FXML private lateinit var cGroupBox: CComboBox<String>
    @FXML private lateinit var cLabelPane: CLabelPane
    @FXML private lateinit var mmFile: Menu
    @FXML private lateinit var mNew: MenuItem
    @FXML private lateinit var mOpen: MenuItem
    @FXML private lateinit var mOpenRecent: Menu
    @FXML private lateinit var mSave: MenuItem
    @FXML private lateinit var mSaveAs: MenuItem
    @FXML private lateinit var mBakRecover: MenuItem
    @FXML private lateinit var mClose: MenuItem
    @FXML private lateinit var mmExport: Menu
    @FXML private lateinit var mExportAsLp: MenuItem
    @FXML private lateinit var mExportAsMeo: MenuItem
    @FXML private lateinit var mExportAsMeoPack: MenuItem
    @FXML private lateinit var mEditComment: MenuItem
    @FXML private lateinit var mmAbout: Menu
    @FXML private lateinit var mAbout: MenuItem

    private val fileFilter = FileChooser.ExtensionFilter(I18N["filetype.translation"], "*${EXTENSION_MEO}", "*${EXTENSION_LP}")
    private val meoFilter = FileChooser.ExtensionFilter(I18N["filetype.translation_meo"], "*${EXTENSION_MEO}")
    private val lpFilter = FileChooser.ExtensionFilter(I18N["filetype.translation_lp"], "*${EXTENSION_LP}")
    private val bakFilter = FileChooser.ExtensionFilter(I18N["filetype.bak"], "*${EXTENSION_BAK}")
    private val packFilter = FileChooser.ExtensionFilter(I18N["filetype.pack"], "*${EXTENSION_PACK}")
    private val fileChooser = CFileChooser()
    private val bakChooser = CFileChooser()
    private val exportChooser = CFileChooser()
    private val exportPackChooser = CFileChooser()

    private inner class BackupTaskManager {

        var task: TimerTask = getNewTask()

        private fun getNewTask(): TimerTask {
            return object : TimerTask() {
                override fun run() {
                    if (State.isChanged) {
                        this@Controller.silentBackup()
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

    init {
        State.controllerAccessor = object : State.ControllerAccessor {
            override fun close() {
                this@Controller.close()
            }

            override fun addLabelLayer() {
                this@Controller.cLabelPane.placeLabelLayer()
            }

            override fun removeLabelLayer(groupId: Int) {
                this@Controller.cLabelPane.removeLabelLayer(groupId)
            }

            override fun updateTree() {
                this@Controller.updateTreeView()
            }

            override fun get(fieldName: String): Any {
                try {
                    val clazz = this@Controller.javaClass
                    val field = clazz.getDeclaredField(fieldName)
                    field.isAccessible = true
                    return field[this@Controller]
                } catch (e: Exception) {
                    showException(e)
                }
                throw IllegalArgumentException("field not found")
            }

        }

        fileChooser.getExtensionFilters().add(fileFilter)
        fileChooser.getExtensionFilters().add(meoFilter)
        fileChooser.getExtensionFilters().add(lpFilter)

        bakChooser.setTitle(I18N["chooser.bak"])
        bakChooser.getExtensionFilters().add(bakFilter)

        exportChooser.setTitle(I18N["chooser.export"])

        exportPackChooser.setTitle(I18N["chooser.pack"])
        exportPackChooser.getExtensionFilters().add(packFilter)
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {

        // ----- Component Initialize ----- //

        // Set last used dir
        val lastFilePath = RecentFiles.getLastOpenFile()
        if (lastFilePath != null) CFileChooser.lastDirectory = File(lastFilePath).parentFile

        // Update OpenRecent
        updateOpenRecent()

        // Set menu text
        setText()

        // Set menu disabled
        setDisable()

        // Set accelerators
        if (isMac) {
            mSave.accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN)
            mSaveAs.accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN, KeyCombination.SHIFT_DOWN)
        } else {
            mSave.accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)
            mSaveAs.accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)
        }

        // Warp cPicBox
        cPicBox.isWrapped = true

        // Init vTree context menu
        CTreeMenu.treeMenu.init(vTree)

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
        cPicBox.valueProperty.addListener { _, _, newValue -> State.currentPicName = newValue }
        // cGroupBox - currentGroupId
        cGroupBox.valueProperty.addListener { _, _, newValue ->
            State.transFile.groupList.forEachIndexed { index, transGroup ->
                if (transGroup.name == cGroupBox.valueProperty.value)
                    State.currentGroupId = index
            }
        }

        // tTransText - transLabel.text
        tTransText.textProperty().bind(object : StringBinding() {
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

            tTransText.textProperty().unbind()
            tTransText.textProperty().bindBidirectional(transLabel.textProperty)
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
        State.isChangedProperty.addListener { _, _, _ -> updateTreeView() }

        // GroupBox update
        State.isChangedProperty.addListener { _, _, _ -> updateGroupList() }

        // Update vTree & cLabelPane when change pic
        State.currentPicNameProperty.addListener { _, _, newValue ->
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
                    vTree.selectionModel.select(item)
                    cLabelPane.moveToLabel(item.meta)
                    cLabelPane.selectedLabelIndex = item.index
                    State.currentLabelIndex = item.index
                }
            }
        }

        // Clear text layer when change group
        State.currentGroupIdProperty.addListener { _, _, _ -> cLabelPane.removeText() }

        // Update tree menu when requested in ViewMode.IndexMode
        vTree.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED) {
            if (State.viewMode == ViewMode.IndexMode) CTreeMenu.treeMenu.update()
        }

        // Update selected group when clicked GroupTreeItem
        vTree.selectionModel.selectedItemProperty().addListener { _, _, item ->
            if (item != null) if (item.parent != null && item !is CTreeItem) {
                cGroupBox.moveTo(State.getGroupIdByName(item.value))
            }
        }

        // Bind text and Tree
        vTree.addEventHandler(ScrollToEvent.ANY) {
            val item = vTree.selectionModel.selectedItem
            if (item != null && item is CTreeItem)
                State.currentLabelIndex = item.index
        }
        vTree.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            val item = vTree.selectionModel.selectedItem
            if (item != null && item is CTreeItem)
                State.currentLabelIndex = item.index
        }
        vTree.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (it.code.isArrowKey) {

                // shift
                val shift = if (it.code == KeyCode.UP) -1 else if (it.code == KeyCode.DOWN) 1 else 0
                val item = vTree.getTreeItem(vTree.selectionModel.selectedIndex + shift)

                if (item != null && item is CTreeItem)
                    State.currentLabelIndex = item.index
            }
        }

        // Bind Label and Tree
        cLabelPane.selectedLabelIndexProperty.addListener { _, _, index ->
            if (index == NOT_FOUND) return@addListener

            val item = findLabelItemByIndex(index as Int)
            vTree.selectionModel.clearSelection()
            vTree.selectionModel.select(item)
            vTree.scrollTo(vTree.getRow(item))
        }
        vTree.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            if (it.clickCount < 2) return@addEventHandler

            val item = vTree.selectionModel.selectedItem
            if (item != null && item is CTreeItem) {
                cLabelPane.moveToLabel(State.transFile.transMap[State.currentPicName]!!.find { e -> e.index == item.index }!!)
            }
        }
        vTree.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (it.code.isArrowKey) {

                // shift
                val shift = if (it.code == KeyCode.UP) -1 else if (it.code == KeyCode.DOWN) 1 else 0
                val item = vTree.getTreeItem(vTree.selectionModel.selectedIndex + shift)

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
        vTree.addEventHandler(KeyEvent.KEY_PRESSED) {
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

                        var index = vTree.selectionModel.selectedIndex + shift
                        vTree.selectionModel.clearSelection()
                        // if item == null (to the end), vTree select nothing, return to top

                        var item = vTree.getTreeItem(index)
                        if (item != null) {
                            index = if (item is CTreeItem) {
                                // Label
                                vTree.selectionModel.select(index)
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
                                while (vTree.getTreeItem(index).children.size == 0) {
                                    index++
                                    if (vTree.getTreeItem(index) == null) break
                                }
                                index + shift
                            }

                            vTree.selectionModel.select(index)
                            item = vTree.getTreeItem(index)
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
        tTransText.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyListener)
        cLabelPane.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyListener)

    }

    private fun setText() {
        mmFile.text = I18N["mm.file"]
        mNew.text = I18N["m.new"]
        mOpen.text = I18N["m.open"]
        mOpenRecent.text = I18N["m.recent"]
        mSave.text = I18N["m.save"]
        mSaveAs.text = I18N["m.save_as"]
        mBakRecover.text = I18N["m.bak_recovery"]
        mClose.text = I18N["m.close"]
        mmExport.text = I18N["mm.export"]
        mExportAsLp.text = I18N["m.lp"]
        mExportAsMeo.text = I18N["m.meo"]
        mExportAsMeoPack.text = I18N["m.pack"]
        mEditComment.text = I18N["m.comment"]
        mmAbout.text = I18N["mm.about"]
        mAbout.text = I18N["m.about"]
    }
    private fun setDisable() {
        mSave.disableProperty().bind(State.isOpenedProperty.not())
        mSaveAs.disableProperty().bind(State.isOpenedProperty.not())
        mExportAsLp.disableProperty().bind(State.isOpenedProperty.not())
        mExportAsMeo.disableProperty().bind(State.isOpenedProperty.not())
        mExportAsMeoPack.disableProperty().bind(State.isOpenedProperty.not())
        mEditComment.disableProperty().bind(State.isOpenedProperty.not())
        bSwitchViewMode.disableProperty().bind(State.isOpenedProperty.not())
        bSwitchWorkMode.disableProperty().bind(State.isOpenedProperty.not())
        tTransText.disableProperty().bind(State.isOpenedProperty.not())
        vTree.disableProperty().bind(State.isOpenedProperty.not())
        cPicBox.disableProperty().bind(State.isOpenedProperty.not())
        cGroupBox.disableProperty().bind(State.isOpenedProperty.not())
        cSlider.disableProperty().bind(State.isOpenedProperty.not())
        cLabelPane.disableProperty().bind(State.isOpenedProperty.not())
    }

    private fun reset() {
        vTree.root = null
        bSwitchWorkMode.text = I18N["mode.work.input"]
        bSwitchViewMode.text = I18N["mode.view.index"]

        cPicBox.reset()
        cGroupBox.reset()
        cLabelPane.reset()
        // cSlider will reset with cImagePane through bind
    }
    private fun updateOpenRecent() {
        mOpenRecent.items.clear()
        for (path in RecentFiles.getAll()) {
            val item = MenuItem(path)
            item.setOnAction {
                stay()
                open(path, getFileType(path))
            }
            mOpenRecent.items.add(item)
        }
    }
    private fun updatePicList() {
        cPicBox.setList(TransFile.getSortedPicList(State.transFile))
    }
    private fun updateGroupList() {
        val list = ArrayList<String>()
        for (transGroup in State.transFile.groupList)
            list.add(transGroup.name)
        cGroupBox.setList(list)
        cGroupBox.moveTo(State.currentGroupId)
    }
    private fun updateTreeView() {
        when (State.viewMode) {
            ViewMode.GroupMode -> {
                vTree.selectionModel.selectionMode = SelectionMode.SINGLE
                vTree.cellFactory = Callback { CTreeCell() }
                vTree.contextMenu = null
                updateTreeViewByGroup()
            }
            ViewMode.IndexMode -> {
                vTree.selectionModel.selectionMode = SelectionMode.MULTIPLE
                vTree.cellFactory = null
                vTree.contextMenu = CTreeMenu.treeMenu
                updateTreeViewByIndex()
            }
        }
        vTree.root.expandAll()
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

        vTree.root = rootItem
    }
    private fun updateTreeViewByIndex() {
        val transLabels = State.transFile.getTransLabelListOf(State.currentPicName)
        val rootItem = TreeItem(State.currentPicName)

        for (transLabel in transLabels) {
            val transGroup = State.transFile.groupList[transLabel.groupId]
            val circle = Circle(GRAPHICS_CIRCLE_RADIUS, Color.web(transGroup.color))
            rootItem.children.add(CTreeItem(transLabel, circle))
        }

        vTree.root = rootItem
    }

    private fun findLabelItemByIndex(index: Int): CTreeItem {
        val transLabels = State.transFile.transMap[State.currentPicName]!!
        val transLabel = transLabels.find { it.index == index }!!
        val whereToSearch = when (State.viewMode) {
            ViewMode.GroupMode -> vTree.root.children[transLabel.groupId]
            ViewMode.IndexMode -> vTree.root
        }
        val item = whereToSearch.children.find { (it as CTreeItem).meta == transLabel }!!
        return item as CTreeItem
    }

    private fun silentBackup() {
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

    private fun stay(): Boolean {
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
                saveTranslation()
            }
            return false
        }
        // Closed
        return true
    }

    // TODO: Use File instead of String
    private fun new(path: String, type: FileType) {
        // Choose Pics
        val potentialPics = ArrayList<String>()
        val pics = ArrayList<String>()
        val dir = File(path).parentFile
        if (dir.isDirectory && dir.listFiles() != null) {
            val files = dir.listFiles()
            if (files != null) for (file in files) if (file.isFile) {
                for (extension in EXTENSIONS_PIC) if (file.name.endsWith(extension)) {
                    potentialPics.add(file.name)
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
            val file = File(path)
            when (type) {
                FileType.LPFile -> exportLP(file, State.transFile)
                FileType.MeoFile -> exportMeo(file, State.transFile)
            }
        } catch (e: IOException) {
            showException(e)
            showError(I18N["error.new_failed"])
        }
    }
    private fun open(path: String, type: FileType) {
        val transFile: TransFile
        try {
            val file = File(path)
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
        State.transPath = path

        // Initialize workspace
        reset()
        State.stage.title = INFO["application.name"] + " - " + File(path).name

        fun createColorHexBinding(): ListBinding<String> {
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
        cLabelPane.colorListProperty.bind(createColorHexBinding())

        updatePicList()
        updateGroupList()

        // Update recent files
        RecentFiles.add(path)
        updateOpenRecent()

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
    private fun save(path: String, type: FileType) {
        val file = File(path)

        // Check folder
        if (file.parent != State.getFileFolder()) {
            val result = showAlert(I18N["alert.save_to_another_place.content"])
            if (!(result.isPresent && result.get() == ButtonType.YES)) return
        }

        // Backup if overwrite
        var bak: File? = null
        if (State.transPath == path) {
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

        State.transPath = path
        State.isChanged = false
    }

    // new & open
    @FXML fun newTranslation() {
        if (stay()) return

        State.reset()
        val file = fileChooser.showOpenDialog(State.stage) ?: return
        val type = getFileType(file.path)
        new(file.path, type)
        open(file.path, type)
    }
    // open
    @FXML fun openTranslation() {
        if (stay()) return

        State.reset()
        val file = fileChooser.showOpenDialog(State.stage) ?: return
        open(file.path, getFileType(file.path))
    }
    // save
    @FXML fun saveTranslation() {
        save(State.transPath, getFileType(State.transPath))
    }
    // save
    @FXML fun saveAsTranslation() {
        val file = fileChooser.showSaveDialog(State.stage) ?: return
        save(file.path, getFileType(file.path))
    }
    // open & save
    @FXML fun bakRecovery() {
        if (stay()) return
        val bak = bakChooser.showOpenDialog(State.stage) ?: return
        val rec = fileChooser.showSaveDialog(State.stage) ?: return
        open(bak.path, FileType.MeoFile)
        save(rec.path, getFileType(rec.path))
    }

    @FXML fun close() {
        if (!State.isChanged) exitProcess(0)

        showAlert(I18N["common.exit"], null, I18N["dialog.exit_save_alert.content"]).ifPresent {
            when (it) {
                ButtonType.YES -> {
                    saveTranslation()
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

    @FXML fun exportTransFile(event: ActionEvent) {
        exportChooser.getExtensionFilters().clear()

        try {
            val file: File
            if (event.source == mExportAsMeo) {
                exportChooser.getExtensionFilters().add(meoFilter)
                file = exportChooser.showSaveDialog(State.stage) ?: return
                exportMeo(file, State.transFile)
            } else {
                exportChooser.getExtensionFilters().add(lpFilter)
                file = exportChooser.showSaveDialog(State.stage) ?: return
                exportLP(file, State.transFile)
            }
        } catch (e: IOException) {
            showException(e)
            showError(I18N["error.export_failed"])
            return
        }

        showInfo(I18N["info.exported_successful"])
    }
    @FXML fun exportTransPack() {
        val file = exportPackChooser.showSaveDialog(State.stage) ?: return

        try {
            pack(file, State.getFileFolder(), State.transFile)
        } catch (e : IOException) {
            showException(e)
            showError(I18N["error.export_failed"])
            return
        }

        showInfo(I18N["info.exported_successful"])
    }

    @FXML fun setComment() {
        showInputArea(State.stage, I18N["dialog.edit_comment.title"], State.transFile.comment).ifPresent {
            State.transFile.comment = it
        }
    }

    @FXML fun about() {
        showLink(
            State.stage,
            I18N["dialog.about.title"],
            null,
            StringBuilder()
                .append(INFO["application.name"]).append(" - ").append(INFO["application.version"]).append("\n")
                .append("Developed By ").append(INFO["application.vendor"]).append("\n")
                .toString(),
            INFO["application.link"]
        ) {
            State.application.hostServices.showDocument(INFO["application.url"])
        }
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