package ink.meodinger.lpfx.component.common

import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.stage.FileChooser
import javafx.stage.Window
import java.io.File


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: ink.meodinger.lpfx.component
 */

/**
 * A FileChooser with shared initial directory with other CFileChooser
 */
class CFileChooser {

    companion object {
        var lastDirectory : File?
            get() = lastDirectoryProperty.value
            set(value) {
                if (value == null) return
                if (!value.exists()) return
                if (value.isDirectory) lastDirectoryProperty.set(value)
                else lastDirectoryProperty.set(value.parentFile)
            }

        val lastDirectoryProperty: ObjectProperty<File> = SimpleObjectProperty(File(System.getProperty("user.home")))
    }

    private val chooser = FileChooser()

    var title: String by chooser.titleProperty()
    var initialFileName: String by chooser.initialFileNameProperty()
    val extensionFilters: ObservableList<FileChooser.ExtensionFilter> = chooser.extensionFilters
    val selectedFilter: FileChooser.ExtensionFilter? = chooser.selectedExtensionFilter

    init {
        chooser.initialDirectoryProperty().bind(lastDirectoryProperty)
    }

    fun showOpenDialog(owner: Window? = null): File? {
        val file = chooser.showOpenDialog(owner)
        if (file != null) lastDirectory = file.parentFile
        return file
    }

    fun showSaveDialog(owner: Window? = null): File? {
        val file = chooser.showSaveDialog(owner)
        if (file != null) lastDirectory = file.parentFile
        return file
    }
}