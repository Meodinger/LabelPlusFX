package ink.meodinger.lpfx.component.singleton

import ink.meodinger.lpfx.State
import ink.meodinger.lpfx.component.common.CComboBox
import ink.meodinger.lpfx.io.LogSender
import ink.meodinger.lpfx.options.CProperty
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Logger.LogType
import ink.meodinger.lpfx.options.Options
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.util.dialog.showError
import ink.meodinger.lpfx.util.platform.isMac
import ink.meodinger.lpfx.util.platform.isWin
import ink.meodinger.lpfx.util.property.onChange
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.string.isMathematicalInteger

import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import kotlin.io.path.deleteIfExists
import kotlin.io.path.name


/**
 * Author: Meodinger
 * Date: 2021/8/30
 * Have fun with my code!
 */

/**
 * A Dialog Singleton for logs set/clean/send
 */
object CLogsDialog : AbstractPropertiesDialog() {

    private const val GAP = 16.0
    private const val ALIVE = 3 * 24 * 60 * 60 * 1000L

    private val root = GridPane()
    private val comboLevel = CComboBox<LogType>()
    private val tableLog = TableView<FileModel>()
    private val labelSent = Label()
    private val buttonSend = Button(I18N["logs.button.send"])
    private val buttonClean = Button(I18N["logs.button.clean"])
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private class FileModel(val file: File) {
        val startTimeProperty: ReadOnlyStringProperty = SimpleStringProperty(formatter.format(Date(file.name.toLong())))
        val endTimeProperty: ReadOnlyStringProperty = SimpleStringProperty(formatter.format(Date(file.lastModified())))
        val sizeProperty: ReadOnlyStringProperty = SimpleStringProperty(String.format("%.2f KB", (file.length() / 1024.0)))
        val nameProperty: ReadOnlyStringProperty = SimpleStringProperty(file.name)

        override fun toString(): String = file.name
    }

    init {
        initOwner(State.stage)

        initLogPane()
        root.padding = Insets(GAP)
        root.vgap = GAP
        root.hgap = GAP
        root.alignment = Pos.TOP_CENTER

        initProperties()

        this.title = I18N["logs.title"]
        this.dialogPane.prefWidth = 600.0
        this.dialogPane.prefHeight = 400.0
        this.dialogPane.content = root
        this.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
    }

    private fun initLogPane() {
        comboLevel.setList(listOf(LogType.DEBUG, LogType.INFO, LogType.WARNING, LogType.ERROR, LogType.FATAL))

        val startTimeCol = TableColumn<FileModel, String>(I18N["logs.table.startTime"]).also { column ->
            column.setCellValueFactory { it.value.startTimeProperty }
        }
        val endTimeCol = TableColumn<FileModel, String>(I18N["logs.table.endTime"]).also { column ->
            column.setCellValueFactory { it.value.endTimeProperty }
        }
        val sizeCol = TableColumn<FileModel, String>(I18N["logs.table.size"]).also { column ->
            column.setCellValueFactory { it.value.sizeProperty }
        }
        val nameCol = TableColumn<FileModel, String>(I18N["logs.table.name"]).also { column ->
            column.setCellValueFactory { it.value.nameProperty }
        }
        tableLog.columns.addAll(startTimeCol, endTimeCol, sizeCol, nameCol)
        tableLog.setRowFactory { _ ->
            TableRow<FileModel>().also { row -> row.setOnMouseClicked {
                if (it.clickCount > 1) Runtime.getRuntime().exec(
                    if (isWin) "notepad ${row.item.file.absolutePath}"
                    else if (isMac) "open -t ${row.item.file.absolutePath}"
                    else "vi ${row.item.file.absolutePath}"
                )
            } }
        }

        tableLog.selectionModel.selectionMode = SelectionMode.SINGLE
        tableLog.selectionModel.selectedItemProperty().addListener(onChange { labelSent.text = "" })
        buttonSend.setOnAction {
            val log = tableLog.selectionModel.selectedItem?.file ?: return@setOnAction

            LogSender.sendLog(log)
            labelSent.text = "${I18N["logs.sent"]} ${log.name}"
        }
        buttonClean.setOnAction {
            val toRemove = ArrayList<FileModel>()
            for (modal in tableLog.items) {
                if (modal.file.name == Logger.log.name) continue
                if (!modal.file.delete()) {
                    Logger.warning("Delete ${modal.file.path} failed", "LogsDialog")
                    showError(String.format(I18N["error.logs.delete_failed.format.s"], modal.file.name))
                    continue
                }
                toRemove.add(modal)
            }
            tableLog.items.removeAll(toRemove)

            Logger.info("Cleaned logs", "LogsDialog")
            Logger.debug("Cleaned", toRemove, "LogsDialog")
        }

        //    0      1           2     3
        // 0  Label  ComboBox
        // 1  Label
        //    -------------------------------
        // 2  | StartTime EndTime Size Name |
        // 3  |                             |
        // 4  |                             |
        //    -------------------------------
        // 5   <    >            Send  Clean

        root.add(Label(I18N["logs.label.level"]), 0, 0)
        root.add(comboLevel, 1, 0)

        root.add(Label(I18N["logs.label.recorded"]), 0, 1)
        root.add(tableLog, 0, 2, 4, 3)
        root.add(labelSent, 0, 5, 2 , 1)
        root.add(buttonSend, 2, 5)
        root.add(buttonClean, 3, 5)
    }

    override fun initProperties() {
        comboLevel.moveTo(LogType.getType(Settings[Settings.LogLevelPreference].asString()))

        val toRemove = ArrayList<Path>()
        val paths = Files.walk(Options.logs).filter { it.name != Options.logs.name }.collect(Collectors.toList())
        for (path in paths) {
            if (!path.name.isMathematicalInteger()) toRemove.add(path)
            else if (Date().time - path.toFile().lastModified() > ALIVE) toRemove.add(path)
        }
        for (path in toRemove) path.deleteIfExists()
        paths.removeAll(toRemove)

        val data = MutableList(paths.size) { FileModel(paths[it].toFile()) }.also {
            it.sortByDescending { modal -> modal.file.lastModified() }
        }
        tableLog.items.clear()
        tableLog.items.addAll(data)
    }

    override fun convertResult(): List<CProperty> {
        val list = ArrayList<CProperty>()

        list.add(CProperty(Settings.LogLevelPreference, comboLevel.value))

        Logger.info("Generated settings", "LogsDialog")
        Logger.debug("got", list, "LogsDialog")

        return list
    }
}