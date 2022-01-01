package ink.meodinger.lpfx.component.common

import ink.meodinger.lpfx.util.file.existsOrNull
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.StringProperty
import javafx.collections.ObservableList
import javafx.stage.FileChooser
import javafx.stage.Window
import java.io.File


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * A FileChooser with shared initial directory with other CFileChooser
 */
class CFileChooser {

    /// FileChooser is final class, extension unavailable

    companion object {
        private val lastDirectoryProperty: ObjectProperty<File> = SimpleObjectProperty(File(System.getProperty("user.home")))
        fun lastDirectoryProperty(): ObjectProperty<File> = lastDirectoryProperty
        var lastDirectory : File?
            get() = lastDirectoryProperty.get().existsOrNull()
            set(value) {
                if (value == null) return
                if (!value.exists()) return
                if (value.isDirectory) lastDirectoryProperty.set(value)
                else lastDirectoryProperty.set(value.parentFile)
            }
    }

    private val chooser = FileChooser()

    private val initialDirectoryProperty: ObjectProperty<File> = chooser.initialDirectoryProperty()
    fun initialDirectoryProperty(): ObjectProperty<File> = initialDirectoryProperty
    var initialDirectory: File by initialDirectoryProperty

    private val initialFilenameProperty: ObjectProperty<String> = chooser.initialFileNameProperty()
    fun initialFilenameProperty(): ObjectProperty<String> = initialFilenameProperty
    var initialFilename: String by initialFilenameProperty

    private val titleProperty: StringProperty = chooser.titleProperty()
    fun titleProperty(): StringProperty = titleProperty
    var title: String by titleProperty

    val extensionFilters: ObservableList<FileChooser.ExtensionFilter> = chooser.extensionFilters
    var selectedExtensionFilter: FileChooser.ExtensionFilter? = chooser.selectedExtensionFilter

    init {
        initialDirectoryProperty.bind(lastDirectoryProperty)
    }

    fun showOpenDialog(owner: Window?): File? {
        val file = chooser.showOpenDialog(owner)
        if (file != null) lastDirectory = file.parentFile
        return file
    }

    fun showSaveDialog(owner: Window?): File? {
        val file = chooser.showSaveDialog(owner)
        if (file != null) lastDirectory = file.parentFile
        return file
    }

    fun showOpenMultipleDialog(owner: Window?) : List<File>? {
        val files = chooser.showOpenMultipleDialog(owner)
        if (files != null) lastDirectory = files.last().parentFile
        return files
    }
}
