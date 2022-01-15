package ink.meodinger.lpfx.component.singleton

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.component.common.CComboBox
import ink.meodinger.lpfx.io.LogSender
import ink.meodinger.lpfx.options.CProperty
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Logger.LogType
import ink.meodinger.lpfx.options.Options
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.dialog.showError
import ink.meodinger.lpfx.util.platform.isMac
import ink.meodinger.lpfx.util.platform.isWin
import ink.meodinger.lpfx.util.property.onChange
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get

import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import kotlin.io.path.name


/**
 * Author: Meodinger
 * Date: 2021/8/30
 * Have fun with my code!
 */

/**
 * A Dialog Singleton for logs set/clean/send
 */
object ADialogLogs : AbstractPropertiesDialog() {

    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private class LogFile(val file: File) {
        val startTimeProperty: ReadOnlyStringProperty = SimpleStringProperty(formatter.format(Date(file.name.toLong())))
        val endTimeProperty: ReadOnlyStringProperty = SimpleStringProperty(formatter.format(Date(file.lastModified())))
        val sizeProperty: ReadOnlyStringProperty = SimpleStringProperty(String.format("%.2f KB", (file.length() / 1024.0)))
        val nameProperty: ReadOnlyStringProperty = SimpleStringProperty(file.name)

        override fun toString(): String = file.name
    }

    private val comboLevel = CComboBox<LogType>()
    private val tableLog = TableView<LogFile>()

    init {
        initOwner(State.stage)

        title = I18N["logs.title"]
        dialogPane.prefWidth = DIALOG_WIDTH
        dialogPane.prefHeight = DIALOG_HEIGHT
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
                items.setAll(LogType.values().toList())
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
                    if (e.clickCount > 1) Runtime.getRuntime().exec(
                        if (isWin) "notepad ${item.file.absolutePath}"
                        else if (isMac) "open -t ${item.file.absolutePath}"
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
                    LogSender.send(log,
                        { labelSent.text = I18N["common.sent"] + " " + log.name },
                        { labelSent.text = "${I18N["common.failed"]} - ${(it.cause ?: it)::class.simpleName}" }
                    )
                }
            }
            add(Button(I18N["logs.button.clean"]), 3, 5) {
                does {
                    val toRemove = ArrayList<LogFile>()
                    for (modal in tableLog.items) {
                        if (modal.file.name == Logger.log.name) continue
                        if (!modal.file.delete()) {
                            Logger.error("Delete ${modal.file.path} failed", LOGSRC_DIALOGS)
                            showError(String.format(I18N["logs.error.delete_failed.s"], modal.file.name), State.stage)
                            continue
                        }
                        toRemove.add(modal)
                    }
                    tableLog.items.removeAll(toRemove)

                    Logger.info("Cleaned logs", LOGSRC_DIALOGS)
                }
            }
        }

        initProperties()
    }

    override fun initProperties() {
        comboLevel.select(LogType.getType(Settings[Settings.LogLevelPreference].asString()))

        val files = Files
            .walk(Options.logs, 1).filter { it.name != Options.logs.name }
            .map(Path::toFile).collect(Collectors.toList())
            .apply { sortByDescending(File::lastModified) }

        tableLog.items.clear()
        tableLog.items.addAll(MutableList(files.size) { LogFile(files[it]) })
    }

    override fun convertResult(): List<CProperty> {
        return listOf(CProperty(Settings.LogLevelPreference, comboLevel.value))
    }
}
