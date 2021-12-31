package ink.meodinger.lpfx.component.singleton

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.component.common.CComboBox
import ink.meodinger.lpfx.io.LogSender
import ink.meodinger.lpfx.options.CProperty
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Logger.LogType
import ink.meodinger.lpfx.options.Options
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.util.component.does
import ink.meodinger.lpfx.util.dialog.showError
import ink.meodinger.lpfx.util.platform.isMac
import ink.meodinger.lpfx.util.platform.isWin
import ink.meodinger.lpfx.util.property.onChange
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.string.isMathematicalNatural

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
object ADialogLogs : AbstractPropertiesDialog() {

    private const val ALIVE = 3 * 24 * 60 * 60 * 1000L // 3 Days
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private class LogFile(val file: File) {
        val startTimeProperty: ReadOnlyStringProperty = SimpleStringProperty(formatter.format(Date(file.name.toLong())))
        val endTimeProperty: ReadOnlyStringProperty = SimpleStringProperty(formatter.format(Date(file.lastModified())))
        val sizeProperty: ReadOnlyStringProperty = SimpleStringProperty(String.format("%.2f KB", (file.length() / 1024.0)))
        val nameProperty: ReadOnlyStringProperty = SimpleStringProperty(file.name)

        override fun toString(): String = file.name
    }

    private val root = GridPane()
    private val comboLevel = CComboBox<LogType>()
    private val tableLog = TableView<LogFile>()

    init {
        initOwner(State.stage)

        comboLevel.items.setAll(listOf(LogType.DEBUG, LogType.INFO, LogType.WARNING, LogType.ERROR, LogType.FATAL))
        val labelSent = Label()
        val buttonSend = Button(I18N["logs.button.send"]) does  {
            val log = tableLog.selectionModel.selectedItem?.file ?: return@does

            labelSent.text = I18N["common.sending"]
            LogSender.send(log,
                { labelSent.text = I18N["common.sent"] + " " + log.name },
                { labelSent.text = I18N["common.failed"] }
            )

        }
        val buttonClean = Button(I18N["logs.button.clean"]) does {
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

        val startTimeCol = TableColumn<LogFile, String>(I18N["logs.table.startTime"]).also { column ->
            column.setCellValueFactory { it.value.startTimeProperty }
        }
        val endTimeCol = TableColumn<LogFile, String>(I18N["logs.table.endTime"]).also { column ->
            column.setCellValueFactory { it.value.endTimeProperty }
        }
        val sizeCol = TableColumn<LogFile, String>(I18N["logs.table.size"]).also { column ->
            column.setCellValueFactory { it.value.sizeProperty }
        }
        val nameCol = TableColumn<LogFile, String>(I18N["logs.table.name"]).also { column ->
            column.setCellValueFactory { it.value.nameProperty }
        }
        tableLog.columns.addAll(startTimeCol, endTimeCol, sizeCol, nameCol)
        tableLog.setRowFactory { _ ->
            TableRow<LogFile>().also { row -> row.setOnMouseClicked {
                if (it.clickCount > 1) Runtime.getRuntime().exec(
                    if (isWin) "notepad ${row.item.file.absolutePath}"
                    else if (isMac) "open -t ${row.item.file.absolutePath}"
                    else "vi ${row.item.file.absolutePath}"
                )
            } }
        }
        tableLog.selectionModel.selectionMode = SelectionMode.SINGLE
        tableLog.selectionModel.selectedItemProperty().addListener(onChange { labelSent.text = "" })

        //    0      1           2     3
        // 0  Label  ComboBox
        // 1  Label
        //    -------------------------------
        // 2  | StartTime EndTime Size Name |
        // 3  |                             |
        // 4  |                             |
        //    -------------------------------
        // 5   <    >            Send  Clean
        root.apply {
            padding = Insets(COMMON_GAP)
            vgap = COMMON_GAP
            hgap = COMMON_GAP
            alignment = Pos.TOP_CENTER

            add(Label(I18N["logs.label.level"]), 0, 0)
            add(comboLevel, 1, 0)

            add(Label(I18N["logs.label.recorded"]), 0, 1)
            add(tableLog, 0, 2, 4, 3)
            add(labelSent, 0, 5, 2 , 1)
            add(buttonSend, 2, 5)
            add(buttonClean, 3, 5)
        }

        initProperties()

        title = I18N["logs.title"]
        dialogPane.prefWidth = DIALOG_WIDTH
        dialogPane.prefHeight = DIALOG_HEIGHT
        dialogPane.content = root
        dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
    }

    override fun initProperties() {
        comboLevel.select(LogType.getType(Settings[Settings.LogLevelPreference].asString()))

        val toRemove = ArrayList<Path>()
        val paths = Files.walk(Options.logs).filter { it.name != Options.logs.name }.collect(Collectors.toList())
        for (path in paths) {
            if (!path.name.isMathematicalNatural()) toRemove.add(path)
            else if (Date().time - path.toFile().lastModified() > ALIVE) toRemove.add(path)
        }
        for (path in toRemove) path.deleteIfExists()
        paths.removeAll(toRemove)

        val data = MutableList(paths.size) { LogFile(paths[it].toFile()) }.also {
            it.sortByDescending { modal -> modal.file.lastModified() }
        }
        tableLog.items.clear()
        tableLog.items.addAll(data)
    }

    override fun convertResult(): List<CProperty> {
        val list = ArrayList<CProperty>()

        list.add(CProperty(Settings.LogLevelPreference, comboLevel.value))

        return list
    }
}
