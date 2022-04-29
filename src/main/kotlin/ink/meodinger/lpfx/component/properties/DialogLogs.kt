package ink.meodinger.lpfx.component.properties

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.component.common.CComboBox
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Logger.LogLevel
import ink.meodinger.lpfx.options.Options
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.dialog.showError
import ink.meodinger.lpfx.util.event.isDoubleClick
import ink.meodinger.lpfx.util.property.onChange

import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.Date
import java.util.stream.Collectors
import kotlin.collections.HashSet
import kotlin.io.path.name


/**
 * Author: Meodinger
 * Date: 2021/8/30
 * Have fun with my code!
 */

/**
 * A Dialog Singleton for logs set/clean/send
 */
class DialogLogs : AbstractPropertiesDialog() {

    private class LogFile(val file: File) {

        companion object {
            private val formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        }

        val startTimeProperty: ReadOnlyStringProperty = SimpleStringProperty(formatter.format(Date(file.name.toLongOrNull() ?: -1)))
        val endTimeProperty: ReadOnlyStringProperty = SimpleStringProperty(formatter.format(Date(file.lastModified())))
        val sizeProperty: ReadOnlyStringProperty = SimpleStringProperty(String.format("%.2f KB", (file.length() / 1024.0)))
        val nameProperty: ReadOnlyStringProperty = SimpleStringProperty(file.name)

        override fun toString(): String = file.name
    }

    private val comboLevel = CComboBox<LogLevel>()
    private val tableLog = TableView<LogFile>()

    init {
        title = I18N["logs.title"]
        dialogPane.prefWidth = PANE_WIDTH
        dialogPane.prefHeight = PANE_HEIGHT
        dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
        withContent(GridPane()) {
            //    0      1           2     3
            // 0  Label  ComboBox
            // 1  Label
            //    -------------------------------
            // 2  | StartTime EndTime Size Name |
            // 3  |                             |
            // 4  |                             |
            //    -------------------------------
            // 5   (Send?)           Send  Clean

            padding = Insets(COMMON_GAP)
            vgap = COMMON_GAP
            hgap = COMMON_GAP
            alignment = Pos.TOP_CENTER

            add(Label(I18N["logs.label.level"]), 0, 0)
            add(comboLevel, 1, 0) {
                items = FXCollections.observableList(LogLevel.values().toList())
            }

            val labelSent = Label()
            add(Label(I18N["logs.label.recorded"]), 0, 1, 4, 1) {
                font = font.bold()
            }
            add(tableLog, 0, 2, 4, 3) {
                addColumn<LogFile, String>(I18N["logs.table.startTime"]) { it.value.startTimeProperty }
                addColumn<LogFile, String>(I18N["logs.table.endTime"]) { it.value.endTimeProperty }
                addColumn<LogFile, String>(I18N["logs.table.size"]) { it.value.sizeProperty }
                addColumn<LogFile, String>(I18N["logs.table.name"]) { it.value.nameProperty }
                selectionModel.selectionMode = SelectionMode.SINGLE
                selectionModel.selectedItemProperty().addListener(onChange { labelSent.text = "" })
                setRowFactory { TableRow<LogFile>().apply { setOnMouseClicked { e ->
                    if (e.isDoubleClick) Runtime.getRuntime().exec(
                        if (Config.isWin) "notepad ${item.file.absolutePath}"
                        else if (Config.isMac) "open -t ${item.file.absolutePath}"
                        else "vi ${item.file.absolutePath}"
                    )
                } } }
            }
            add(labelSent, 0, 5, 2 , 1) {
                textFill = Color.BLUE
            }
            add(Button(I18N["logs.button.send"]), 2, 5) {
                does {
                    val log = tableLog.selectionModel.selectedItem?.file ?: return@does

                    labelSent.text = I18N["common.sending"]
                    Logger.sendLog(log,
                        { labelSent.text = I18N["common.sent"] + " " + log.name },
                        { labelSent.text = "${I18N["common.failed"]} - ${(it.cause ?: it)::class.simpleName}" }
                    )
                }
            }
            add(Button(I18N["logs.button.clean"]), 3, 5) {
                does {
                    val toRemove = HashSet<LogFile>()
                    for (modal in tableLog.items) {
                        if (modal.file.name == Logger.log.name) continue
                        if (!modal.file.delete()) {
                            Logger.error("Delete log file ${modal.file.path} failed", LOG_SRC_OTHER)
                            showError(owner, String.format(I18N["logs.error.delete_failed.s"], modal.file.name))
                            continue
                        }
                        toRemove.add(modal)
                    }
                    tableLog.items.removeAll(toRemove)

                    Logger.info("Cleaned logs", LOG_SRC_OTHER)
                }
            }
        }

        initProperties()
    }

    override fun initProperties() {
        comboLevel.index = Settings.logLevel.ordinal

        val files = Files
            .walk(Options.logs, 1).filter { it.name != Options.logs.name }
            .map(Path::toFile).collect(Collectors.toList())
            .apply { sortByDescending(File::lastModified) }

        tableLog.items.clear()
        tableLog.items.addAll(MutableList(files.size) { LogFile(files[it]) })
    }

    override fun convertResult(): Map<String, Any> {
        return mapOf(
            Settings.LogLevel to comboLevel.index.let(LogLevel.values()::get),
        )
    }
}
