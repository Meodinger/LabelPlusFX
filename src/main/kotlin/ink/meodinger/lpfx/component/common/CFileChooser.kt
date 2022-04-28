package ink.meodinger.lpfx.component.common

import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue

import javafx.beans.property.*
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
 * A FileChooser with shared initial directory with other CFileChoosers
 *
 * @see javafx.stage.FileChooser
 */
class CFileChooser {

    /// FileChooser is final class, inherit it is unavailable

    companion object {
        private val lastDirectoryProperty: ObjectProperty<File> = SimpleObjectProperty(File(System.getProperty("user.home")))
        fun lastDirectoryProperty(): ReadOnlyObjectProperty<File> = lastDirectoryProperty
        var lastDirectory : File?
            get() = lastDirectoryProperty.get().takeIf(File::exists)
            set(value) {
                if (value == null || !value.exists()) return
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

    /**
     * @see javafx.stage.FileChooser.showOpenDialog
     */
    fun showOpenDialog(owner: Window?): File? {
        return chooser.showOpenDialog(owner).also {
            lastDirectory = it?.parentFile
        }
    }

    /**
     * @see javafx.stage.FileChooser.showSaveDialog
     */
    fun showSaveDialog(owner: Window?): File? {
        return chooser.showSaveDialog(owner).also {
            lastDirectory = it?.parentFile
        }
    }

    /**
     * @see javafx.stage.FileChooser.showOpenMultipleDialog
     */
    fun showOpenMultipleDialog(owner: Window?) : List<File>? {
        return chooser.showOpenMultipleDialog(owner).also {
            lastDirectory = it?.last()?.parentFile
        }
    }
}
