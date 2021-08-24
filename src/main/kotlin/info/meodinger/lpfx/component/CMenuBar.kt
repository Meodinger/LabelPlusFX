package info.meodinger.lpfx.component

import info.meodinger.lpfx.*
import info.meodinger.lpfx.io.*
import info.meodinger.lpfx.options.RecentFiles
import info.meodinger.lpfx.util.dialog.*
import info.meodinger.lpfx.util.disableMnemonicParsingForAll
import info.meodinger.lpfx.util.file.transfer
import info.meodinger.lpfx.util.platform.isMac
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.INFO
import info.meodinger.lpfx.util.resource.get

import javafx.event.ActionEvent
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.stage.FileChooser
import java.io.*

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

        mmFile.items.addAll(mNew, mOpen, mSave, mSaveAs, SeparatorMenuItem(), mBakRecover, SeparatorMenuItem(), mClose)
        mmExport.items.addAll(mExportAsLp, mExportAsMeo, mExportAsTransPack, SeparatorMenuItem(), mEditComment)
        mmAbout.items.addAll(mAbout)
        this.menus.addAll(mmFile, mmExport, mmAbout)
    }

    fun updateOpenRecent() {
        mOpenRecent.items.clear()
        for (path in RecentFiles.getAll()) {
            val item = MenuItem(path)
            item.setOnAction {
                State.controller.stay()
                State.controller.open(File(path), getFileType(path))
            }
            mOpenRecent.items.add(item)
        }
    }

    private fun newTranslation() {
        // new & open

        if (State.controller.stay()) return

        State.reset()

        fileChooser.title = I18N["chooser.new"]
        val file = fileChooser.showSaveDialog(State.stage) ?: return
        val type = getFileType(file.path)

        State.controller.new(file, type)
        State.controller.open(file, type)
    }
    private fun openTranslation() {
        // open

        if (State.controller.stay()) return

        State.reset()

        fileChooser.title = I18N["chooser.open"]
        val file = fileChooser.showOpenDialog(State.stage) ?: return

        State.controller.open(file, getFileType(file.path))
    }
    private fun saveTranslation() {
        // save

        State.controller.save(File(State.transPath), getFileType(State.transPath))
    }
    private fun saveAsTranslation() {
        // save

        fileChooser.title = I18N["chooser.save"]
        val file = fileChooser.showSaveDialog(State.stage) ?: return

        State.controller.save(file, getFileType(file.path))
    }
    private fun bakRecovery() {
        // transfer & open

        if (State.controller.stay()) return

        State.reset()

        fileChooser.title = I18N["chooser.rec"]
        val bak = bakChooser.showOpenDialog(State.stage) ?: return
        val rec = fileChooser.showSaveDialog(State.stage) ?: return

        try {
            transfer(bak, rec)
        } catch (e: Exception) {
            e.printStackTrace()
            showError(I18N["error.recovery_failed"])
            showException(e)
            return
        }

        State.controller.open(rec, getFileType(rec.path))
    }

    private fun exportTransFile(event: ActionEvent) {
        exportChooser.extensionFilter.clear()

        try {
            val file: File
            if (event.source == mExportAsMeo) {
                exportChooser.extensionFilter.add(meoFilter)
                file = exportChooser.showSaveDialog(State.stage) ?: return
                exportMeo(file, State.transFile)
            } else {
                exportChooser.extensionFilter.add(lpFilter)
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
    private fun exportTransPack() {
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

    private fun editComment() {
        showInputArea(State.stage, I18N["dialog.edit_comment.title"], State.transFile.comment).ifPresent {
            State.transFile.comment = it
            State.isChanged = true
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