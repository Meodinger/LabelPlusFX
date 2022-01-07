package ink.meodinger.lpfx.component.singleton

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.component.common.CFileChooser
import ink.meodinger.lpfx.io.UpdateChecker
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.RecentFiles
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.dialog.*
import ink.meodinger.lpfx.util.platform.isMac
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.INFO
import ink.meodinger.lpfx.util.resource.get

import javafx.beans.binding.Bindings
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.stage.FileChooser
import java.io.File
import java.nio.file.Files
import java.util.stream.Collectors
import kotlin.io.path.extension
import kotlin.io.path.name


/**
 * Author: Meodinger
 * Date: 2021/8/17
 * Have fun with my code!
 */

/**
 * A MenuBar for main scene, did not make it singleton for fxml loader
 */
object AMenuBar : MenuBar() {

    // ----- Recent ----- //

    private val mOpenRecent = Menu(I18N["m.recent"])

    // ----- Choosers ----- //

    private val picChooser        = CFileChooser()
    private val picFilter         = FileChooser.ExtensionFilter(I18N["filetype.pictures"], List(EXTENSIONS_PIC.size) { index -> "*.${EXTENSIONS_PIC[index]}" })
    private val pngFilter         = FileChooser.ExtensionFilter(I18N["filetype.picture_png"], "*.${EXTENSION_PIC_PNG}")
    private val jpgFilter         = FileChooser.ExtensionFilter(I18N["filetype.picture_jpg"], "*.${EXTENSION_PIC_JPG}")
    private val jpegFilter        = FileChooser.ExtensionFilter(I18N["filetype.picture_jpeg"], "*.${EXTENSION_PIC_JPEG}")

    private val fileChooser       = CFileChooser()
    private val exportChooser     = CFileChooser()
    private val fileFilter        = FileChooser.ExtensionFilter(I18N["filetype.translation"],  List(EXTENSIONS_FILE.size) { index -> "*.${EXTENSIONS_FILE[index]}" })
    private val lpFilter          = FileChooser.ExtensionFilter(I18N["filetype.translation_lp"], "*.${EXTENSION_FILE_LP}")
    private val meoFilter         = FileChooser.ExtensionFilter(I18N["filetype.translation_meo"], "*.${EXTENSION_FILE_MEO}")

    private val backupChooser     = CFileChooser()
    private val bakFilter         = FileChooser.ExtensionFilter(I18N["filetype.backup"], "*.${EXTENSION_BAK}")

    private val exportPackChooser = CFileChooser()
    private val packFilter        = FileChooser.ExtensionFilter(I18N["filetype.pack"], "*.${EXTENSION_PACK}")

    init {
        picChooser.title = I18N["m.externalPic.chooser.title"]
        picChooser.extensionFilters.addAll(picFilter, pngFilter, jpgFilter, jpegFilter)

        // fileChooser's tile will change
        fileChooser.extensionFilters.addAll(fileFilter, meoFilter, lpFilter)

        backupChooser.title = I18N["chooser.bak"]
        backupChooser.extensionFilters.add(bakFilter)

        exportChooser.title = I18N["chooser.export"]
        // exportChooser's filter will change

        exportPackChooser.title = I18N["chooser.pack"]
        exportPackChooser.extensionFilters.add(packFilter)
        exportPackChooser.initialFilename = "$PACKAGE_FILE_NAME.$EXTENSION_PACK"

        disableMnemonicParsingForAll()
        menu(I18N["mm.file"]) {
            item(I18N["m.new"]) {
                does { newTranslation() }
                accelerator = KeyCodeCombination(
                    KeyCode.N,
                    if (isMac) KeyCombination.META_DOWN else KeyCombination.CONTROL_DOWN
                )
            }
            item(I18N["m.open"]) {
                does { openTranslation() }
                accelerator = KeyCodeCombination(
                    KeyCode.O,
                    if (isMac) KeyCombination.META_DOWN else KeyCombination.CONTROL_DOWN
                )
            }
            menu(mOpenRecent) {
                disableProperty().bind(Bindings.createBooleanBinding({ items.isEmpty() }, items))
            }
            item(I18N["m.close"]) {
                does { closeTranslation() }
                disableProperty().bind(!State.isOpenedProperty)
            }
            separator()
            item(I18N["m.save"]) {
                does { saveTranslation() }
                disableProperty().bind(!State.isOpenedProperty)
                accelerator = KeyCodeCombination(
                    KeyCode.S,
                    if (isMac) KeyCombination.META_DOWN else KeyCombination.CONTROL_DOWN
                )
            }
            item(I18N["m.save_as"]) {
                does { saveAsTranslation() }
                disableProperty().bind(!State.isOpenedProperty)
                accelerator = KeyCodeCombination(
                    KeyCode.S,
                    KeyCombination.SHIFT_DOWN,
                    if (isMac) KeyCombination.META_DOWN else KeyCombination.CONTROL_DOWN
                )
            }
            separator()
            item(I18N["m.bak_recovery"]) {
                does { bakRecovery() }
            }
            separator()
            item(I18N["m.exit"]) {
                does { State.controller.exit() }
            }
        }
        menu(I18N["mm.edit"]) {
            item(I18N["m.comment"]) {
                does { editComment() }
                disableProperty().bind(!State.isOpenedProperty)
            }
            separator()
            item(I18N["m.projectPics"]) {
                does { editProjectPictures() }
                disableProperty().bind(!State.isOpenedProperty)
            }
            item(I18N["m.externalPic"]) {
                does { addExternalPicture() }
                disableProperty().bind(!State.isOpenedProperty)
            }
            item(I18N["m.specify"]) {
                does { State.controller.specifyPicFiles() }
                disableProperty().bind(!State.isOpenedProperty)
            }
        }
        menu(I18N["mm.export"]) {
            item(I18N["m.lp"]) {
                does { exportTransFile(FileType.LPFile) }
                disableProperty().bind(!State.isOpenedProperty)
                accelerator = KeyCodeCombination(
                    KeyCode.E,
                    if (isMac) KeyCombination.META_DOWN else KeyCombination.CONTROL_DOWN
                )
            }
            item(I18N["m.meo"]) {
                does { exportTransFile(FileType.MeoFile) }
                disableProperty().bind(!State.isOpenedProperty)
                accelerator = KeyCodeCombination(
                    KeyCode.E,
                    KeyCombination.SHIFT_DOWN,
                    if (isMac) KeyCombination.META_DOWN else KeyCombination.CONTROL_DOWN
                )
            }
            separator()
            item(I18N["m.pack"]) {
                does { exportTransPack() }
                disableProperty().bind(!State.isOpenedProperty)
                accelerator = KeyCodeCombination(
                    KeyCode.S,
                    KeyCombination.ALT_DOWN,
                    if (isMac) KeyCombination.META_DOWN else KeyCombination.CONTROL_DOWN
                )
            }
        }
        menu(I18N["mm.about"]) {
            item(I18N["m.settings"]) {
                does { settings() }
            }
            item(I18N["m.logs"]) {
                does { logs() }
            }
            separator()
            item(I18N["m.about"]) {
                does { about() }
            }
            separator()
            item(I18N["m.crash"]) {
                does { crash() }
            }
        }
    }

    fun updateRecentFiles() {
        mOpenRecent.items.clear()

        for (path in RecentFiles.getAll())
            if (File(path).exists())
                mOpenRecent.items.add(MenuItem(path) does { openRecentTranslation(this) })
    }
    private fun openRecentTranslation(item: MenuItem) {
        // Open recent, remove item if not exist

        if (State.controller.stay()) return

        val path = item.text
        val file = File(path)
        if (!file.exists()) {
            showError(String.format(I18N["error.file_not_exist.s"], path), State.stage)
            RecentFiles.remove(path)
            mOpenRecent.items.remove(item)
            return
        }

        State.reset()

        State.controller.open(file, FileType.getType(file))
    }

    private fun newTranslation() {
        // new & open

        if (State.controller.stay()) return

        State.reset()

        fileChooser.title = I18N["chooser.new"]
        fileChooser.selectedExtensionFilter = fileFilter
        fileChooser.initialFilename = "$INITIAL_FILE_NAME.$EXTENSION_FILE_MEO"
        val file = fileChooser.showSaveDialog(State.stage) ?: return

        val projectFolder = State.controller.new(file, FileType.getType(file))
        if (projectFolder != null) State.controller.open(file, FileType.getType(file), projectFolder)
    }
    private fun openTranslation() {
        // open

        if (State.controller.stay()) return

        State.reset()

        fileChooser.title = I18N["chooser.open"]
        fileChooser.selectedExtensionFilter = fileFilter
        fileChooser.initialFilename = ""
        val file = fileChooser.showOpenDialog(State.stage) ?: return

        State.controller.open(file, FileType.getType(file))
    }
    private fun saveTranslation() {
        // save

        State.controller.save(State.translationFile, FileType.getType(State.translationFile), true)
        State.controller.labelInfo(I18N["info.saved_successfully"])
    }
    private fun saveAsTranslation() {
        // save

        fileChooser.title = I18N["chooser.save"]
        fileChooser.selectedExtensionFilter = fileFilter
        fileChooser.initialFilename = State.translationFile.name
        val file = fileChooser.showSaveDialog(State.stage) ?: return

        State.controller.save(file, FileType.getType(file), false)
        State.controller.labelInfo(I18N["info.saved_successfully"])
    }
    private fun closeTranslation() {
        if (State.controller.stay()) return

        State.reset()
    }
    private fun bakRecovery() {
        // transfer & open

        if (State.controller.stay()) return

        State.reset()

        val bak = backupChooser.showOpenDialog(State.stage) ?: return
        CFileChooser.lastDirectory = bak.parentFile.parentFile

        fileChooser.title = I18N["chooser.rec"]
        fileChooser.selectedExtensionFilter = fileFilter
        fileChooser.initialFilename = "$RECOVERY_FILE_NAME.$EXTENSION_FILE_MEO"
        val rec = fileChooser.showSaveDialog(State.stage) ?: return

        State.controller.recovery(bak, rec, FileType.getType(rec))
    }

    private fun editComment() {
        showInputArea(State.stage, I18N["m.comment.dialog.title"], State.transFile.comment).ifPresent {
            State.setComment(it)
            State.isChanged = true
        }
    }
    private fun editProjectPictures() {
        // Choose Pics
        val selected = State.transFile.sortedPicNames
        val unselected = Files.walk(State.projectFolder.toPath(), 1)
            .filter {
                if (selected.contains(it.name)) return@filter false
                for (extension in EXTENSIONS_PIC) if (it.extension == extension) return@filter true
                false
            }
            .map { it.name }
            .collect(Collectors.toList())

        showChoiceList(State.stage, unselected, selected).ifPresent {
            if (it.isEmpty()) {
                showInfo(I18N["info.required_at_least_1_pic"], State.stage)
                return@ifPresent
            }

            val picNames = State.transFile.picNames

            // Edit date
            val toAdd = ArrayList<String>()
            for (picName in it) if (!picNames.contains(picName)) toAdd.add(picName)
            val toRemove = ArrayList<String>()
            for (picName in picNames) if (!it.contains(picName)) toRemove.add(picName)

            if (toAdd.size == 0 && toRemove.size == 0) return@ifPresent
            if (toRemove.size != 0) {
                val confirm = showConfirm(I18N["confirm.removing_pic"], State.stage)
                if (!confirm.isPresent || confirm.get() != ButtonType.YES) return@ifPresent
            }

            for (picName in toAdd) State.addPicture(picName)
            for (picName in toRemove) State.removePicture(picName)
            // Mark change
            State.isChanged = true
        }
    }
    private fun addExternalPicture() {
        val files = picChooser.showOpenMultipleDialog(State.stage) ?: return
        val picNames = State.transFile.sortedPicNames

        val conflictList = ArrayList<String>()
        for (file in files) {
            val picName = file.name
            if (picNames.contains(picName)) {
                conflictList.add(picName)
                continue
            }

            // Edit data
            State.addPicture(picName, file)
            // Mark Change
            State.isChanged = true
        }
        if (conflictList.isNotEmpty()) {
            showInfo(
                I18N["common.info"],
                I18N["m.externalPic.dialog.header"],
                conflictList.joinToString(",\n"),
                State.stage
            )
        }
    }

    private fun exportTransFile(type: FileType) {
        exportChooser.extensionFilters.clear()

        when (type) {
            FileType.MeoFile -> {
                exportChooser.extensionFilters.add(meoFilter)
                exportChooser.initialFilename = "$EXPORT_FILE_NAME.$EXTENSION_FILE_MEO"
            }
            FileType.LPFile -> {
                exportChooser.extensionFilters.add(lpFilter)
                exportChooser.initialFilename = "$EXPORT_FILE_NAME.$EXTENSION_FILE_LP"
            }
        }
        val file = exportChooser.showSaveDialog(State.stage) ?: return
        State.controller.export(file, type)
    }
    private fun exportTransPack() {
        val file = exportPackChooser.showSaveDialog(State.stage) ?: return

        State.controller.pack(file)
    }

    private fun settings() {
        val list = ADialogSettings.generateProperties()

        Logger.info("Generated common settings", LOGSRC_DIALOGS)
        Logger.debug("got $list", LOGSRC_DIALOGS)

        var updatePane = false
        var updateRules = false

        for (property in list) {
            /// Too slow, find a faster way
            if (Settings[property.key].asString() == property.value) continue

            when (property.key) {
                Settings.LabelAlpha, Settings.LabelRadius -> updatePane = true
                Settings.LigatureRules -> updateRules = true
            }

            Settings[property.key] = property
        }

        if (updatePane && State.isOpened) State.controller.renderLabelPane()
        if (updateRules) State.controller.updateLigatureRules()
    }
    private fun logs() {
        val list = ADialogLogs.generateProperties()

        Logger.info("Generated logs settings", LOGSRC_DIALOGS)
        Logger.debug("got $list", LOGSRC_DIALOGS)

        for (property in list) {
            /// Too slow, find a faster way
            if (Settings[property.key].asString() == property.value) continue

            when (property.key) {
                Settings.LogLevelPreference -> Logger.level = Logger.LogType.getType(property.value)
            }

            Settings[property.key] = property
        }

        Logger.level = Logger.LogType.getType(Settings[Settings.LogLevelPreference].asString())
    }
    private fun about() {
        showLink(
            State.stage,
            iconImageView,
            I18N["m.about.dialog.title"],
            null,
            StringBuilder()
                .append(INFO["application.name"]).append(" - ").append(UpdateChecker.V).append("\n")
                .append("Developed By ").append(INFO["application.vendor"]).append("\n")
                .toString(),
            INFO["application.link"]
        ) {
            State.application.hostServices.showDocument(INFO["application.url"])
        }
    }
    private fun crash() {
        if (properties[CrashCount] == null) properties[CrashCount] = 0

        properties[CrashCount] = (properties[CrashCount] as Int) + 1
        if (properties[CrashCount] as Int >= 5) {
            properties[CrashCount] = 0
            val confirm = showWarning(I18N["confirm.extra"], State.stage)
            if (confirm.isPresent && confirm.get() == ButtonType.YES) {
                State.controller.justMonika()
            } else return
        }

        throw RuntimeException("Crash")
    }
    private const val CrashCount = "C_Crash_Count"

}
