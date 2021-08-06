package info.meodinger.lpfx

import info.meodinger.lpfx.component.*
import info.meodinger.lpfx.component.CLabelPane.LabelEvent
import info.meodinger.lpfx.io.exportLP
import info.meodinger.lpfx.io.exportMeo
import info.meodinger.lpfx.io.pack
import info.meodinger.lpfx.options.Config
import info.meodinger.lpfx.options.Options
import info.meodinger.lpfx.options.RecentFiles
import info.meodinger.lpfx.type.TransFile
import info.meodinger.lpfx.type.TransLabel
import info.meodinger.lpfx.util.dialog.*
import info.meodinger.lpfx.util.platform.isMac
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.INFO
import info.meodinger.lpfx.util.resource.get
import javafx.application.Platform

import javafx.beans.value.ChangeListener
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.stage.FileChooser
import java.io.File
import java.net.URL
import java.util.*
import java.util.function.BiFunction
import kotlin.system.exitProcess

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx
 */
class Controller : Initializable {

    @FXML private lateinit var tTransText: TextArea
    @FXML private lateinit var bSwitchViewMode: Button
    @FXML private lateinit var bSwitchWorkMode: Button
    @FXML private lateinit var pMain: SplitPane
    @FXML private lateinit var pRight: SplitPane
    @FXML private lateinit var pText: AnchorPane
    @FXML private lateinit var vTree: TreeView<String>
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

    private val symbolMenu = object : ContextMenu() {

        private val radius = 6.0
        private val symbols = listOf(
            Pair("※", true),
            Pair("◎", true),
            Pair("★", true),
            Pair("☆", true),
            Pair("～", true),
            Pair("♡", false),
            Pair("♥", false),
            Pair("♢", false),
            Pair("♦", false),
            Pair("♪", false)
        )

        fun createSymbolItem(symbol: String, displayable: Boolean): MenuItem {
            return MenuItem(
                symbol,
                if (displayable) Circle(radius, Color.GREEN)
                else Circle(radius, Color.RED)
            ).also {
                it.style = "-fx-font-family: \"Segoe UI Symbol\""
            }
        }

        init {
            for (symbol in symbols) items.add(createSymbolItem(symbol.first, symbol.second).also {
                it.setOnAction { tTransText.insertText(tTransText.caretPosition, symbol.first) }
            })
        }
    }

    private val timer = Timer()
    private val task = object : TimerTask() {
        override fun run() {
            if (State.isChanged) {
                this@Controller.silentBackup()
            }
        }
    }

    init {
        State.controllerAccessor = object : State.ControllerAccessor {
            override fun close() {
                this@Controller.close()
            }

            override fun reset() {
                this@Controller.reset()
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

            override fun updateGroupList() {
                this@Controller.updateGroupList()
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

        CFileChooser.lastDirectory = File(RecentFiles.getLastOpenFile() ?: Options.lpfx.toString()).parentFile

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
        setText()
        setDisable()

        // Init image
        cLabelPane.isVisible = false
        Platform.runLater {
            cLabelPane.moveToCenter()
            cLabelPane.isVisible = true
        }

        // Initialize
        CTreeMenu.treeMenu.init(vTree)
        pMain.setDividerPositions(Config[Config.MAIN_DIVIDER].asDouble())
        pRight.setDividerPositions(Config[Config.RIGHT_DIVIDER].asDouble())
        cPicBox.isWrapped = true
        updateRecentFiles()

        // Register handler
        cLabelPane.handleInputMode = EventHandler {
            if (State.workMode != WorkMode.InputMode) return@EventHandler
            when (it.eventType) {
                LabelEvent.LABEL_POINTED -> {
                    val transLabel = State.transFile.getTransLabelAt(State.currentPicName, it.labelIndex)

                    cLabelPane.removeText()
                    cLabelPane.placeText(transLabel.text, Color.BLACK, it.rootX, it.rootY * cLabelPane.imageHeight)
                }
                LabelEvent.LABEL_CLICKED -> {
                    val transLabel = State.transFile.getTransLabelAt(State.currentPicName, it.labelIndex)

                    tTransText.textProperty().unbind()
                    tTransText.textProperty().bindBidirectional(transLabel.textProperty)
                }
            }
        }
        cLabelPane.handleLabelMode = EventHandler {
            if (State.workMode != WorkMode.LabelMode) return@EventHandler
            when (it.eventType) {
                LabelEvent.LABEL_OTHER -> {
                    val transGroup = State.transFile.getTransGroupAt(State.currentGroupId)
                    cLabelPane.removeText()
                    cLabelPane.placeText(transGroup.name, Color.web(transGroup.color), it.rootX, it.rootY)
                }
                LabelEvent.LABEL_PLACE -> {
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
                LabelEvent.LABEL_REMOVE -> {
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
            }
        }

        // Accelerator
        if (isMac) {
            mSave.accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN)
            mSaveAs.accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN, KeyCombination.SHIFT_DOWN)
        } else {
            mSave.accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)
            mSaveAs.accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)
        }

        // Fix split ratio when resize
        val geometryListener = ChangeListener<Number> { _, _, _ ->
            val debounce = { input: Double -> (input * 100 + 0.2) / 100 }
            pMain.setDividerPositions(debounce(pMain.dividerPositions[0]))
            pRight.setDividerPositions(debounce(pRight.dividerPositions[0]))
        }
        State.stage.widthProperty().addListener(geometryListener)
        State.stage.heightProperty().addListener(geometryListener)

        // Update config
        pMain.dividers[0].positionProperty().addListener { _, _, newValue ->
            Config[Config.MAIN_DIVIDER].value = newValue.toString()
        }
        pRight.dividers[0].positionProperty().addListener { _, _, newValue ->
            Config[Config.RIGHT_DIVIDER].value = newValue.toString()
        }

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
        mSave.isDisable = !State.isOpened
        mSaveAs.isDisable = !State.isOpened
        mExportAsLp.isDisable = !State.isOpened
        mExportAsMeo.isDisable = !State.isOpened
        mExportAsMeoPack.isDisable = !State.isOpened
        mEditComment.isDisable = !State.isOpened
        bSwitchViewMode.isDisable = !State.isOpened
        bSwitchWorkMode.isDisable = !State.isOpened
        tTransText.isDisable = !State.isOpened
        vTree.isDisable = !State.isOpened
        cPicBox.isDisable = !State.isOpened
        cGroupBox.isDisable = !State.isOpened
        cSlider.isDisable = !State.isOpened
        cLabelPane.isDisable = !State.isOpened
    }

    private fun reset() {}
    private fun updateRecentFiles() {}
    private fun updateGroupList() {}
    private fun updateTreeView() {}
    private fun updateTreeViewByGroup() {}
    private fun updateTreeViewByIndex() {}

    private fun findLabelItemByIndex(index: Int): CTreeItem {
        val transLabel = State.transFile.getTransLabelAt(State.currentPicName, index)
        val whereToSearch: List<TreeItem<String>> = when (State.viewMode) {
            ViewMode.GroupMode -> vTree.root.children[transLabel.groupId].children
            ViewMode.IndexMode -> vTree.root.children
        }
        for (l in whereToSearch) if ((l as CTreeItem).meta.index == index) return l
        throw IllegalStateException("Not found")
    }

    private fun silentBackup() {}

    private fun prepare() {}
    private fun stay() {}

    private fun new(path: String) {}
    private fun open(path: String) {}
    private fun save(path: String, isSilent: Boolean) {}

    // new & open
    @FXML fun newTranslation() {
        stay()
        val file = fileChooser.showOpenDialog(State.stage) ?: return
        new(file.path)
        prepare()
        open(file.path)
    }
    // open
    @FXML fun openTranslation() {
        stay()
        val file = fileChooser.showOpenDialog(State.stage) ?: return
        prepare()
        open(file.path)
    }
    // save
    @FXML fun saveTranslation() {
        save(State.transPath, false)
    }
    // save
    @FXML fun saveAsTranslation() {
        val file = fileChooser.showSaveDialog(State.stage) ?: return
        save(file.path, false)
    }
    // open & save
    @FXML fun bakRecovery() {
        stay()
        val bak = bakChooser.showOpenDialog(State.stage) ?: return
        val rec = fileChooser.showSaveDialog(State.stage) ?: return
        prepare()
        open(bak.path)
        save(rec.path, false)
    }

    @FXML fun close() {
        if (!State.isChanged) exitProcess(0)

        showAlert(I18N["common.exit"], I18N["dialog.exit_save_alert.content"], I18N["common.save"], I18N["common.not_save"]).ifPresent {
            when (it.buttonData) {
                ButtonBar.ButtonData.YES -> {
                    saveTranslation()
                    exitProcess(0)
                }
                ButtonBar.ButtonData.NO -> {
                    exitProcess(0)
                }
                else -> return@ifPresent
            }
        }
    }

    @FXML fun exportTransFile(event: ActionEvent) {
        exportChooser.getExtensionFilters().clear()

        val exporter = when (event.source) {
            mExportAsLp -> {
                exportChooser.getExtensionFilters().add(lpFilter)
                BiFunction<File, TransFile, Boolean> { file, transFile -> exportLP(file, transFile) }
            }
            mExportAsMeo -> {
                exportChooser.getExtensionFilters().addAll(meoFilter)
                BiFunction<File, TransFile, Boolean> { file, transFile -> exportMeo(file, transFile) }
            }
            else -> return
        }

        val file = exportChooser.showSaveDialog(State.stage) ?: return

        if (exporter.apply(file, State.transFile)) {
            showInfo(I18N["info.exported_successful"])
        } else {
            showAlert(I18N["alert.export_failed"])
        }
    }
    @FXML fun exportTransPack() {
        val file = exportPackChooser.showSaveDialog(State.stage) ?: return

        if (pack(file, State.getFileFolder(), State.transFile)) {
            showInfo(I18N["info.exported_successful"])
        } else {
            showAlert(I18N["alert.export_failed"])
        }
    }

    @FXML fun setComment() {
        showInputArea(State.stage, I18N["dialog.edit_comment.title"], State.transFile.comment).ifPresent {
            State.transFile.comment = it
        }
    }

    @FXML fun about() {
        showInfoWithLink(
            I18N["dialog.about.title"],
            StringBuilder()
                .appendLine(INFO["application.name"])
                .appendLine(INFO["application.version"])
                .appendLine(INFO["application.vendor"])
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