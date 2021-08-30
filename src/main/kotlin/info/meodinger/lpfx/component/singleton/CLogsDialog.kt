package info.meodinger.lpfx.component.singleton

import info.meodinger.lpfx.State
import info.meodinger.lpfx.component.CComboBox
import info.meodinger.lpfx.options.CProperty
import info.meodinger.lpfx.options.Logger
import info.meodinger.lpfx.options.Logger.LogType
import info.meodinger.lpfx.options.Options
import info.meodinger.lpfx.options.Settings
import info.meodinger.lpfx.util.dialog.showAlert
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get
import info.meodinger.lpfx.util.sendLog

import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import java.io.File
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import kotlin.io.path.name

/**
 * Author: Meodinger
 * Date: 2021/8/30
 * Location: info.meodinger.lpfx.component.singleton
 */
object CLogsDialog : Dialog<List<CProperty>>() {

    private const val Gap = 16.0

    private val root = GridPane()
    private val comboLevel = CComboBox<LogType>()
    private val tableLog = TableView<FileModal>()
    private val labelSent = Label()
    private val buttonSend = Button(I18N["logs.button.send"])
    private val buttonClean = Button(I18N["logs.button.clean"])
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private class FileModal(val file: File) {
        val nameProperty: ReadOnlyStringProperty = SimpleStringProperty(file.name)
        val timeProperty: ReadOnlyStringProperty = SimpleStringProperty(formatter.format(Date(file.lastModified())))
        val sizeProperty: ReadOnlyStringProperty = SimpleStringProperty(String.format("%.2f KB", (file.length() / 1024.0)))
    }

    init {
        initOwner(State.stage)

        initLogPane()
        root.padding = Insets(Gap)
        root.vgap = Gap
        root.hgap = Gap
        root.alignment = Pos.TOP_CENTER

        this.title = I18N["logs.title"]
        this.dialogPane.prefWidth = 600.0
        this.dialogPane.prefHeight = 400.0
        this.dialogPane.content = root
        this.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
        this.setResultConverter {
            when (it) {
                ButtonType.OK -> convertResult()
                else -> emptyList()
            }
        }
    }

    private fun initLogPane() {
        comboLevel.setList(listOf(LogType.DEBUG, LogType.INFO, LogType.WARNING, LogType.ERROR, LogType.FATAL))
        comboLevel.moveTo(LogType.getType(Settings[Settings.LogLevelPreference].asString()))

        val paths = Files.walk(Options.logs).filter { it.name != Options.logs.name }.collect(Collectors.toList())
        val data = MutableList(paths.size) { FileModal(paths[it].toFile()) }

        val nameCol = TableColumn<FileModal, String>(I18N["logs.table.name"]).also { column ->
            column.setCellValueFactory { it.value.nameProperty }
        }
        val timeCol = TableColumn<FileModal, String>(I18N["logs.table.time"]).also { column ->
            column.setCellValueFactory { it.value.timeProperty }
        }
        val sizeCol = TableColumn<FileModal, String>(I18N["logs.table.size"]).also { column ->
            column.setCellValueFactory { it.value.sizeProperty }
        }
        tableLog.columns.addAll(nameCol, timeCol, sizeCol)
        tableLog.items.addAll(data)

        tableLog.selectionModel.selectedItemProperty().addListener { _, _ , _ ->
            labelSent.text = ""
        }
        buttonSend.setOnAction {
            val log = tableLog.selectionModel.selectedItem.file

            sendLog(log)
            labelSent.text = "Sent ${log.name}"
        }
        buttonClean.setOnAction {
            val toRemove = ArrayList<FileModal>()
            for (modal in data) {
                if (modal.file.name == Logger.log.name) continue
                if (!modal.file.delete()) {
                    showAlert("Delete ${modal.file.name} failed")
                    continue
                }
                toRemove.add(modal)
            }
            data.removeAll(toRemove)
            tableLog.items.removeAll(toRemove)
        }

        //    0      1        2     3
        // 0  Label  ComboBox
        // 1  Label
        //    ----------------------------
        // 2  | FileName  EndTime  Size  |
        // 3  |                          |
        // 4  |                          |
        //    ----------------------------
        // 5   <    >         Send  Clean

        root.add(Label(I18N["logs.label.level"]), 0, 0)
        root.add(comboLevel, 1, 0)

        root.add(Label(I18N["logs.label.recorded"]), 0, 1)
        root.add(tableLog, 0, 2, 4, 3)
        root.add(labelSent, 0, 5, 2 , 1)
        root.add(buttonSend, 2, 5)
        root.add(buttonClean, 3, 5)
    }

    private fun convertResult(): List<CProperty> {
        val list = ArrayList<CProperty>()

        list.add(CProperty(Settings.LogLevelPreference, comboLevel.value))

        return list
    }

}