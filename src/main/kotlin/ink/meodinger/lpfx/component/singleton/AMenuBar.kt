package ink.meodinger.lpfx.component.singleton

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.component.CLabelPane
import ink.meodinger.lpfx.component.common.CFileChooser
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.RecentFiles
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.type.LPFXTask
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.dialog.*
import ink.meodinger.lpfx.util.doNothing
import ink.meodinger.lpfx.util.file.existsOrNull
import ink.meodinger.lpfx.util.platform.isMac
import ink.meodinger.lpfx.util.property.onNew
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.INFO
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.string.deleteTail
import ink.meodinger.lpfx.util.translator.convert2Simplified
import ink.meodinger.lpfx.util.translator.convert2Traditional

import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
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

    private val newChooser        = DirectoryChooser()
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

        newChooser.title = I18N["chooser.new"]
        newChooser.initialDirectoryProperty().bind(CFileChooser.lastDirectoryProperty())

        // fileChooser's tile will change
        fileChooser.extensionFilters.addAll(fileFilter, meoFilter, lpFilter)

        backupChooser.title = I18N["chooser.bak"]
        backupChooser.extensionFilters.add(bakFilter)

        exportChooser.title = I18N["chooser.export"]
        // exportChooser's filter will change

        exportPackChooser.title = I18N["chooser.pack"]
        exportPackChooser.extensionFilters.add(packFilter)

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
                disableProperty().bind(Bindings.createBooleanBinding(items::isEmpty, items))
            }
            item(I18N["m.close"]) {
                does { closeTranslation() }
                disableProperty().bind(!State.isOpenedProperty())
            }
            separator()
            item(I18N["m.save"]) {
                does { saveTranslation() }
                disableProperty().bind(!State.isOpenedProperty())
                accelerator = KeyCodeCombination(
                    KeyCode.S,
                    if (isMac) KeyCombination.META_DOWN else KeyCombination.CONTROL_DOWN
                )
            }
            item(I18N["m.save_as"]) {
                does { saveAsTranslation() }
                disableProperty().bind(!State.isOpenedProperty())
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
                does { if (!State.controller.stay()) State.application.exit() }
            }
        }
        menu(I18N["mm.edit"]) {
            item(I18N["m.comment"]) {
                does { editComment() }
                disableProperty().bind(!State.isOpenedProperty())
            }
            separator()
            item(I18N["m.projectPics"]) {
                does { editProjectPictures() }
                disableProperty().bind(!State.isOpenedProperty())
            }
            item(I18N["m.externalPic"]) {
                does { addExternalPicture() }
                disableProperty().bind(!State.isOpenedProperty())
            }
            item(I18N["m.specify"]) {
                does { specifyPictures() }
                disableProperty().bind(!State.isOpenedProperty())
            }
        }
        menu(I18N["mm.export"]) {
            item(I18N["m.lp"]) {
                does { exportTransFile(FileType.LPFile) }
                disableProperty().bind(!State.isOpenedProperty())
                accelerator = KeyCodeCombination(
                    KeyCode.E,
                    if (isMac) KeyCombination.META_DOWN else KeyCombination.CONTROL_DOWN
                )
            }
            item(I18N["m.meo"]) {
                does { exportTransFile(FileType.MeoFile) }
                disableProperty().bind(!State.isOpenedProperty())
                accelerator = KeyCodeCombination(
                    KeyCode.E,
                    KeyCombination.SHIFT_DOWN,
                    if (isMac) KeyCombination.META_DOWN else KeyCombination.CONTROL_DOWN
                )
            }
            separator()
            item(I18N["m.pack"]) {
                does { exportTransPack() }
                disableProperty().bind(!State.isOpenedProperty())
                accelerator = KeyCodeCombination(
                    KeyCode.S,
                    KeyCombination.ALT_DOWN,
                    KeyCombination.SHIFT_DOWN, // Ctrl+Alt+S is occupied by QQ Screen Record
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
            item(I18N["m.cheat"]) {
                does { cheatSheet() }
            }
            separator()
            item(I18N["m.crash"]) {
                does { crash() }
            }
        }
        menu(I18N["mm.tools"]) {
            item(I18N["m.dict"]) {
                does { toggleDict() }
                accelerator = KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN)
            }
            separator()
            item(I18N["m.cht2zh"]) {
                does { cht2zh() }
                disableProperty().bind(!State.isOpenedProperty())
            }
            item(I18N["m.zh2cht"]) {
                does { cht2zh(true) }
                disableProperty().bind(!State.isOpenedProperty())
            }
        }
    }

    fun updateRecentFiles() {
        mOpenRecent.items.clear()

        for (path in RecentFiles.getAll()) if (File(path).exists())
            mOpenRecent.items.add(MenuItem(path) does { openRecentTranslation(this) })
    }
    private fun openRecentTranslation(item: MenuItem) {
        // Open recent, remove item if not exist

        if (State.controller.stay()) return

        val path = item.text
        val file = File(path).existsOrNull() ?: run {
            showError(State.stage, String.format(I18N["error.file_not_exist.s"], path))
            RecentFiles.remove(path)
            mOpenRecent.items.remove(item)
            return
        }

        State.reset()

        State.controller.open(file)
    }

    private fun newTranslation() {
        // new & open

        if (State.controller.stay()) return

        State.reset()

        val fileDirectory = newChooser.showDialog(State.stage) ?: return
        val extension = if (Settings.useMeoFileAsDefault) EXTENSION_FILE_MEO else EXTENSION_FILE_LP

        val file = fileDirectory.resolve("${fileDirectory.name}.$extension")
        if (file.exists()) {
            val confirm = showConfirm(State.stage, I18N["m.new.dialog.overwrite"])
            if (confirm.isEmpty || confirm.get() == ButtonType.NO) return
        }

        val projectFolder = State.controller.new(file)
        if (projectFolder != null) State.controller.open(file, projectFolder = projectFolder)
    }
    private fun openTranslation() {
        // open

        if (State.controller.stay()) return

        State.reset()

        fileChooser.title = I18N["chooser.open"]
        fileChooser.selectedExtensionFilter = fileFilter
        fileChooser.initialFilename = ""
        val file = fileChooser.showOpenDialog(State.stage) ?: return

        State.controller.open(file)
    }
    private fun saveTranslation() {
        // save

        State.controller.save(State.translationFile, silent = true)
    }
    private fun saveAsTranslation() {
        // save

        fileChooser.title = I18N["chooser.save"]
        fileChooser.selectedExtensionFilter = fileFilter
        fileChooser.initialFilename = State.translationFile.name
        val file = fileChooser.showSaveDialog(State.stage) ?: return

        State.controller.save(file)
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
        val translationFolder = bak.parentFile.parentFile

        // Not update
        CFileChooser.lastDirectory = translationFolder

        fileChooser.title = I18N["chooser.rec"]
        fileChooser.selectedExtensionFilter = fileFilter
        fileChooser.initialFilename = "Re.${translationFolder.name}.$EXTENSION_FILE_MEO"
        val rec = fileChooser.showSaveDialog(State.stage) ?: return

        State.controller.recovery(bak, rec)
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
            }.map(Path::name).collect(Collectors.toList())

        showChoiceList(State.stage, unselected, selected).ifPresent {
            if (it.isEmpty()) {
                showInfo(State.stage, I18N["info.required_at_least_1_pic"])
                return@ifPresent
            }

            val picNames = State.transFile.picNames

            // Edit date
            val toAdd = HashSet<String>()
            for (picName in it) if (!picNames.contains(picName)) toAdd.add(picName)
            val toRemove = HashSet<String>()
            for (picName in picNames) if (!it.contains(picName)) toRemove.add(picName)

            if (toAdd.size == 0 && toRemove.size == 0) return@ifPresent
            if (toRemove.size != 0) {
                val confirm = showConfirm(State.stage, I18N["confirm.removing_pic"])
                if (!confirm.isPresent || confirm.get() != ButtonType.YES) return@ifPresent
            }

            for (picName in toAdd) State.addPicture(picName)
            for (picName in toRemove) State.removePicture(picName)
            // Update View
            State.currentPicName = State.transFile.sortedPicNames[0]
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
                State.stage,
                I18N["m.externalPic.dialog.header"],
                conflictList.joinToString(",\n"),
                I18N["common.info"]
            )
        }
    }
    private fun specifyPictures() {
        val completed = State.controller.specifyPicFiles()
        if (completed != null) {
            if (!completed) showInfo(State.stage, I18N["specify.info.incomplete"])
            State.currentPicName = State.transFile.sortedPicNames[0]
        }
    }

    private fun exportTransFile(type: FileType) {
        exportChooser.extensionFilters.clear()
        exportChooser.extensionFilters.add(when (type) {
            FileType.LPFile -> lpFilter
            FileType.MeoFile -> meoFilter
        })

        val exportName =
            if (Settings.useExportNameTemplate) Settings.exportNameTemplate
                .replace(Settings.VARIABLE_FILENAME, State.translationFile.nameWithoutExtension)
                .replace(Settings.VARIABLE_DIRNAME, State.getFileFolder().name)
                .replace(Settings.VARIABLE_PROJECT, State.projectFolder.name)
            else State.getFileFolder().name
        exportChooser.initialFilename = "$exportName.${type.extension}"

        val file = exportChooser.showSaveDialog(State.stage) ?: return
        State.controller.export(file, type)
    }
    private fun exportTransPack() {
        exportPackChooser.initialFilename = "${State.getFileFolder().name}.$EXTENSION_PACK"
        val file = exportPackChooser.showSaveDialog(State.stage) ?: return

        State.controller.pack(file)
    }

    private fun settings() {
        val list = ADialogSettings.generateProperties()

        Logger.info("Generated common settings", LOGSRC_DIALOGS)
        Logger.debug("got $list", LOGSRC_DIALOGS)

        for (property in list) {
            /// Too slow, find a faster way
            when (property.key) {
                Settings.DefaultGroupNameList     -> Settings.defaultGroupNameList = FXCollections.observableList(property.asStringList())
                Settings.DefaultGroupColorHexList -> Settings.defaultGroupColorHexList = FXCollections.observableList(property.asStringList())
                Settings.IsGroupCreateOnNewTrans  -> Settings.isGroupCreateOnNewTransList = FXCollections.observableList(property.asBooleanList())
                Settings.ScaleOnNewPictureOrdinal -> Settings.newPictureScalePicture = CLabelPane.NewPictureScale.values()[property.asInteger()]
                Settings.ViewModeOrdinals         -> Settings.viewModes = FXCollections.observableList(property.asIntegerList().map { ViewMode.values()[it] })
                Settings.LabelRadius              -> Settings.labelRadius = property.asDouble()
                Settings.LabelAlpha               -> Settings.labelAlpha = property.asString()
                Settings.LigatureRules            -> Settings.ligatureRules = FXCollections.observableList(property.asPairList())
                Settings.InstantTranslate         -> Settings.instantTranslate = property.asBoolean()
                Settings.UseMeoFileAsDefault      -> Settings.useMeoFileAsDefault = property.asBoolean()
                Settings.UseExportNameTemplate    -> Settings.useExportNameTemplate = property.asBoolean()
                Settings.ExportNameTemplate       -> Settings.exportNameTemplate = property.asString()
                else -> doNothing()
            }
        }
    }
    private fun logs() {
        val list = ADialogLogs.generateProperties()

        Logger.info("Generated logs settings", LOGSRC_DIALOGS)
        Logger.debug("got $list", LOGSRC_DIALOGS)

        Logger.level = Settings.logLevel
    }
    private fun about() {
        showLink(
            State.stage,
            iconImageView,
            I18N["m.about.dialog.title"],
            null,
            StringBuilder()
                .append(INFO["application.name"]).append(" - ").append(V).append("\n")
                .append("Developed By ").append(INFO["application.vendor"]).append("\n")
                .toString(),
            INFO["application.link"]
        ) {
            State.application.hostServices.showDocument(INFO["application.url"])
        }
    }
    private fun cheatSheet() {
        ADialogCheatSheet.show()
        ADialogCheatSheet.toFront()
    }
    private fun crash() {
        throw RuntimeException("Crash")
    }

    private fun cht2zh(inverse: Boolean = false) {
        val converter = if (inverse) ::convert2Traditional else ::convert2Simplified

        val task = object : LPFXTask<Unit>() {
            val DELIMITER = "#|#"

            override fun call() {
                State.isChanged = true

                val picNames = State.transFile.sortedPicNames
                val picCount = State.transFile.picCount
                for ((picIndex, picName) in picNames.withIndex()) {
                    handleCancel { return }
                    val labels = State.transFile.getTransList(picName)
                    var labelIndex = 0
                    val labelCount = labels.size

                    if (labelCount == 0) continue

                    val builder = StringBuilder()
                    for (label in labels) builder.append(label.text).append(DELIMITER)
                    val iterator = converter(builder.deleteTail(DELIMITER).toString()).split(DELIMITER).also {
                        if (it.size != labelCount) {
                            updateMessage("at [$picName] ${it.joinToString()}")
                            cancel()
                            return
                        }
                    }.iterator()
                    for (label in labels) {
                        labelIndex++
                        updateProgress((1.0 * (picIndex + labelIndex / labelCount) / picCount), 1.0)
                        label.text = iterator.next().trim() // Sometimes will get whitespaces at label start
                    }
                }
            }
        }

        Dialog<Unit>().apply {
            initOwner(State.stage)
            dialogPane.withContent(ProgressBar()) {
                progressProperty().bind(task.progressProperty())
                progressProperty().addListener(onNew<Number, Double> {
                    if (it >= 1.0) {
                        result = Unit
                        close()
                    }
                })
            }

            task.messageProperty().addListener(onNew {
                if (it.isNotBlank()) {
                    result = Unit
                    close()
                    showError(State.stage, "Error: $it")
                }
            })
        }.show()

        task()
    }
    fun toggleDict() {
        AOnlineDict.x = State.stage.x - (AOnlineDict.width + COMMON_GAP * 2) + State.stage.width
        AOnlineDict.y = State.stage.y + (COMMON_GAP * 2)
        AOnlineDict.show()
    }

}
