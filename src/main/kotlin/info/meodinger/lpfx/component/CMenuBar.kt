package info.meodinger.lpfx.component

import info.meodinger.lpfx.*
import info.meodinger.lpfx.component.singleton.CLogsDialog
import info.meodinger.lpfx.component.singleton.CSettingsDialog
import info.meodinger.lpfx.options.Logger
import info.meodinger.lpfx.options.RecentFiles
import info.meodinger.lpfx.options.Settings
import info.meodinger.lpfx.util.dialog.showChoiceList
import info.meodinger.lpfx.util.dialog.showInfo
import info.meodinger.lpfx.util.dialog.showInputArea
import info.meodinger.lpfx.util.dialog.showLink
import info.meodinger.lpfx.util.disableMnemonicParsingForAll
import info.meodinger.lpfx.util.platform.isMac
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.INFO
import info.meodinger.lpfx.util.resource.get

import javafx.event.ActionEvent
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.stage.FileChooser
import java.io.*
import java.nio.file.Files
import java.util.stream.Collectors
import kotlin.io.path.name


/**
 * Author: Meodinger
 * Date: 2021/8/17
 * Location: info.meodinger.lpfx.component
 */

/**
 * A MenuBar for main scene, did not make it singleton for fxml loader
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
    private val mEditPictures = MenuItem(I18N["m.pictures"])
    private val mmAbout = Menu(I18N["mm.about"])
    private val mSettings = MenuItem(I18N["m.settings"])
    private val mLogs = MenuItem(I18N["m.logs"])
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

    init {
        fileChooser.extensionFilter.add(fileFilter)
        fileChooser.extensionFilter.add(meoFilter)
        fileChooser.extensionFilter.add(lpFilter)
        bakChooser.title = I18N["chooser.bak"]
        bakChooser.extensionFilter.add(bakFilter)
        exportChooser.title = I18N["chooser.export"]
        exportPackChooser.title = I18N["chooser.pack"]
        exportPackChooser.extensionFilter.add(packFilter)

        this.disableMnemonicParsingForAll()

        mNew.setOnAction { newTranslation() }
        mOpen.setOnAction { openTranslation() }
        mSave.setOnAction { saveTranslation() }
        mSaveAs.setOnAction { saveAsTranslation() }
        mBakRecover.setOnAction { bakRecovery() }
        mClose.setOnAction { State.controller.close() }
        mExportAsLp.setOnAction { exportTransFile(it) }
        mExportAsMeo.setOnAction { exportTransFile(it) }
        mExportAsTransPack.setOnAction { exportTransPack() }
        mEditComment.setOnAction { editComment() }
        mEditPictures.setOnAction { editPictures() }
        mSettings.setOnAction { settings() }
        mLogs.setOnAction { logs() }
        mAbout.setOnAction { about() }

        mSave.disableProperty().bind(State.isOpenedProperty.not())
        mSaveAs.disableProperty().bind(State.isOpenedProperty.not())
        mExportAsLp.disableProperty().bind(State.isOpenedProperty.not())
        mExportAsMeo.disableProperty().bind(State.isOpenedProperty.not())
        mExportAsTransPack.disableProperty().bind(State.isOpenedProperty.not())
        mEditComment.disableProperty().bind(State.isOpenedProperty.not())
        mEditPictures.disableProperty().bind(State.isOpenedProperty.not())

        // Set accelerators
        if (isMac) {
            mSave.accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN)
            mSaveAs.accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN, KeyCombination.SHIFT_DOWN)
        } else {
            mSave.accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)
            mSaveAs.accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)
        }

        mmFile.items.addAll(mNew, mOpen, mOpenRecent, mSave, mSaveAs, SeparatorMenuItem(), mBakRecover, SeparatorMenuItem(), mClose)
        mmExport.items.addAll(mExportAsLp, mExportAsMeo, mExportAsTransPack, SeparatorMenuItem(), mEditComment, mEditPictures)
        mmAbout.items.addAll(mSettings, mLogs, SeparatorMenuItem(), mAbout)
        this.menus.addAll(mmFile, mmExport, mmAbout)
    }

    fun updateOpenRecent() {
        mOpenRecent.items.clear()
        for (path in RecentFiles.getAll()) {
            mOpenRecent.items.add(MenuItem(path).also { it.setOnAction { openRecentTranslation(path) } })
        }
    }
    private fun openRecentTranslation(path: String) {
        if (State.controller.stay()) return

        State.reset()

        State.controller.open(File(path), FileType.getType(path))
    }

    private fun newTranslation() {
        // new & open

        if (State.controller.stay()) return

        State.reset()

        fileChooser.title = I18N["chooser.new"]
        val file = fileChooser.showSaveDialog(State.stage) ?: return
        val type = FileType.getType(file.path)

        State.controller.new(file, type)
        State.controller.open(file, type)
    }
    private fun openTranslation() {
        // open

        if (State.controller.stay()) return

        State.reset()

        fileChooser.title = I18N["chooser.open"]
        val file = fileChooser.showOpenDialog(State.stage) ?: return

        State.controller.open(file, FileType.getType(file.path))
    }
    private fun saveTranslation() {
        // save

        State.controller.save(File(State.transPath), FileType.getType(State.transPath), true)
    }
    private fun saveAsTranslation() {
        // save

        fileChooser.title = I18N["chooser.save"]
        val file = fileChooser.showSaveDialog(State.stage) ?: return

        State.controller.save(file, FileType.getType(file.path), false)
    }
    private fun bakRecovery() {
        // transfer & open

        if (State.controller.stay()) return

        State.reset()

        fileChooser.title = I18N["chooser.rec"]
        val bak = bakChooser.showOpenDialog(State.stage) ?: return
        val rec = fileChooser.showSaveDialog(State.stage) ?: return

        State.controller.recovery(bak, rec)
    }

    private fun exportTransFile(event: ActionEvent) {
        exportChooser.extensionFilter.clear()

        val file: File
        if (event.source == mExportAsMeo) {
            exportChooser.extensionFilter.add(meoFilter)
            file = exportChooser.showSaveDialog(State.stage) ?: return
            State.controller.export(file, FileType.MeoFile)
        } else {
            exportChooser.extensionFilter.add(lpFilter)
            file = exportChooser.showSaveDialog(State.stage) ?: return
            State.controller.export(file, FileType.LPFile)
        }
    }
    private fun exportTransPack() {
        val file = exportPackChooser.showSaveDialog(State.stage) ?: return

        State.controller.pack(file)
    }

    private fun editComment() {
        showInputArea(State.stage, I18N["dialog.edit_comment.title"], State.transFile.comment).ifPresent {
            State.setComment(it)
            State.isChanged = true
        }
    }
    private fun editPictures() {
        // Choose Pics
        val selected = State.transFile.transMap.keys.toList()
        val unselected = Files.walk(File(State.getFileFolder()).toPath()).filter {
            if (selected.contains(it.name)) return@filter false
            for (extension in EXTENSIONS_PIC) if (it.name.endsWith(extension)) return@filter true
            false
        }.map { it.name }.collect(Collectors.toList())

        showChoiceList(State.stage, unselected, selected).ifPresent {
            if (it.isEmpty()) {
                showInfo(I18N["alert.required_at_least_1_ic"])
                return@ifPresent
            }

            // Edit date
            val toRemove = ArrayList<String>()
            for (picName in State.transFile.transMap.keys) if (!it.contains(picName)) toRemove.add(picName)
            for (picName in toRemove) State.removePicture(picName)
            val toAdd = ArrayList<String>()
            for (picName in it) if (!State.transFile.transMap.keys.contains(picName)) toAdd.add(picName)
            for (picName in toAdd) State.addPicture(picName)
            // Update view
            State.controller.updatePicList()
            // Mark change
            State.isChanged = true
        }
    }

    private fun settings() {
        val result = CSettingsDialog.showAndWait()
        if (!result.isPresent) return
        val list = result.get()

        var updatePane = false
        for (property in list) {
            if (Settings[property.key].asString() == property.value) continue

            when (property.key) {
                Settings.LabelAlpha -> updatePane = true
                Settings.LabelRadius -> updatePane = true
            }

            Settings[property.key] = property
        }

        if (updatePane && State.isOpened) State.controller.updateLabelPane()
    }
    private fun logs() {
        val result = CLogsDialog.showAndWait()
        if (!result.isPresent) return
        val list = result.get()

        for (property in list) {
            if (Settings[property.key].asString() == property.value) continue

            when (property.key) {
                Settings.LogLevelPreference -> Logger.level = Logger.LogType.getType(property.value)
            }

            Settings[property.key] = property
        }
    }
    private fun about() {
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