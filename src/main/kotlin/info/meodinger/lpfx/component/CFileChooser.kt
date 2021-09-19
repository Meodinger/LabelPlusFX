package info.meodinger.lpfx.component

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.stage.FileChooser
import javafx.stage.Window
import java.io.File
import tornadofx.getValue
import tornadofx.setValue


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.component
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
                if (value.isDirectory) lastDirectoryProperty.set(value)
                else lastDirectoryProperty.set(value.parentFile)
            }

        val lastDirectoryProperty: ObjectProperty<File> = SimpleObjectProperty(File(System.getProperty("user.home")))
    }

    private val chooser = FileChooser()

    var title: String by chooser.titleProperty()
    val extensionFilter: ObservableList<FileChooser.ExtensionFilter> = chooser.extensionFilters

    init {
        chooser.initialDirectoryProperty().bind(lastDirectoryProperty)
    }

    fun showOpenDialog(owner: Window? = null): File? {
        val file = chooser.showOpenDialog(owner)
        lastDirectory = file
        return file
    }

    fun showSaveDialog(owner: Window? = null): File? {
        val file = chooser.showSaveDialog(owner)
        lastDirectory = file
        return file
    }
}