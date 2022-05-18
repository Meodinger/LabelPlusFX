package ink.meodinger.lpfx.component.common

import ink.meodinger.lpfx.util.file.exists
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue

import javafx.beans.property.*
import javafx.collections.ObservableList
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.Window
import java.io.File


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * A FileChooser with shared initial directory with other CFileChoosers
 * @see javafx.stage.FileChooser
 */
class CFileChooser {

    /// FileChooser is final class, inherit it is unavailable

    companion object {
        private val lastDirectoryProperty: ObjectProperty<File?> = SimpleObjectProperty(File(System.getProperty("user.home")))
        /**
         * ObjectProperty for the shared last directory. This intents could not
         * to be bound to some other properties. If you want to change this manually,
         * use setter instead.
         */
        fun lastDirectoryProperty(): ReadOnlyObjectProperty<File?> = lastDirectoryProperty
        /**
         * The shared last directory for all CFileChoosers.
         * @see lastDirectoryProperty
         */
        var lastDirectory : File?
            get() = lastDirectoryProperty.get().takeIf(File?::exists)
            set(value) {
                if (value == null || !value.exists()) return
                if (value.isDirectory) lastDirectoryProperty.set(value)
                else lastDirectoryProperty.set(value.parentFile)
            }
    }

    private val chooser = FileChooser()

    /**
     * An export to `FileChooser::extensionFilters`
     * @see javafx.stage.FileChooser.getExtensionFilters
     */
    val extensionFilters: ObservableList<ExtensionFilter> = chooser.extensionFilters

    private val selectedExtensionFilterProperty: ObjectProperty<ExtensionFilter?> = chooser.selectedExtensionFilterProperty()
    /**
     * An export to `FileChooser::selectedExtensionFilterProperty()`
     * @see javafx.stage.FileChooser.selectedExtensionFilter
     */
    fun selectedExtensionFilterProperty(): ObjectProperty<ExtensionFilter?> = selectedExtensionFilterProperty
    /**
     * @see selectedExtensionFilterProperty
     */
    var selectedExtensionFilter: ExtensionFilter? by selectedExtensionFilterProperty

    private val initialDirectoryProperty: ObjectProperty<File?> = chooser.initialDirectoryProperty()
    /**
     * An export to `FileChooser::initialDirectoryProperty()`
     * @see javafx.stage.FileChooser.initialDirectory
     */
    fun initialDirectoryProperty(): ObjectProperty<File?> = initialDirectoryProperty
    /**
     * @see initialDirectoryProperty
     */
    var initialDirectory: File? by initialDirectoryProperty

    private val initialFilenameProperty: ObjectProperty<String> = chooser.initialFileNameProperty()
    /**
     * An export to `FileChooser::initialFileNameProperty()`
     * @see javafx.stage.FileChooser.initialFileName
     */
    fun initialFilenameProperty(): ObjectProperty<String> = initialFilenameProperty
    /**
     * @see initialFilenameProperty
     */
    var initialFilename: String by initialFilenameProperty

    private val titleProperty: StringProperty = chooser.titleProperty()
    /**
     * An export to `FileChooser::titleProperty()`
     * @see javafx.stage.FileChooser.title
     */
    fun titleProperty(): StringProperty = titleProperty
    /**
     * @see titleProperty
     */
    var title: String by titleProperty

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
