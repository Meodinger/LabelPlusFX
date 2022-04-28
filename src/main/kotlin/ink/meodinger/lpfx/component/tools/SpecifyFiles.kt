package ink.meodinger.lpfx.component.tools

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.component.common.CRollerLabel
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.dialog.showConfirm
import ink.meodinger.lpfx.util.file.exists
import ink.meodinger.lpfx.util.property.minus

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

class SpecifyFiles(private val state: State) : Dialog<List<File?>>() {

    companion object {
        private const val RowShift = 1
        private val Unspecified = I18N["specify.unspecified"]
    }

    private val thisWindow = dialogPane.scene.window
    private val contentGridPane = GridPane().apply {
        hgap = COMMON_GAP
        vgap = COMMON_GAP
        padding = Insets(COMMON_GAP)
        alignment = Pos.CENTER
    }
    private val fileChooser = FileChooser().apply {
        extensionFilters.addAll(
            FileChooser.ExtensionFilter(I18N["file_type.pictures"], List(EXTENSIONS_PIC.size) { index -> "*.${EXTENSIONS_PIC[index]}" }),
            FileChooser.ExtensionFilter(I18N["file_type.picture_bmp"], "*.${EXTENSION_PIC_BMP}"),
            FileChooser.ExtensionFilter(I18N["file_type.picture_gif"], "*.${EXTENSION_PIC_GIF}"),
            FileChooser.ExtensionFilter(I18N["file_type.picture_png"], "*.${EXTENSION_PIC_PNG}"),
            FileChooser.ExtensionFilter(I18N["file_type.picture_jpeg"], "*.${EXTENSION_PIC_JPG}", "*.${EXTENSION_PIC_JPEG}"),
            FileChooser.ExtensionFilter(I18N["file_type.picture_tiff"], "*.${EXTENSION_PIC_TIF}", "*.${EXTENSION_PIC_TIFF}"),
            FileChooser.ExtensionFilter(I18N["file_type.picture_webp"], "*.${EXTENSION_PIC_WEBP}"),
        )
    }
    private val dirChooser = DirectoryChooser()

    private lateinit var workingTransFile: TransFile
    private var picCount: Int = 0
    private var picNames: List<String> = ArrayList()
    private var files: MutableList<File?> = ArrayList()
    private var labels: MutableList<CRollerLabel> = ArrayList()

    init {
        title = I18N["specify.title"]
        dialogPane.prefWidth = PANE_WIDTH
        dialogPane.prefHeight = PANE_HEIGHT
        dialogPane.buttonTypes.addAll(ButtonType.APPLY, ButtonType.CANCEL)
        withContent(BorderPane()) {
            val stackPane = StackPane(contentGridPane)
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
                        for (label in labels) if (label.text != Unspecified) {
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
                        if (!preserve) state.transFile.projectFolder = directory

                        // auto-fill
                        val newPicPaths = Files.walk(directory.toPath(), 1)
                            .filter { path -> EXTENSIONS_PIC.contains(path.extension.lowercase()) }
                            .collect(Collectors.toList())
                        for (i in 0 until picCount) {
                            if (preserve && files[i].exists()) continue

                            val lastIndex = newPicPaths.size - 1
                            for (j in newPicPaths.indices) {
                                val oldPicFile = workingTransFile.getFile(picNames[i])!!
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

    /**
     * Specify pictures of current translation file
     * @return true if completed; false if not; null if cancel
     */
    fun specify(): Boolean? {
        // clear & re-init
        contentGridPane.children.clear()
        contentGridPane.add(Label(I18N["specify.dialog.pic_name"]), 0, 0)
        contentGridPane.add(Label(I18N["specify.dialog.pic_path"]), 1, 0)

        // update variables
        workingTransFile = state.transFile
        picCount = state.transFile.picCount
        picNames = state.transFile.sortedPicNames

        // prepare
        fileChooser.initialDirectory = state.transFile.projectFolder
        dirChooser.initialDirectory = state.transFile.projectFolder

        files = MutableList(picCount) { workingTransFile.getFile(picNames[it])?.takeIf(File::exists) }
        labels = MutableList(picCount) { CRollerLabel().apply {
            prefWidth = 300.0
            text = files[it]?.takeIf(File::exists)?.let(File::getPath) ?: Unspecified
            tooltipProperty().bind(object : ObjectBinding<Tooltip>() {
                init { bind(this@apply.textProperty()) }
                override fun computeValue(): Tooltip = Tooltip(this@apply.text).apply { showDelay = Duration(0.0) }
            })
            textFillProperty().bind(object : ObjectBinding<Color>() {
                init { bind(this@apply.textProperty()) }
                override fun computeValue(): Color = if (this@apply.text == Unspecified) Color.RED else Color.BLACK
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

            contentGridPane.add(Label(picNames[i]), 0, i + RowShift)
            contentGridPane.add(labels[i], 1, i + RowShift)
            contentGridPane.add(button, 2, i + RowShift)
        }

        // manually control label roll to save resource
        for (label in labels) label.startRoll()
        val result = showAndWait()
        for (label in labels) label.stopRoll()

        // Result list has the same order as TransFile.sortedPicNames.
        // Empty list if Closed or Cancelled;
        // List of specified files (not-null) or didn't specified files (null)
        val picFiles = if (result.isPresent) result.get() else emptyList()

        // Closed or Cancelled
        if (picFiles.isEmpty()) return null

        val picCount = state.transFile.picCount
        val picNames = state.transFile.sortedPicNames
        var completed = true
        for (i in 0 until picCount) {
            val picFile = picFiles[i]
            if (!picFile.exists()) {
                completed = false
                continue
            }

            state.transFile.setFile(picNames[i], picFile)
        }
        return completed
    }

}
