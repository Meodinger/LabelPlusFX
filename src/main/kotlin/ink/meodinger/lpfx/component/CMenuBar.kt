package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.action.Action
import ink.meodinger.lpfx.action.ActionType
import ink.meodinger.lpfx.action.ComplexAction
import ink.meodinger.lpfx.action.PictureAction
import ink.meodinger.lpfx.component.common.CFileChooser
import ink.meodinger.lpfx.component.properties.ADialogLogs
import ink.meodinger.lpfx.component.properties.ADialogSettings
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Preference
import ink.meodinger.lpfx.options.RecentFiles
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.type.LPFXTask
import ink.meodinger.lpfx.util.collection.addFirst
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.dialog.*
import ink.meodinger.lpfx.util.doNothing
import ink.meodinger.lpfx.util.file.notExists
import ink.meodinger.lpfx.util.platform.isMac
import ink.meodinger.lpfx.util.property.BidirectionalListener
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.onNew
import ink.meodinger.lpfx.util.property.divAssign
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.INFO
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.string.deleteTail
import ink.meodinger.lpfx.util.translator.convert2Simplified
import ink.meodinger.lpfx.util.translator.convert2Traditional

import javafx.beans.binding.Bindings
import javafx.beans.property.ListProperty
import javafx.beans.property.SimpleListProperty
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
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
class CMenuBar(private val state: State) : MenuBar() {

    // ----- Recent ----- //

    private val mOpenRecent = Menu(I18N["m.recent"])
    private fun openRecentTranslation(item: MenuItem) {
        // Open recent, remove item if not exist

        if (state.controller.stay()) return

        val file = File(item.text)
        if (file.notExists()) {
            showError(state.stage, String.format(I18N["error.file_not_exist.s"], file.path))
            RecentFiles.remove(file)
            mOpenRecent.items.remove(item)
            return
        }

        state.reset()

        state.controller.open(file)
    }

    private val recentFilesProperty: ListProperty<File> = SimpleListProperty()
    fun recentFilesProperty(): ListProperty<File> = recentFilesProperty
    val recentFiles: ObservableList<File> by recentFilesProperty

    // ----- Dialogs ----- //

    private val cheatSheet     by lazy { CCheatSheet(state.application.hostServices) }
    private val onlineDict     by lazy { COnlineDict() }
    private val dialogLogs     by lazy { ADialogLogs() }
    private val dialogSettings by lazy { ADialogSettings() }

    // ----- Choosers ----- //

    private val anyFilter         = FileChooser.ExtensionFilter(I18N["filetype.any"], "*.*")

    private val picChooser        = CFileChooser()
    private val picFilter         = FileChooser.ExtensionFilter(I18N["filetype.pictures"], List(EXTENSIONS_PIC.size) { index -> "*.${EXTENSIONS_PIC[index]}" })
    private val pngFilter         = FileChooser.ExtensionFilter(I18N["filetype.picture_png"], "*.${EXTENSION_PIC_PNG}")
    private val jpgFilter         = FileChooser.ExtensionFilter(I18N["filetype.picture_jpg"], "*.${EXTENSION_PIC_JPG}")
    private val jpegFilter        = FileChooser.ExtensionFilter(I18N["filetype.picture_jpeg"], "*.${EXTENSION_PIC_JPEG}")

    private val newChooser        = CFileChooser()
    private val fileChooser       = CFileChooser()
    private val exportChooser     = CFileChooser()
    private val fileFilter        = FileChooser.ExtensionFilter(I18N["filetype.translation"],  List(EXTENSIONS_FILE.size) { index -> "*.${EXTENSIONS_FILE[index]}" })
    private val lpFilter          = FileChooser.ExtensionFilter(I18N["filetype.translation_lp"], "*.${EXTENSION_FILE_LP}")
    private val meoFilter         = FileChooser.ExtensionFilter(I18N["filetype.translation_meo"], "*.${EXTENSION_FILE_MEO}")

    private val backupChooser     = FileChooser()
    private val bakFilter         = FileChooser.ExtensionFilter(I18N["filetype.backup"], "*.${EXTENSION_BAK}")

    private val exportPackChooser = CFileChooser()
    private val packFilter        = FileChooser.ExtensionFilter(I18N["filetype.pack"], "*.${EXTENSION_PACK}")

    init {
        picChooser.title = I18N["m.externalPic.chooser.title"]
        picChooser.extensionFilters.addAll(picFilter, pngFilter, jpgFilter, jpegFilter)

        newChooser.title = I18N["chooser.new"]
        newChooser.extensionFilters.addAll(anyFilter, fileFilter, lpFilter, meoFilter)

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
                disableProperty().bind(!state.openedProperty())
            }
            separator()
            item(I18N["m.save"]) {
                does { saveTranslation() }
                disableProperty().bind(!state.openedProperty())
                accelerator = KeyCodeCombination(
                    KeyCode.S,
                    if (isMac) KeyCombination.META_DOWN else KeyCombination.CONTROL_DOWN
                )
            }
            item(I18N["m.save_as"]) {
                does { saveAsTranslation() }
                disableProperty().bind(!state.openedProperty())
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
                does { if (!state.controller.stay()) state.application.exit() }
            }
        }
        menu(I18N["mm.edit"]) {
            item(I18N["m.undo"]) {
                does { state.undo() }
                disableProperty().bind(!state.canUndoProperty())
                accelerator = KeyCodeCombination(
                    KeyCode.Z,
                    if (isMac) KeyCombination.META_DOWN else KeyCombination.CONTROL_DOWN
                )
            }
            item(I18N["m.redo"]) {
                does { state.redo() }
                disableProperty().bind(!state.canRedoProperty())
                accelerator = KeyCodeCombination(
                    KeyCode.Z,
                    KeyCombination.SHIFT_DOWN,
                    if (isMac) KeyCombination.META_DOWN else KeyCombination.CONTROL_DOWN
                )
            }
            separator()
            item(I18N["m.comment"]) {
                does { editComment() }
                disableProperty().bind(!state.openedProperty())
            }
            separator()
            item(I18N["m.projectPics"]) {
                does { editProjectPictures() }
                disableProperty().bind(!state.openedProperty())
            }
            item(I18N["m.externalPic"]) {
                does { addExternalPicture() }
                disableProperty().bind(!state.openedProperty())
            }
            item(I18N["m.specify"]) {
                does { specifyPictures() }
                disableProperty().bind(!state.openedProperty())
            }
        }
        menu(I18N["mm.export"]) {
            item(I18N["m.lp"]) {
                does { exportTransFile(FileType.LPFile) }
                disableProperty().bind(!state.openedProperty())
                accelerator = KeyCodeCombination(
                    KeyCode.E,
                    if (isMac) KeyCombination.META_DOWN else KeyCombination.CONTROL_DOWN
                )
            }
            item(I18N["m.meo"]) {
                does { exportTransFile(FileType.MeoFile) }
                disableProperty().bind(!state.openedProperty())
                accelerator = KeyCodeCombination(
                    KeyCode.E,
                    KeyCombination.SHIFT_DOWN,
                    if (isMac) KeyCombination.META_DOWN else KeyCombination.CONTROL_DOWN
                )
            }
            separator()
            item(I18N["m.pack"]) {
                does { exportTransPack() }
                disableProperty().bind(!state.openedProperty())
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
            item(I18N["m.update"]) {
                does { checkUpdate() }
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
            checkItem(I18N["m.dict"]) {
                does { showDict() }
                accelerator = KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN)
                BidirectionalListener.listen(
                    selectedProperty(), { _, _, _ -> isSelected = onlineDict.isShowing },
                    onlineDict.showingProperty(), { _, _, new  -> isSelected = new }
                )
            }
            separator()
            item(I18N["m.cht2zh"]) {
                does { cht2zh() }
                disableProperty().bind(!state.openedProperty())
            }
            item(I18N["m.zh2cht"]) {
                does { cht2zh(true) }
                disableProperty().bind(!state.openedProperty())
            }
            separator()
            checkItem(I18N["m.stats_bar"]) {
                selectedProperty().bindBidirectional(Preference.showStatsBarProperty())
            }
        }

        recentFilesProperty.addListener(ListChangeListener {
            while (it.next()) {
                if (it.wasPermutated()) {
                    // will not happen
                    throw IllegalStateException("Permuted: $it")
                } else if (it.wasUpdated()) {
                    // will not happen
                    throw IllegalStateException("Updated: $it")
                } else {
                    if (it.wasRemoved()) {
                        it.removed.forEach { file ->
                            mOpenRecent.items.removeIf { item -> item.text == file.path }
                        }
                    }
                    if (it.wasAdded()) {
                        it.addedSubList.forEach { file ->
                            mOpenRecent.items.addFirst(MenuItem(file.path) does { openRecentTranslation(this) })
                        }
                    }
                }
            }
        })
    }

    private fun newTranslation() {
        if (state.controller.stay()) return

        state.reset()

        val extension = if (Settings.useMeoFileAsDefault) EXTENSION_FILE_MEO else EXTENSION_FILE_LP
        val filename = "Nova traduko" // It's Esperanto!
        newChooser.initialFilename = "$filename.$extension"

        val file = newChooser.showSaveDialog(state.stage)?.let {
            if (EXTENSIONS_FILE.contains(it.extension)) {
                if (filename == it.nameWithoutExtension) {
                    it.parentFile.let { f -> f.resolve("${f.name}.${it.extension}") }
                } else it
            } else File("${it.nameWithoutExtension}.$extension")
        } ?: return
        if (file.exists()) {
            val confirm = showConfirm(state.stage, I18N["m.new.dialog.overwrite"])
            if (confirm.isEmpty || confirm.get() == ButtonType.NO) return
        }

        val projectFolder = state.controller.new(file)
        if (projectFolder != null) state.controller.open(file, projectFolder = projectFolder)
    }
    private fun openTranslation() {
        if (state.controller.stay()) return

        fileChooser.title = I18N["chooser.open"]
        fileChooser.selectedExtensionFilter = fileFilter
        fileChooser.initialFilename = ""
        val file = fileChooser.showOpenDialog(state.stage) ?: return

        state.reset()

        state.controller.open(file)
    }
    private fun saveTranslation() {
        state.controller.save(state.translationFile, silent = true)
    }
    private fun saveAsTranslation() {
        fileChooser.title = I18N["chooser.save"]
        fileChooser.selectedExtensionFilter = fileFilter
        fileChooser.initialFilename = state.translationFile.name
        val file = fileChooser.showSaveDialog(state.stage) ?: return

        state.controller.save(file)
    }
    private fun closeTranslation() {
        if (state.controller.stay()) return

        state.reset()
    }
    private fun bakRecovery() {
        if (state.controller.stay()) return

        backupChooser.initialDirectory = state.getBakFolder() ?: CFileChooser.lastDirectory
        val bak = backupChooser.showOpenDialog(state.stage) ?: return

        fileChooser.title = I18N["chooser.rec"]
        fileChooser.selectedExtensionFilter = fileFilter
        fileChooser.initialFilename = "Re.${bak.parentFile.parentFile.name}.$EXTENSION_FILE_MEO"
        val rec = fileChooser.showSaveDialog(state.stage) ?: return

        state.reset()

        state.controller.recovery(bak, rec)
    }

    private fun editComment() {
        showInputArea(state.stage, I18N["m.comment.dialog.title"], state.transFile.comment).ifPresent {
            state.doAction(object : Action {

                private val oriComment = state.transFile.comment

                override val type: ActionType = ActionType.CHANGE

                override fun commit() {
                    state.transFile.comment = it
                }

                override fun revert() {
                    state.transFile.comment = oriComment
                }

            })
            state.isChanged = true
        }
    }
    private fun editProjectPictures() {
        // Choose Pics
        val selected = state.transFile.sortedPicNames
        val unselected = Files.walk(state.transFile.projectFolder.toPath(), 1)
            .filter {
                if (selected.contains(it.name)) return@filter false
                for (extension in EXTENSIONS_PIC) if (it.extension == extension) return@filter true
                false
            }.map(Path::name).collect(Collectors.toList())

        showChoiceList(state.stage, unselected, selected).ifPresent {
            if (it.isEmpty()) {
                showInfo(state.stage, I18N["info.required_at_least_1_pic"])
                return@ifPresent
            }

            val picNames = state.transFile.sortedPicNames

            // Edit date
            val toAdd = ArrayList<String>()
            for (picName in it) if (!picNames.contains(picName)) toAdd.add(picName)
            val toRemove = ArrayList<String>()
            for (picName in picNames) if (!it.contains(picName)) toRemove.add(picName)

            if (toAdd.size == 0 && toRemove.size == 0) return@ifPresent
            if (toRemove.size != 0) {
                val confirm = showConfirm(state.stage, I18N["confirm.removing_pic"])
                if (!confirm.isPresent || confirm.get() != ButtonType.YES) return@ifPresent
            }

            state.doAction(ComplexAction.of(
                toAdd.map { picName -> PictureAction(ActionType.ADD, state, picName) },
                toRemove.map { picName -> PictureAction(ActionType.REMOVE, state, picName) }
            ))
            state.currentPicName = state.transFile.sortedPicNames[0]
            // Mark change
            state.isChanged = true
        }
    }
    private fun addExternalPicture() {
        val files = picChooser.showOpenMultipleDialog(state.stage) ?: return
        val picNames = state.transFile.sortedPicNames

        val conflictList = ArrayList<String>()
        for (file in files) {
            val picName = file.name
            if (picNames.contains(picName)) {
                conflictList.add(picName)
                continue
            }

            // Edit data
            state.doAction(PictureAction(ActionType.ADD, state, picName, file))
            // Mark Change
            state.isChanged = true
        }
        if (conflictList.isNotEmpty()) {
            showWarning(
                state.stage,
                I18N["m.externalPic.dialog.header"],
                conflictList.joinToString(",\n"),
                I18N["common.warning"]
            )
        }
    }
    private fun specifyPictures() {
        val completed = state.controller.specifyPicFiles() ?: return
        if (!completed) showInfo(state.stage, I18N["specify.info.incomplete"])
        state.controller.requestRepaint()
    }

    private fun exportTransFile(type: FileType) {
        exportChooser.extensionFilters.clear()
        exportChooser.extensionFilters.add(when (type) {
            FileType.LPFile -> lpFilter
            FileType.MeoFile -> meoFilter
        })

        val exportName =
            if (Settings.useExportNameTemplate) Settings.exportNameTemplate
                .replace(Settings.VARIABLE_FILENAME, state.translationFile.nameWithoutExtension)
                .replace(Settings.VARIABLE_DIRNAME, state.getFileFolder()!!.name)
                .replace(Settings.VARIABLE_PROJECT, state.transFile.projectFolder.name)
            else state.getFileFolder()!!.name
        exportChooser.initialFilename = "$exportName.${type.extension}"

        val file = exportChooser.showSaveDialog(state.stage) ?: return
        state.controller.export(file, type)
    }
    private fun exportTransPack() {
        exportPackChooser.initialFilename = "${state.getFileFolder()!!.name}.$EXTENSION_PACK"
        val file = exportPackChooser.showSaveDialog(state.stage) ?: return

        state.controller.pack(file)
    }

    private fun settings() {
        dialogSettings.owner ?: dialogSettings.initOwner(state.stage)

        val map = dialogSettings.generateProperties()

        Logger.info("Generated common settings", LOGSRC_DIALOGS)
        Logger.debug("got $map", LOGSRC_DIALOGS)

        @Suppress("UNCHECKED_CAST")
        for ((key, value) in map) when (key) {
            Settings.DefaultGroupNameList     -> Settings.defaultGroupNameList        /= value as List<String>
            Settings.DefaultGroupColorHexList -> Settings.defaultGroupColorHexList    /= value as List<String>
            Settings.IsGroupCreateOnNewTrans  -> Settings.isGroupCreateOnNewTransList /= value as List<Boolean>
            Settings.NewPictureScale          -> Settings.newPictureScalePicture       = value as CLabelPane.NewPictureScale
            Settings.ViewModes                -> Settings.viewModes                   /= value as List<ViewMode>
            Settings.LabelRadius              -> Settings.labelRadius                  = value as Double
            Settings.LabelColorOpacity        -> Settings.labelColorOpacity            = value as Double
            Settings.LabelTextOpaque          -> Settings.labelTextOpaque              = value as Boolean
            Settings.LigatureRules            -> Settings.ligatureRules               /= value as List<Pair<String, String>>
            Settings.InstantTranslate         -> Settings.instantTranslate             = value as Boolean
            Settings.UseMeoFileAsDefault      -> Settings.useMeoFileAsDefault          = value as Boolean
            Settings.UseExportNameTemplate    -> Settings.useExportNameTemplate        = value as Boolean
            Settings.ExportNameTemplate       -> Settings.exportNameTemplate           = value as String
            Settings.AutoCheckUpdate          -> Settings.autoCheckUpdate              = value as Boolean
            else -> doNothing()
        }
    }
    private fun logs() {
        dialogLogs.owner ?: dialogLogs.initOwner(state.stage)

        val map = dialogLogs.generateProperties()

        Logger.info("Generated logs settings", LOGSRC_DIALOGS)
        Logger.debug("got $map", LOGSRC_DIALOGS)

        @Suppress("UNCHECKED_CAST")
        for ((key, value) in map) when (key) {
            Settings.LogLevel -> Settings.logLevel = Logger.LogLevel.values()[value as Int]
            else -> doNothing()
        }

        Logger.level = Settings.logLevel
    }
    private fun about() {
        showLink(
            state.stage,
            iconImageView,
            I18N["m.about.dialog.title"],
            null,
            StringBuilder()
                .append(INFO["application.name"]).append(" - ").append(V).append("\n")
                .append("Developed By ").append(INFO["application.vendor"]).append("\n")
                .toString(),
            INFO["application.link"]
        ) {
            state.application.hostServices.showDocument(INFO["application.url"])
        }
    }
    private fun checkUpdate() {
        state.controller.checkUpdate(true)
    }
    private fun cheatSheet() {
        cheatSheet.show()
        cheatSheet.toFront()
    }
    private fun crash() {
        throw RuntimeException("Crash")
    }

    private fun cht2zh(inverse: Boolean = false) {
        val converter = if (inverse) ::convert2Traditional else ::convert2Simplified

        // TODO: Use Action
        val task = object : LPFXTask<Unit>() {
            val DELIMITER = "#|#"
            val state = this@CMenuBar.state

            override fun call() {
               state.isChanged = true

                val picNames = state.transFile.sortedPicNames
                val picCount = state.transFile.picCount
                for ((picIndex, picName) in picNames.withIndex()) {
                    if (isCancelled) return
                    updateProgress(1.0 * (picIndex + 1) / picCount, 1.0)

                    val labels = state.transFile.getTransList(picName)
                    val labelCount = labels.size

                    if (labelCount == 0) continue

                    val builder = StringBuilder()
                    for (label in labels) builder.append(label.text).append(DELIMITER)
                    val iterator = converter(builder.deleteTail(DELIMITER).toString()).split(DELIMITER).also {
                        if (it.size != labelCount) {
                            updateMessage("at [$picName] ${it.joinToString()}")
                            return
                        }
                    }.iterator()
                    // Sometimes will get whitespaces at label start
                    for (label in labels) label.text = iterator.next().trim()
                }
            }
        }

        Dialog<ButtonType>().apply {
            initOwner(state.stage)
            dialogPane.buttonTypes.add(ButtonType.CANCEL)

            val button = dialogPane.lookupButton(ButtonType.CANCEL)
            fun cancel() = button.fireEvent(ActionEvent())

            dialogPane.withContent(ProgressBar()) {
                progressProperty().bind(task.progressProperty())
                progressProperty().addListener(onNew<Number, Double> { if (it >= 1.0) cancel() })
            }

            resultProperty().addListener(onNew { task.cancel() })
            task.messageProperty().addListener(onNew {
                cancel()
                showError(state.stage, "Error: $it")
            })
        }.show()

        task()
    }
    private fun showDict() {
        onlineDict.showDict(state.stage)
        onlineDict.toFront()
    }

}
