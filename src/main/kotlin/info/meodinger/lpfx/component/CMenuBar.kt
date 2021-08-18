package info.meodinger.lpfx.component

import info.meodinger.lpfx.*
import info.meodinger.lpfx.io.*
import info.meodinger.lpfx.options.Options
import info.meodinger.lpfx.options.RecentFiles
import info.meodinger.lpfx.options.Settings
import info.meodinger.lpfx.type.TransFile
import info.meodinger.lpfx.type.TransGroup
import info.meodinger.lpfx.type.TransLabel
import info.meodinger.lpfx.util.dialog.*
import info.meodinger.lpfx.util.disableMnemonicParsingForAll
import info.meodinger.lpfx.util.platform.isMac
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.INFO
import info.meodinger.lpfx.util.resource.get
import info.meodinger.lpfx.util.using

import javafx.event.ActionEvent
import javafx.scene.control.ButtonType
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.stage.FileChooser
import java.io.*
import java.util.*
import kotlin.system.exitProcess

/**
 * Author: Meodinger
 * Date: 2021/8/17
 * Location: info.meodinger.lpfx.component
 */
class CMenuBar : MenuBar() {

    private val mmFile = Menu(I18N["mm.file"])
    private val mNew = MenuItem(I18N["m.new"])
    private val mOpen = MenuItem(I18N["m.open"])
    private val mOpenRecent = Menu(I18N["m.recent"])
    private val mSave = MenuItem(I18N["m.save"])
    private val mSaveAs = MenuItem(I18N["m.save_as"])
    private val mBakRecover = MenuItem(I18N["m.bak_recovery"])
    private val mClose = MenuItem(I18N["m.close"])
    private val mmExport = Menu(I18N["mm.export"])
    private val mExportAsLp = MenuItem(I18N["m.lp"])
    private val mExportAsMeo = MenuItem(I18N["m.meo"])
    private val mExportAsTransPack = MenuItem(I18N["m.pack"])
    private val mEditComment = MenuItem(I18N["m.comment"])
    private val mmAbout = Menu(I18N["mm.about"])
    private val mAbout = MenuItem(I18N["m.about"])

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
                        this@CMenuBar.silentBackup()
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
        fileChooser.getExtensionFilters().add(fileFilter)
        fileChooser.getExtensionFilters().add(meoFilter)
        fileChooser.getExtensionFilters().add(lpFilter)
        bakChooser.setTitle(I18N["chooser.bak"])
        bakChooser.getExtensionFilters().add(bakFilter)
        exportChooser.setTitle(I18N["chooser.export"])
        exportPackChooser.setTitle(I18N["chooser.pack"])
        exportPackChooser.getExtensionFilters().add(packFilter)

        this.disableMnemonicParsingForAll()

        mNew.setOnAction { newTranslation() }
        mOpen.setOnAction { openTranslation() }
        mSave.setOnAction { saveTranslation() }
        mSaveAs.setOnAction { saveAsTranslation() }
        mBakRecover.setOnAction { bakRecovery() }
        mClose.setOnAction { close() }
        mExportAsLp.setOnAction { exportTransFile(it) }
        mExportAsMeo.setOnAction { exportTransFile(it) }
        mExportAsTransPack.setOnAction { exportTransPack() }
        mEditComment.setOnAction { editComment() }
        mAbout.setOnAction { about() }

        mSave.disableProperty().bind(State.isOpenedProperty.not())
        mSaveAs.disableProperty().bind(State.isOpenedProperty.not())
        mExportAsLp.disableProperty().bind(State.isOpenedProperty.not())
        mExportAsMeo.disableProperty().bind(State.isOpenedProperty.not())
        mExportAsTransPack.disableProperty().bind(State.isOpenedProperty.not())
        mEditComment.disableProperty().bind(State.isOpenedProperty.not())

        // Set accelerators
        if (isMac) {
            mSave.accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN)
            mSaveAs.accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN, KeyCombination.SHIFT_DOWN)
        } else {
            mSave.accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)
            mSaveAs.accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)
        }

        this.menus.addAll(mmFile, mmExport, mmAbout)
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
    private fun new(file: File, type: FileType) {
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
    private fun open(file: File, type: FileType) {
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
    private fun save(file: File, type: FileType) {

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

    fun updateOpenRecent() {
        mOpenRecent.items.clear()
        for (path in RecentFiles.getAll()) {
            val item = MenuItem(path)
            item.setOnAction {
                stay()
                open(File(path), getFileType(path))
            }
            mOpenRecent.items.add(item)
        }
    }

    fun newTranslation() {
        // new & open

        if (stay()) return

        State.reset()
        val file = fileChooser.showSaveDialog(State.stage) ?: return
        val type = getFileType(file.path)
        new(file, type)
        open(file, type)
    }
    fun openTranslation() {
        // open

        if (stay()) return

        State.reset()
        val file = fileChooser.showOpenDialog(State.stage) ?: return
        open(file, getFileType(file.path))
    }
    fun saveTranslation() {
        // save

        save(File(State.transPath), getFileType(State.transPath))
    }
    fun saveAsTranslation() {
        // save

        val file = fileChooser.showSaveDialog(State.stage) ?: return
        save(file, getFileType(file.path))
    }
    fun bakRecovery() {
        // open & save

        if (stay()) return
        val bak = bakChooser.showOpenDialog(State.stage) ?: return
        val rec = fileChooser.showSaveDialog(State.stage) ?: return
        open(bak, FileType.MeoFile)
        save(rec, getFileType(rec.path))
    }

    fun close() {
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

    fun exportTransFile(event: ActionEvent) {
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
    fun exportTransPack() {
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

    fun editComment() {
        showInputArea(State.stage, I18N["dialog.edit_comment.title"], State.transFile.comment).ifPresent {
            State.transFile.comment = it
        }
    }

    fun about() {
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
}