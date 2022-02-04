package ink.meodinger.lpfx.component.singleton

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.component.common.CRollerLabel
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.dialog.showConfirm
import ink.meodinger.lpfx.util.file.existsOrNull
import ink.meodinger.lpfx.util.property.minus
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get

import javafx.beans.binding.ObjectBinding
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.util.Duration
import java.io.File
import java.nio.file.Files
import java.util.stream.Collectors
import kotlin.io.path.*


/**
 * Author: Meodinger
 * Date: 2021/11/22
 * Have fun with my code!
 */

object ADialogSpecify : Dialog<List<File>>() {

    private val contentGridPane = GridPane()
    private const val rowShift = 1

    private val thisWindow = dialogPane.scene.window
    private val fileChooser = FileChooser().apply {
        extensionFilters.add(FileChooser.ExtensionFilter(
            I18N["filetype.pictures"],
            List(EXTENSIONS_PIC.size) { index -> "*.${EXTENSIONS_PIC[index]}" }
        ))
    }
    private val dirChooser = DirectoryChooser()

    private var workingTransFile: TransFile = TransFile.DEFAULT_TRANSFILE
    private var projectFolder: File = DEFAULT_FILE
    private var picCount: Int = 0
    private var picNames: List<String> = ArrayList()
    private var files: MutableList<File> = ArrayList()
    private var labels: MutableList<CRollerLabel> = ArrayList()

    private val unspecified = I18N["specify.unspecified"]

    init {
        initOwner(State.stage)

        title = I18N["specify.title"]
        dialogPane.prefWidth = PANE_WIDTH
        dialogPane.prefHeight = PANE_HEIGHT
        dialogPane.buttonTypes.addAll(ButtonType.APPLY, ButtonType.CANCEL)
        withContent(BorderPane()) {
            val stackPane = StackPane(contentGridPane.apply {
                hgap = COMMON_GAP
                vgap = COMMON_GAP
                padding = Insets(COMMON_GAP)
                alignment = Pos.TOP_CENTER
            })
            val scrollPane = ScrollPane(stackPane)
            stackPane.prefWidthProperty().bind(scrollPane.widthProperty() - COMMON_GAP)

            center(scrollPane) { style = "-fx-background-color:transparent;" }
            bottom(HBox()) {
                alignment = Pos.CENTER_RIGHT
                padding = Insets(COMMON_GAP, COMMON_GAP / 2, COMMON_GAP / 2, COMMON_GAP)

                add(Button(I18N["specify.dialog.choose_folder"])) {
                    does {
                        // need show confirm?
                        var show = false
                        for (label in labels) if (label.text != unspecified) {
                            show = true
                            break
                        }

                        // preserve already-set path?
                        var preserve = false
                        if (show) {
                            val confirmPre = showConfirm(thisWindow, I18N["specify.confirm.preserve"])
                            preserve = confirmPre.isPresent && confirmPre.get() == ButtonType.YES
                        }

                        // get project folder
                        val directory = dirChooser.showDialog(thisWindow) ?: return@does
                        if (!preserve) State.projectFolder = directory

                        // auto-fill
                        val newPicPaths = Files.walk(directory.toPath(), 1)
                            .filter { path -> EXTENSIONS_PIC.contains(path.extension.lowercase()) }
                            .collect(Collectors.toList())
                        for (i in 0 until picCount) {
                            if (preserve && files[i] !== DEFAULT_FILE) continue

                            val lastIndex = newPicPaths.size - 1
                            for (j in newPicPaths.indices) {
                                val oldPicFile = workingTransFile.getFile(picNames[i])
                                // check full filename & simple filename
                                val fit =
                                    newPicPaths[j].name == oldPicFile.name || newPicPaths[j].nameWithoutExtension == oldPicFile.nameWithoutExtension
                                if (fit) {
                                    labels[i].text = newPicPaths[j].pathString
                                    files[i] = newPicPaths[j].toFile()

                                    // swap
                                    val temp = newPicPaths.last()
                                    newPicPaths[lastIndex] = newPicPaths[j]
                                    newPicPaths[j] = temp
                                }
                            }
                        }
                    }
                }
            }
        }

        setResultConverter {
            when (it) {
                ButtonType.APPLY -> files
                else -> emptyList()
            }
        }
    }

    fun specify(): List<File> {
        // clear & re-init
        contentGridPane.children.clear()
        contentGridPane.add(Label(I18N["specify.dialog.pic_name"]), 0, 0)
        contentGridPane.add(Label(I18N["specify.dialog.pic_path"]), 1, 0)

        // update variables
        workingTransFile = State.transFile
        projectFolder = State.projectFolder
        picCount = State.transFile.picCount
        picNames = State.transFile.sortedPicNames

        // prepare
        fileChooser.initialDirectory = projectFolder
        dirChooser.initialDirectory = projectFolder

        files = MutableList(picCount) { workingTransFile.getFile(picNames[it]).existsOrNull() ?: DEFAULT_FILE }
        labels = MutableList(picCount) { CRollerLabel().apply {
            prefWidth = 300.0
            text = if (files[it] !== DEFAULT_FILE) files[it].path else unspecified
            tooltipProperty().bind(object : ObjectBinding<Tooltip>() {
                init { bind(this@apply.textProperty()) }
                override fun computeValue(): Tooltip = Tooltip(this@apply.text).apply { showDelay = Duration(0.0) }
            })
            textFillProperty().bind(object : ObjectBinding<Color>() {
                init { bind(this@apply.textProperty()) }
                override fun computeValue(): Color = if (this@apply.text == unspecified) Color.RED else Color.BLACK
            })
        } }

        // add
        for (i in 0 until picCount) {
            val button = Button(I18N["specify.dialog.choose_file"]) does {
                // Manually specify pic file
                val picFile = fileChooser.showOpenDialog(thisWindow) ?: return@does
                labels[i].text = picFile.path
                files[i] = picFile
            }

            contentGridPane.add(Label(picNames[i]), 0, i + rowShift)
            contentGridPane.add(labels[i], 1, i + rowShift)
            contentGridPane.add(button, 2, i + rowShift)
        }

        // manually control label roll to save resource
        for (label in labels) label.startRoll()
        val result = showAndWait()
        for (label in labels) label.stopRoll()

        return if (result.isPresent) result.get() else emptyList()
    }

}
