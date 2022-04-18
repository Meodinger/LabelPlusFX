package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.action.*
import ink.meodinger.lpfx.component.common.CFileChooser
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Preference
import ink.meodinger.lpfx.options.RecentFiles
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.type.LPFXTask
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.dialog.*
import ink.meodinger.lpfx.util.doNothing
import ink.meodinger.lpfx.util.file.notExists
import ink.meodinger.lpfx.util.image.resizeByRadius
import ink.meodinger.lpfx.util.property.*
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
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
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
 * A MenuBar for main scene
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

    // ----- Choosers ----- //

    private val filterAny     = FileChooser.ExtensionFilter(I18N["file_type.any"], "*.*")

    private val chooserPic    = CFileChooser()
    private val filterPic     = FileChooser.ExtensionFilter(I18N["file_type.pictures"], List(EXTENSIONS_PIC.size) { index -> "*.${EXTENSIONS_PIC[index]}" })
    private val filterPNG     = FileChooser.ExtensionFilter(I18N["file_type.picture_png"], "*.${EXTENSION_PIC_PNG}")
    private val filterJPG     = FileChooser.ExtensionFilter(I18N["file_type.picture_jpg"], "*.${EXTENSION_PIC_JPG}", "*.${EXTENSION_PIC_JPEG}")
    private val filterGIF     = FileChooser.ExtensionFilter(I18N["file_type.picture_gif"], "*.${EXTENSION_PIC_GIF}")
    private val filterBMP     = FileChooser.ExtensionFilter(I18N["file_type.picture_bmp"], "*.${EXTENSION_PIC_BMP}")
    private val filterTIFF    = FileChooser.ExtensionFilter(I18N["file_type.picture_tiff"], "*.${EXTENSION_PIC_TIF}", "*.${EXTENSION_PIC_TIFF}")

    private val chooserNew    = CFileChooser()
    private val chooserFile   = CFileChooser()
    private val chooserExport = CFileChooser()
    private val filterFile    = FileChooser.ExtensionFilter(I18N["file_type.translation"],  List(EXTENSIONS_FILE.size) { index -> "*.${EXTENSIONS_FILE[index]}" })
    private val filterLP      = FileChooser.ExtensionFilter(I18N["file_type.translation_lp"], "*.${EXTENSION_FILE_LP}")
    private val filterMEO     = FileChooser.ExtensionFilter(I18N["file_type.translation_meo"], "*.${EXTENSION_FILE_MEO}")

    private val chooserBackup = FileChooser()
    private val filterBak     = FileChooser.ExtensionFilter(I18N["file_type.backup"], "*.${EXTENSION_BAK}")

    private val chooserPack   = CFileChooser()
    private val filterPack    = FileChooser.ExtensionFilter(I18N["file_type.pack"], "*.${EXTENSION_PACK}")

    init {
        chooserPic.title = I18N["m.externalPic.chooser.title"]
        chooserPic.extensionFilters.addAll(filterPic, filterPNG, filterJPG, filterGIF, filterBMP, filterTIFF)

        chooserNew.title = I18N["chooser.new"]
        chooserNew.extensionFilters.addAll(filterAny, filterFile, filterLP, filterMEO)

        // fileChooser's tile will change
        chooserFile.extensionFilters.addAll(filterFile, filterMEO, filterLP)

        chooserBackup.title = I18N["chooser.bak"]
        chooserBackup.extensionFilters.addAll(filterBak, filterAny)

        chooserExport.title = I18N["chooser.export"]
        // exportChooser's filter will change

        chooserPack.title = I18N["chooser.pack"]
        chooserPack.extensionFilters.add(filterPack)

        menu(I18N["mm.file"]   + "(_F)") {
            item(I18N["m.new"]) {
                does { newTranslation() }
                accelerator = KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN)
            }
            item(I18N["m.open"]) {
                does { openTranslation() }
                accelerator = KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN)
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
                accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN)
            }
            item(I18N["m.save_as"]) {
                does { saveAsTranslation() }
                disableProperty().bind(!state.openedProperty())
                accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)
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
        menu(I18N["mm.edit"]   + "(_E)") {
            item(I18N["m.undo"]) {
                does { state.undo() }
                disableProperty().bind(!state.canUndoProperty())
                accelerator = KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN)
            }
            item(I18N["m.redo"]) {
                does { state.redo() }
                disableProperty().bind(!state.canRedoProperty())
                accelerator = KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)
            }
            separator()
            item(I18N["m.snr"]) {
                does { searchAndReplace() }
                disableProperty().bind(!state.openedProperty())
                accelerator = KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN)
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
        menu(I18N["mm.export"] + "(_X)") {
            item(I18N["m.lp"]) {
                does { exportTransFile(FileType.LPFile) }
                disableProperty().bind(!state.openedProperty())
                accelerator = KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN)
            }
            item(I18N["m.meo"]) {
                does { exportTransFile(FileType.MeoFile) }
                disableProperty().bind(!state.openedProperty())
                accelerator = KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)
            }
            separator()
            item(I18N["m.pack"]) {
                does { exportTransPack() }
                disableProperty().bind(!state.openedProperty())
            }
        }
        menu(I18N["mm.tools"]  + "(_T)") {
            checkItem(I18N["m.dict"]) {
                does { showDict(); isSelected = true; }
                accelerator = KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN)
                state.application.onlineDict.showingProperty().addListener(onNew(this::setSelected))
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
        menu(I18N["mm.about"]  + "(_H)") {
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
                        it.addedSubList.forEachIndexed { index, file ->
                            val item = MenuItem(file.path) does { openRecentTranslation(this) }
                            mOpenRecent.items.add(it.from + index, item)
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
        chooserNew.initialFilename = "$FILENAME_DEFAULT.$extension"

        val file = chooserNew.showSaveDialog(state.stage)?.let file@{
            val name = it.nameWithoutExtension.takeUnless(FILENAME_DEFAULT::equals) ?: it.parentFile.name
            val ext  = it.extension.takeIf(EXTENSIONS_FILE::contains) ?: extension

            return@file it.parentFile.resolve("$name.$ext")
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

        chooserFile.title = I18N["chooser.open"]
        chooserFile.selectedExtensionFilter = filterFile
        chooserFile.initialFilename = ""
        val file = chooserFile.showOpenDialog(state.stage) ?: return

        state.reset()

        state.controller.open(file)
    }
    private fun saveTranslation() {
        state.controller.save(state.translationFile, silent = true)

        showChecker()
    }
    private fun saveAsTranslation() {
        chooserFile.title = I18N["chooser.save"]
        chooserFile.selectedExtensionFilter = filterFile
        chooserFile.initialFilename = state.translationFile.name
        val file = chooserFile.showSaveDialog(state.stage) ?: return

        state.controller.save(file)

        showChecker()
    }
    private fun closeTranslation() {
        if (state.controller.stay()) return

        state.reset()
    }
    private fun bakRecovery() {
        if (state.controller.stay()) return

        chooserBackup.initialDirectory = state.getBakFolder() ?: CFileChooser.lastDirectory
        val bak = chooserBackup.showOpenDialog(state.stage) ?: return
        if (bak.parentFile?.parentFile != null) chooserFile.initialDirectory = bak.parentFile.parentFile

        val extension = if (Settings.useMeoFileAsDefault) EXTENSION_FILE_MEO else EXTENSION_FILE_LP
        val filename  = "Re.${bak.parentFile?.parentFile?.name ?: "cover"}"

        chooserFile.title = I18N["chooser.rec"]
        chooserFile.selectedExtensionFilter = filterFile
        chooserFile.initialFilename = "$filename.$extension"
        val rec = chooserFile.showSaveDialog(state.stage) ?: return

        state.reset()

        state.controller.recovery(bak, rec)
    }

    private fun searchAndReplace() {
        state.application.searchAndReplace.show()
        state.application.searchAndReplace.toFront()
    }
    private fun editComment() {
        showInputArea(state.stage, I18N["m.comment.dialog.title"], state.transFile.comment).ifPresent {
            state.doAction(object : Action {

                private val oriComment = state.transFile.comment

                override val type: ActionType = ActionType.CHANGE

                override fun commit() {
                    state.transFile.comment = it
                    state.isChanged = true
                }

                override fun revert() {
                    state.transFile.comment = oriComment
                }

            })
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
            // Update selection
            state.currentPicName = state.transFile.sortedPicNames[0]
        }
    }
    private fun addExternalPicture() {
        val files = chooserPic.showOpenMultipleDialog(state.stage) ?: return
        val picNames = state.transFile.sortedPicNames

        val conflictList = ArrayList<String>()
        for (file in files) {
            val picName = file.name
            if (picNames.contains(picName)) {
                conflictList.add(picName)
                continue
            }

            state.doAction(PictureAction(ActionType.ADD, state, picName, file))
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
        val completed = state.application.dialogSpecify.specify() ?: return
        if (!completed) showInfo(state.stage, I18N["specify.info.incomplete"])
        state.controller.requestUpdatePane()
    }

    private fun exportTransFile(type: FileType) {
        chooserExport.extensionFilters.clear()
        chooserExport.extensionFilters.add(when (type) {
            FileType.LPFile -> filterLP
            FileType.MeoFile -> filterMEO
        })

        val exportName =
            if (Settings.useExportNameTemplate) Settings.exportNameTemplate
                .replace(Settings.VARIABLE_FILENAME, state.translationFile.nameWithoutExtension)
                .replace(Settings.VARIABLE_DIRNAME, state.getFileFolder()!!.name)
                .replace(Settings.VARIABLE_PROJECT, state.transFile.projectFolder.name)
            else state.getFileFolder()!!.name
        chooserExport.initialFilename = "$exportName.${type.extension}"

        val file = chooserExport.showSaveDialog(state.stage) ?: return
        state.controller.export(file, type)
    }
    private fun exportTransPack() {
        chooserPack.initialFilename = "${state.getFileFolder()!!.name}.$EXTENSION_PACK"
        val file = chooserPack.showSaveDialog(state.stage) ?: return

        state.controller.pack(file)
    }

    private fun settings() {
        val map = state.application.dialogSettings.generateProperties()

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
            Settings.AutoCheckUpdate          -> Settings.autoCheckUpdate              = value as Boolean
            Settings.InstantTranslate         -> Settings.instantTranslate             = value as Boolean
            Settings.CheckFormatWhenSave      -> Settings.checkFormatWhenSave          = value as Boolean
            Settings.UseMeoFileAsDefault      -> Settings.useMeoFileAsDefault          = value as Boolean
            Settings.UseExportNameTemplate    -> Settings.useExportNameTemplate        = value as Boolean
            Settings.ExportNameTemplate       -> Settings.exportNameTemplate           = value as String
            else -> doNothing()
        }
    }
    private fun logs() {
        val map = state.application.dialogLogs.generateProperties()

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
            ImageView(ICON.resizeByRadius(GENERAL_ICON_RADIUS)),
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
        state.application.cheatSheet.show()
        state.application.cheatSheet.toFront()
    }
    private fun crash() {
        throw RuntimeException("Crash")
    }

    private fun cht2zh(inverse: Boolean = false) {
        val converter = if (inverse) ::convert2Traditional else ::convert2Simplified

        val task = object : LPFXTask<Unit>() {
            val DELIMITER = "#|#"
            val state = this@CMenuBar.state

            override fun call() {
                val actions = ArrayList<LabelAction>()

                val picNames = state.transFile.sortedPicNames
                val picCount = state.transFile.picCount
                for ((picIndex, picName) in picNames.withIndex()) {
                    if (isCancelled) return
                    updateProgress(1.0 * (picIndex + 1) / picCount, 1.2)

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
                    labels.mapTo(actions) { LabelAction(ActionType.CHANGE, state, picName, it, newText = iterator.next().trim()) }
                }

                state.doAction(ComplexAction.of(actions))
                updateProgress(1.0, 1.0)
            }
        }

        Dialog<ButtonType>().apply {
            initOwner(state.stage)

            // Center the button
            dialogPane = object : DialogPane() {
                override fun createButtonBar() = (super.createButtonBar() as ButtonBar).apply {
                    buttonOrder = ButtonBar.BUTTON_ORDER_NONE
                }
            }
            dialogPane.applyCss()
            dialogPane.buttonTypes.add(ButtonType.CANCEL)
            dialogPane.lookup(".container").apply {
                (this as HBox).children.add(Region().also {
                    HBox.setHgrow(it, Priority.ALWAYS)
                    ButtonBar.setButtonData(it, ButtonBar.ButtonData.BIG_GAP)
                })
            }

            val button = dialogPane.lookupButton(ButtonType.CANCEL) as Button
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
        val dict = state.application.onlineDict
        dict.x = state.stage.x - COMMON_GAP + state.stage.width - dict.width
        dict.y = state.stage.y + COMMON_GAP
        dict.show()
        dict.toFront()
    }
    private fun showChecker() {
        if (!Settings.checkFormatWhenSave) return

        val checker = state.application.formatChecker

        if (checker.check()) return
        if (!checker.isShowing) showAlert(state.stage, I18N["checker.warning"])

        checker.x = state.stage.x - COMMON_GAP + state.stage.width - checker.width
        checker.y = state.stage.y + COMMON_GAP + state.application.onlineDict.height
        checker.show()
        checker.toFront()
    }

}
