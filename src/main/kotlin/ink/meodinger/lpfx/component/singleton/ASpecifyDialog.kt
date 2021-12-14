package ink.meodinger.lpfx.component.singleton

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.component.common.CRollerLabel
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.util.component.add
import ink.meodinger.lpfx.util.component.bottom
import ink.meodinger.lpfx.util.component.center
import ink.meodinger.lpfx.util.component.does
import ink.meodinger.lpfx.util.dialog.showConfirm
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
import kotlin.collections.ArrayList
import kotlin.io.path.*


/**
 * Author: Meodinger
 * Date: 2021/11/22
 * Have fun with my code!
 */

object ASpecifyDialog : Dialog<List<File>>() {

    private val unspecified = I18N["specify.unspecified"]
    private val defaultFile = File("")

    private val contentPane = BorderPane()
    private val contentGridPane = GridPane().also {
        it.hgap = COMMON_GAP
        it.vgap = COMMON_GAP
        it.padding = Insets(COMMON_GAP)
        it.alignment = Pos.TOP_CENTER
    }
    private val contentStackPane = StackPane(contentGridPane)
    private val contentScrollPane = ScrollPane(contentStackPane)

    private val thisWindow = dialogPane.scene.window
    private val fileChooser = FileChooser().also {
        val extensions = List(EXTENSIONS_PIC.size) { index -> "*.${EXTENSIONS_PIC[index]}" }
        val fileFilter = FileChooser.ExtensionFilter(I18N["filetype.pictures"], extensions)
        it.extensionFilters.add(fileFilter)
    }
    private val dirChooser = DirectoryChooser()

    private var workingTransFile: TransFile = TransFile.DEFAULT_FILE
    private var projectFolder: File = defaultFile
    private var picCount: Int = 0
    private var picNames: List<String> = ArrayList()
    private var files: MutableList<File> = ArrayList()
    private var labels: MutableList<CRollerLabel> = ArrayList()

    init {
        initOwner(State.stage)

        contentStackPane.prefWidthProperty().bind(contentScrollPane.widthProperty() - COMMON_GAP)
        contentPane.apply {
            center(contentScrollPane) { style = "-fx-background-color:transparent;" }
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
                            val confirmPre = showConfirm(I18N["specify.confirm.preserve"], thisWindow)
                            preserve = confirmPre.isPresent && confirmPre.get() == ButtonType.YES
                        }

                        // get project folder
                        val directory = dirChooser.showDialog(thisWindow) ?: return@does
                        if (!preserve) State.projectFolder = directory

                        // auto-fill
                        val newPicPaths = Files
                            .walk(directory.toPath(),1)
                            .filter { path -> EXTENSIONS_PIC.contains(path.extension.lowercase()) }
                            .collect(Collectors.toList())
                        for (i in 0 until picCount) {
                            if (preserve && files[i] != defaultFile) continue
                            for (j in newPicPaths.indices) {
                                val oldPicFile = workingTransFile.getFile(picNames[i])
                                // check full filename & simple filename
                                if (newPicPaths[j].name == oldPicFile.name ||
                                    newPicPaths[j].nameWithoutExtension == oldPicFile.nameWithoutExtension
                                ) {
                                    labels[i].text = newPicPaths[j].pathString
                                    files[i] = newPicPaths[j].toFile()

                                    // swap
                                    val temp = newPicPaths.last()
                                    newPicPaths[newPicPaths.size - 1] = newPicPaths[j]
                                    newPicPaths[j] = temp
                                }
                            }
                        }
                    }
                }
            }
        }

        title = I18N["specify.title"]
        dialogPane.prefWidth = DIALOG_WIDTH
        dialogPane.prefHeight = DIALOG_HEIGHT
        dialogPane.content = contentPane
        dialogPane.buttonTypes.addAll(ButtonType.APPLY, ButtonType.CANCEL)

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

        files = MutableList(picCount) {
            val file = workingTransFile.getFile(picNames[it])
            if (file.exists()) file else defaultFile
        }
        labels = MutableList(picCount) { CRollerLabel().also { label ->
            label.prefWidth = 300.0
            label.tooltipProperty().bind(object : ObjectBinding<Tooltip>() {
                init { bind(label.textProperty()) }
                override fun computeValue(): Tooltip = Tooltip(label.text).also { it.showDelay = Duration(0.0) }
            })
            label.textFillProperty().bind(object : ObjectBinding<Color>() {
                init { bind(label.textProperty()) }
                override fun computeValue(): Color = if (label.text == unspecified) Color.RED else Color.BLACK
            })
            if (files[it] != defaultFile) label.text = files[it].path else label.text = unspecified
        } }

        // add

        for (i in 0 until picCount) {
            val button = Button(I18N["specify.dialog.choose_file"]) does {
                // Manually specify pic file
                val picFile = fileChooser.showOpenDialog(thisWindow) ?: return@does
                labels[i].text = picFile.path
                files[i] = picFile
            }

            contentGridPane.add(Label(picNames[i]), 0, i + 1)
            contentGridPane.add(labels[i], 1, i + 1)
            contentGridPane.add(button, 2, i + 1)
        }

        // manually control label roll to save resource
        for (label in labels) label.startRoll()
        val result = showAndWait()
        for (label in labels) label.stopRoll()

        return if (result.isPresent) result.get() else emptyList()
    }

}
