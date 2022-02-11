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

    /// FileChooser is final class, inherit it is unavailable

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
        initialDirectoryProperty.bindBidirectional(lastDirectoryProperty)
    }

    fun showOpenDialog(owner: Window?): File? {
        return chooser.showOpenDialog(owner).also {
            if (it != null) lastDirectory = it.parentFile
        }
    }

    fun showSaveDialog(owner: Window?): File? {
        return chooser.showSaveDialog(owner).also {
            if (it != null) lastDirectory = it.parentFile
        }
    }

    fun showOpenMultipleDialog(owner: Window?) : List<File>? {
        return chooser.showOpenMultipleDialog(owner).also {
            if (it != null) lastDirectory = it.last().parentFile
        }
    }
}
