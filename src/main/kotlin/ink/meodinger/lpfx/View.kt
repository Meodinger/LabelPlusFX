package ink.meodinger.lpfx

import ink.meodinger.lpfx.action.*
import ink.meodinger.lpfx.component.*
import ink.meodinger.lpfx.component.common.*
import ink.meodinger.lpfx.component.dialog.*
import ink.meodinger.lpfx.options.*
import ink.meodinger.lpfx.type.LPFXTask
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.util.collection.contact
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.doNothing
import ink.meodinger.lpfx.util.image.resizeByRadius
import ink.meodinger.lpfx.util.property.*
import ink.meodinger.lpfx.util.string.deleteTrailing
import ink.meodinger.lpfx.util.string.emptyString
import ink.meodinger.lpfx.util.string.sortByDigit
import ink.meodinger.lpfx.util.translator.convert2Simplified
import ink.meodinger.lpfx.util.translator.convert2Traditional

import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.beans.value.ChangeListener
import javafx.collections.ListChangeListener
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.stage.FileChooser
import javafx.util.Callback
import javafx.util.Duration
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.io.path.extension
import kotlin.io.path.name


/**
 * Author: Meodinger
 * Date: 2021/11/26
 * Have fun with my code!
 */

/**
 * Main View
 */
class View(private val state: State) : BorderPane() {

    companion object {
        // Scale
        private const val SCALE_MIN : Double = 0.1
        private const val SCALE_MAX : Double = 4.0
        private const val SCALE_INIT: Double = 0.8
    }

    // region Components

    /**
     * Work mode switch button
     */
    val bSwitchWorkMode: Button = Button()

    /**
     * View mode switch button
     */
    val bSwitchViewMode: Button = Button()

    /**
     * StatsBar label: location
     */
    val lLocation: Label = Label()

    /**
     * StatsBar label: backup information
     */
    val lBackup: Label = Label()

    /**
     * StatsBar label: accumulate editing time
     */
    val lAccEditTime: Label = Label()

    /**
     * Picture ComboBox, change pictures
     */
    val cPicBox: CComboBox<String> = CComboBox()

    /**
     * Group ComboBox, change groups
     */
    val cGroupBox: CComboBox<TransGroup> = CComboBox()

    /**
     * GroupBar, display TransGroups above the LabelPane
     */
    val cGroupBar: CGroupBar = CGroupBar()

    /**
     * LabelPane, display Image & Labels
     */
    val cLabelPane: CLabelPane = CLabelPane()

    /**
     * TreeView, display labels by label-index or by groupId
     */
    val cTreeView: CTreeView = CTreeView()

    /**
     * TransArea, edit label's text
     */
    val cTransArea: CLigatureArea = CLigatureArea()


    // Private Components
    private val statsBar: HBox = HBox()
    private val cTreeMenu: CTreeMenu = CTreeMenu(state, cTreeView)
    private val cTextMenu: CTextMenu = CTextMenu(cTransArea)

    // endregion

    // region Chooser & Filter

    private val chooserPic    = CFileChooser()
    private val chooserNew    = CFileChooser()
    private val chooserFile   = CFileChooser()
    private val chooserPack   = CFileChooser()
    private val chooserBackup = FileChooser()

    private val filterAny     = FileChooser.ExtensionFilter(I18N["file_type.any"], "*.*")
    private val filterPic     = FileChooser.ExtensionFilter(I18N["file_type.pictures"], List(EXTENSIONS_PIC.size) { index -> "*.${EXTENSIONS_PIC[index]}" })
    private val filterBMP     = FileChooser.ExtensionFilter(I18N["file_type.picture_bmp"], "*.${EXTENSION_PIC_BMP}")
    private val filterGIF     = FileChooser.ExtensionFilter(I18N["file_type.picture_gif"], "*.${EXTENSION_PIC_GIF}")
    private val filterPNG     = FileChooser.ExtensionFilter(I18N["file_type.picture_png"], "*.${EXTENSION_PIC_PNG}")
    private val filterJPEG    = FileChooser.ExtensionFilter(I18N["file_type.picture_jpeg"], "*.${EXTENSION_PIC_JPG}", "*.${EXTENSION_PIC_JPEG}")
    private val filterTIFF    = FileChooser.ExtensionFilter(I18N["file_type.picture_tiff"], "*.${EXTENSION_PIC_TIF}", "*.${EXTENSION_PIC_TIFF}")
    private val filterWEBP    = FileChooser.ExtensionFilter(I18N["file_type.picture_webp"], "*.${EXTENSION_PIC_WEBP}")
    private val filterFile    = FileChooser.ExtensionFilter(I18N["file_type.translation"],  List(EXTENSIONS_FILE.size) { index -> "*.${EXTENSIONS_FILE[index]}" })
    private val filterLP      = FileChooser.ExtensionFilter(I18N["file_type.translation_lp"], "*.${EXTENSION_FILE_LP}")
    private val filterMEO     = FileChooser.ExtensionFilter(I18N["file_type.translation_meo"], "*.${EXTENSION_FILE_MEO}")
    private val filterBak     = FileChooser.ExtensionFilter(I18N["file_type.backup"], "*.${EXTENSION_BAK}")
    private val filterPack    = FileChooser.ExtensionFilter(I18N["file_type.pack"], "*.${EXTENSION_PACK}")

    // endregion

    init {
        state.view = this

        chooserPic.title = I18N["m.externalPic.chooser.title"]
        chooserPic.extensionFilters.addAll(filterPic, filterPNG, filterJPEG, filterGIF, filterBMP, filterTIFF, filterWEBP)

        chooserNew.title = I18N["chooser.new"]
        chooserNew.extensionFilters.addAll(filterAny, filterFile, filterMEO, filterLP)

        // fileChooser's tile will change
        chooserFile.extensionFilters.addAll(filterFile, filterMEO, filterLP)

        chooserBackup.title = I18N["chooser.bak"]
        chooserBackup.extensionFilters.addAll(filterBak, filterAny)

        chooserPack.title = I18N["chooser.pack"]
        chooserPack.extensionFilters.add(filterPack)

        top(MenuBar()) {
            menu(I18N["mm.file"]) {
                item(I18N["m.new"]) {
                    does { newTranslation() }
                    accelerator = KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN)
                }
                item(I18N["m.open"]) {
                    does { openTranslation() }
                    accelerator = KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN)
                }
                menu(I18N["m.recent"]) {
                    disableProperty().bind(items.emptyProperty())

                    RecentFiles.recentFilesProperty().addListener(ListChangeListener {
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
                                        items.removeIf { item -> item.text == file.path }
                                    }
                                }
                                if (it.wasAdded()) {
                                    it.addedSubList.forEachIndexed { index, file ->
                                        items.add(it.from + index, MenuItem(file.path) does { openRecentTranslation(this) })
                                    }
                                }
                            }
                        }
                    })
                    RecentFiles.recentFiles.forEach {
                        items.add(MenuItem(it.path) does { openRecentTranslation(this) })
                    }
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
                item(I18N["m.settings"]) {
                    does { settings() }
                }
                separator()
                item(I18N["m.exit"]) {
                    does { exitApplication() }
                }
            }
            menu(I18N["mm.edit"]) {
                item(I18N["m.undo"]) {
                    does { state.undo() }
                    disableProperty().bind(!state.undoableProperty())
                    accelerator = KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN)
                }
                item(I18N["m.redo"]) {
                    does { state.redo() }
                    disableProperty().bind(!state.redoableProperty())
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
            menu(I18N["mm.export"]) {
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
            menu(I18N["mm.tools"]) {
                checkItem(I18N["m.dict"]) {
                    does { showDict(); isSelected = true; }
                    accelerator = KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN)

                    // Use binding will make item not operatable
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
            menu(I18N["mm.about"]) {
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
        }
        center(SplitPane()) {
            orientation = Orientation.HORIZONTAL

            add(BorderPane()) {
                top(cGroupBar) {
                    setOnGroupCreate { cTreeMenu.triggerGroupCreate() }
                }
                center(cLabelPane) {
                    initScale = SCALE_INIT
                    minScale = SCALE_MIN
                    maxScale = SCALE_MAX
                    disableProperty().bind(!state.openedProperty())
                    labelRadiusProperty().bind(Settings.labelRadiusProperty())
                    labelColorOpacityProperty().bind(Settings.labelColorOpacityProperty())
                    labelTextOpaqueProperty().bind(Settings.labelTextOpaqueProperty())
                    newPictureScaleProperty().bind(Settings.newPictureScaleProperty())
                    useWheelToScaleProperty().bind(Settings.useWheelToScaleProperty())
                }
                bottom(HBox()) {
                    add(CTextSlider()) {
                        disableProperty().bind(cLabelPane.disableProperty())
                        initScaleProperty().bind(cLabelPane.initScaleProperty())
                        scaleProperty().bindBidirectional(cLabelPane.scaleProperty())
                        minScaleProperty().bindBidirectional(cLabelPane.minScaleProperty())
                        maxScaleProperty().bindBidirectional(cLabelPane.maxScaleProperty())
                    }
                    add(HBox()) {
                        hgrow = Priority.ALWAYS
                    }
                    add(cPicBox) {
                        prefWidth = 200.0
                        isWrapped = true
                        disableProperty().bind(!state.openedProperty())
                    }
                }
            }
            add(SplitPane()) {
                orientation = Orientation.VERTICAL

                add(BorderPane()) {
                    top(HBox()) {
                        add(bSwitchWorkMode) {
                            isMnemonicParsing = false
                            disableProperty().bind(!state.openedProperty())
                            textProperty().bind(state.workModeProperty().asString())
                            does { switchWorkMode() }
                        }
                        add(cGroupBox) {
                            prefWidth = 160.0
                            disableProperty().bind(!state.openedProperty())

                            innerBox.buttonCell = object : ListCell<TransGroup>() {
                                override fun updateItem(item: TransGroup?, empty: Boolean) {
                                    super.updateItem(item, empty)
                                    text = if (empty) null else item?.name
                                }
                            }
                            innerBox.cellFactory = Callback {
                                object : ListCell<TransGroup>() {
                                    override fun updateItem(item: TransGroup?, empty: Boolean) {
                                        super.updateItem(item, empty)

                                        if (empty || item == null) {
                                            text = null
                                            graphic = null
                                        } else {
                                            text = item.name
                                            graphic = Circle(8.0, item.color)
                                        }
                                    }
                                }
                            }
                        }
                        add(HBox()) {
                            hgrow = Priority.ALWAYS
                        }
                        add(bSwitchViewMode) {
                            isMnemonicParsing = false
                            disableProperty().bind(!state.openedProperty())
                            textProperty().bind(state.viewModeProperty().asString())
                            does { switchViewMode() }
                        }
                    }
                    center(cTreeView) {
                        contextMenu = cTreeMenu
                        disableProperty().bind(!state.openedProperty())
                        setCellFactory { object : TreeCell<String>() {

                            private var markListener: ChangeListener<Boolean> = onNew {
                                textFill = if (it) Color.RED else Color.BLACK
                            }

                            init {
                                treeItemProperty().addListener { _, oldV, newV ->
                                    if (oldV is CTreeLabelItem) oldV.transLabel.markedProperty().removeListener(markListener)
                                    if (newV is CTreeLabelItem) newV.transLabel.markedProperty().addListener(markListener)
                                }
                            }

                            override fun updateItem(item: String?, empty: Boolean) {
                                super.updateItem(item, empty)
                                textFill = Color.BLACK

                                val actualItem = treeItem
                                if (item != null && !empty) {
                                    text = item
                                    graphic = actualItem.graphic
                                    if (actualItem is CTreeLabelItem && actualItem.transLabel.isMarked) {
                                        textFill = Color.RED
                                    }
                                } else {
                                    text = emptyString()
                                    graphic = null
                                }
                            }
                        } }
                    }
                }
                add(TitledPane()) {
                    // The max-height of TitledPane is the same as pref-height by default.
                    // If we set it to Double.MAX_VALUE, the divider position will not change
                    // when expand/collapse. If we only let it be MAX when it is expanded,
                    // the divider position will have a jump when collapse if actual width is
                    // bigger than pref-width. So we set it to MAX and manually control the position.
                    maxHeight = Double.MAX_VALUE // Make TitledPane cannot change divider position

                    // Manually control the divider animation
                    var lastDividerPosition = 0.0
                    expandedProperty().addListener(onNew {
                        if (it) {
                            // @see javafx.scene.control.skin.TitledPaneSkin.doAnimationTransition
                            val keyValue = KeyValue(dividers[0].positionProperty(), lastDividerPosition, Interpolator.LINEAR)
                            // @see javafx.scene.control.skin.TitledPaneSkin.TRANSITION_DURATION
                            val timeline = Timeline(KeyFrame(Duration.millis(350.0), keyValue))
                            // Start animation
                            timeline.play()
                        } else {
                            lastDividerPosition = dividerPositions[0]

                            val keyValue = KeyValue(dividers[0].positionProperty(), 1.0, Interpolator.LINEAR)
                            val timeline = Timeline(KeyFrame(Duration.millis(350.0), keyValue))
                            timeline.play()
                        }
                    })

                    textProperty().bind(state.currentLabelIndexProperty().transform {
                        if (it == NOT_FOUND) emptyString() else it.toString()
                    })
                    withContent(cTransArea) {
                        isWrapText = true
                        disableProperty().bind(!state.openedProperty())
                        ligatureRulesProperty().bind(Settings.ligatureRulesProperty())
                        fontProperty().bindBidirectional(Preference.textAreaFontProperty())
                    }
                }
                // Set divider after added children, or there were no dividers
                dividers[0].positionProperty().bindBidirectional(Preference.rightDividerPositionProperty())
            }
            // Set divider after added children, or there were no dividers
            dividers[0].positionProperty().bindBidirectional(Preference.mainDividerPositionProperty())
        }
        bottom(statsBar) {
            val generalPadding = Insets(4.0, 8.0, 4.0, 8.0)
            add(HBox()) {
                hgrow = Priority.ALWAYS
            }
            add(Separator()) {
                orientation = Orientation.VERTICAL
            }
            add(lLocation) {
                text = "-- : --"
                padding = generalPadding
                prefWidth = 120.0
            }
            add(Separator()) {
                orientation = Orientation.VERTICAL
            }
            add(lBackup) {
                text = I18N["stats.not_backed"]
                padding = generalPadding
                prefWidth = 150.0
            }
            add(Separator()) {
                orientation = Orientation.VERTICAL
            }
            add(lAccEditTime) {
                text = String.format(I18N["stats.accumulator.s"], "--:--:--")
                padding = generalPadding
                prefWidth = 180.0
            }
            add(CResizeGrid()) {
                prefHeightProperty().bind(statsBar.heightProperty())
                prefWidthProperty().bind(statsBar.heightProperty())
            }
        }

        if (!Preference.isShowStatsBar) children.remove(statsBar)
        Preference.showStatsBarProperty().addListener(onNew {
            if (it) bottom(statsBar) else children.remove(statsBar)
        })
    }

    private fun switchViewMode() {
        state.viewMode = ViewMode.values()[(state.viewMode.ordinal + 1) % ViewMode.values().size]
    }
    private fun switchWorkMode() {
        state.workMode = WorkMode.values()[(state.workMode.ordinal + 1) % WorkMode.values().size]
        state.viewMode = Settings.viewModes[state.workMode.ordinal]
    }

    private fun openRecentTranslation(item: MenuItem) {
        // Open recent, remove item if not exist

        if (state.controller.stay()) return

        val file = File(item.text)
        if (!file.exists()) {
            showError(state.stage, String.format(I18N["error.file_not_exist.s"], file.path))
            RecentFiles.remove(file)
            item.parentMenu.items.remove(item)
            return
        }

        state.reset()

        state.controller.open(file, file.parentFile)
    }

    private fun newTranslation() {
        if (state.controller.stay()) return

        val extension = if (Settings.useMeoFileAsDefault) EXTENSION_FILE_MEO else EXTENSION_FILE_LP

        chooserNew.initialFilename = "$FILENAME_DEFAULT.$extension"
        chooserNew.selectedExtensionFilter = filterAny

        var differentWithInput = false
        val file = chooserNew.showSaveDialog(state.stage)?.let file@{
            val filename  = it.nameWithoutExtension.takeUnless(FILENAME_DEFAULT::equals) ?: it.parentFile.name
            val extexsion = it.extension.lowercase().takeIf(EXTENSIONS_FILE::contains) ?: extension

            return@file it.parentFile.resolve("$filename.$extexsion").also { final -> differentWithInput = it.name != final.name }
        } ?: return
        if (differentWithInput && file.exists()) {
            val confirm = showConfirm(state.stage, String.format(I18N["m.new.dialog.overwrite.s"], file.name))
            if (!(confirm.isPresent && confirm.get() == ButtonType.YES)) return
        }

        state.reset()

        val projectFolder = state.controller.new(file)
        if (projectFolder != null) state.controller.open(file, projectFolder)
    }
    private fun openTranslation() {
        if (state.controller.stay()) return

        chooserFile.title = I18N["chooser.open"]
        chooserFile.initialFilename = ""
        chooserFile.selectedExtensionFilter = filterFile
        val file = chooserFile.showOpenDialog(state.stage) ?: return

        state.reset()

        state.controller.open(file, file.parentFile)
    }
    private fun saveTranslation() {
        state.controller.save(state.translationFile, true)

        showChecker()
    }
    private fun saveAsTranslation() {
        chooserFile.title = I18N["chooser.save"]
        chooserFile.initialFilename = state.translationFile.name
        chooserFile.selectedExtensionFilter = filterFile

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

        chooserBackup.initialDirectory = if (state.isOpened) state.getBakFolder() else CFileChooser.lastDirectory
        chooserFile.initialDirectory = if (state.isOpened) state.getFileFolder() else CFileChooser.lastDirectory
        val bak = chooserBackup.showOpenDialog(state.stage) ?: return

        val filename  = "Re.${bak.parentFile?.parentFile?.name ?: "cover"}"
        val extension = if (Settings.useMeoFileAsDefault) EXTENSION_FILE_MEO else EXTENSION_FILE_LP
        chooserFile.title = I18N["chooser.rec"]
        chooserFile.initialFilename = "$filename.$extension"
        chooserFile.selectedExtensionFilter = if (Settings.useMeoFileAsDefault) filterMEO else filterLP

        val rec = chooserFile.showSaveDialog(state.stage) ?: return
        state.reset()
        state.controller.recovery(bak, rec)
    }
    private fun exitApplication() {
        if (state.controller.stay()) return

        state.application.stop()
    }

    private fun searchAndReplace() {
        state.application.searchAndReplace.show()
        state.application.searchAndReplace.toFront()
    }
    private fun editComment() {
        val result = showInputArea(state.stage, I18N["m.comment.dialog.title"], state.transFile.comment)
        if (!result.isPresent) return

        state.doAction(object : Action {

            private val oriComment = state.transFile.comment
            private val dstComment = result.get()

            override val type: ActionType = ActionType.CHANGE

            override fun commit() {
                state.transFile.comment = dstComment
                state.isChanged = true
            }

            override fun revert() {
                state.transFile.comment = oriComment
                state.isChanged = true
            }

        })
    }
    private fun editProjectPictures() {
        // Choose Pics
        val selected = state.transFile.sortedPicNames
        val unselected = Files.walk(state.transFile.projectFolder.toPath(), 1)
            .filter {
                if (it.name in selected) return@filter false
                for (extension in EXTENSIONS_PIC) if (it.extension == extension) return@filter true
                false
            }.map(Path::name).collect(Collectors.toList())

        val result = showChoiceList(state.stage, unselected.sortByDigit(), selected.sortByDigit())
        if (!result.isPresent) return

        if (result.get().isEmpty()) {
            showInfo(state.stage, I18N["info.required_at_least_1_pic"])
            return
        }

        val picNames = state.transFile.sortedPicNames
        val resNames = result.get()

        val toAdd = ArrayList<String>()
        for (picName in resNames) if (picName !in picNames) toAdd.add(picName)
        val toRemove = ArrayList<String>()
        for (picName in picNames) if (picName !in resNames) toRemove.add(picName)

        if (toAdd.size == 0 && toRemove.size == 0) return
        if (toRemove.size != 0) {
            val confirm = showConfirm(state.stage, I18N["confirm.removing_pic"])
            if (!(confirm.isPresent && confirm.get() == ButtonType.YES)) return
        }

        state.doAction(ComplexAction(contact(
            toAdd.map { picName -> PictureAction(ActionType.ADD, state, picName) },
            toRemove.map { picName -> PictureAction(ActionType.REMOVE, state, picName) }
        )))
        // Update selection
        state.currentPicName = state.transFile.sortedPicNames[0]
    }
    private fun addExternalPicture() {
        val files = chooserPic.showOpenMultipleDialog(state.stage) ?: return
        val picNames = state.transFile.sortedPicNames

        val actionList = ArrayList<Action>()
        val conflictList = ArrayList<String>()
        for (file in files) {
            if (file.name in picNames) {
                conflictList.add(file.path)
                continue
            }
            actionList.add(PictureAction(ActionType.ADD, state, file.name, file))
        }

        state.doAction(ComplexAction(actionList))
        // Show conficts
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
        val exportName =
            if (Settings.useExportNameTemplate) Settings.exportNameTemplate
                .replace(Settings.VARIABLE_FILENAME, state.translationFile.nameWithoutExtension)
                .replace(Settings.VARIABLE_DIRNAME, state.getFileFolder().name)
                .replace(Settings.VARIABLE_PROJECT, state.transFile.projectFolder.name)
            else state.getFileFolder().name

        chooserFile.title = I18N["chooser.export"]
        chooserFile.initialFilename = "$exportName.${type.extension}"
        chooserFile.selectedExtensionFilter = if (type == FileType.MeoFile) filterMEO else filterLP

        val file = chooserFile.showSaveDialog(state.stage) ?: return
        state.controller.export(file)
    }
    private fun exportTransPack() {
        chooserPack.initialFilename = "${state.getFileFolder().name}.$EXTENSION_PACK"

        val file = chooserPack.showSaveDialog(state.stage) ?: return
        state.controller.pack(file)
    }

    private fun settings() {
        val map = state.application.dialogSettings.generateProperties()

        Logger.info("Generated common settings", "MenuBar")
        Logger.debug("got $map", "MenuBar")

        @Suppress("UNCHECKED_CAST")
        for ((key, value) in map) when (key) {
            Settings.DefaultGroupNameList     -> Settings.defaultGroupNameList        .setAll(value as List<String>)
            Settings.DefaultGroupColorHexList -> Settings.defaultGroupColorHexList    .setAll(value as List<String>)
            Settings.IsGroupCreateOnNewTrans  -> Settings.isGroupCreateOnNewTransList .setAll(value as List<Boolean>)
            Settings.LigatureRules            -> Settings.ligatureRules               .setAll(value as List<Pair<String, String>>)
            Settings.QuickInputTexts          -> Settings.quickInputTexts             .setAll(value as List<String>)
            Settings.ViewModes                -> Settings.viewModes                   .setAll(value as List<ViewMode>)
            Settings.NewPictureScale          -> Settings.newPictureScalePicture      = value as CLabelPane.NewPictureScale
            Settings.UseWheelToScale          -> Settings.useWheelToScale             = value as Boolean
            Settings.LabelRadius              -> Settings.labelRadius                 = value as Double
            Settings.LabelColorOpacity        -> Settings.labelColorOpacity           = value as Double
            Settings.LabelTextOpaque          -> Settings.labelTextOpaque             = value as Boolean
            Settings.AutoCheckUpdate          -> Settings.autoCheckUpdate             = value as Boolean
            Settings.AutoOpenLastFile         -> Settings.autoOpenLastFile            = value as Boolean
            Settings.InstantTranslate         -> Settings.instantTranslate            = value as Boolean
            Settings.CheckFormatWhenSave      -> Settings.checkFormatWhenSave         = value as Boolean
            Settings.UseMeoFileAsDefault      -> Settings.useMeoFileAsDefault         = value as Boolean
            Settings.UseExportNameTemplate    -> Settings.useExportNameTemplate       = value as Boolean
            Settings.ExportNameTemplate       -> Settings.exportNameTemplate          = value as String
            Settings.UseCustomBaiduKey        -> Settings.useCustomBaiduKey           = value as Boolean
            Settings.BaiduTransLateKey        -> Settings.baiduTransLateKey           = value as String
            Settings.BaiduTransLateAppId      -> Settings.baiduTransLateAppId         = value as String
            else -> doNothing()
        }
    }
    private fun logs() {
        val map = state.application.dialogLogs.generateProperties()

        Logger.info("Generated logs settings", "MenuBar")
        Logger.debug("got $map", "MenuBar")

        @Suppress("UNCHECKED_CAST")
        for ((key, value) in map) when (key) {
            Settings.LogLevel -> Settings.logLevel = value as Logger.LogLevel
            else -> doNothing()
        }

        Logger.level = Settings.logLevel
    }
    private fun about() {
        showLink(
            state.stage,
            ImageView(ICON.resizeByRadius(GENERAL_ICON_RADIUS)),
            I18N["common.about"],
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
            val state = this@View.state

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
                    val iterator = converter(builder.deleteTrailing(DELIMITER).toString()).split(DELIMITER).also {
                        if (it.size != labelCount) {
                            updateMessage("at [$picName] ${it.joinToString()}")
                            return
                        }
                    }.iterator()
                    // Sometimes will get whitespaces at label start
                    labels.mapTo(actions) { LabelAction(ActionType.CHANGE, state, picName, it, newText = iterator.next().trim()) }
                }

                state.doAction(ComplexAction(actions))
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
            dialogPane.buttonTypes.add(ButtonType.CANCEL)
            dialogPane.applyCss()
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
        dict.x = state.stage.x - 16.0 + state.stage.width - dict.width
        dict.y = state.stage.y + 16.0
        dict.show()
        dict.toFront()
    }
    private fun showChecker() {
        if (!Settings.checkFormatWhenSave) return

        val checker = state.application.formatChecker

        if (checker.check()) return
        if (!checker.isShowing) showWarning(state.stage, I18N["checker.warning"])

        checker.x = state.stage.x - 16.0 + state.stage.width - checker.width
        checker.y = state.stage.y + 16.0 + state.application.onlineDict.height
        checker.show()
        checker.toFront()
    }

}
