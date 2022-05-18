package ink.meodinger.lpfx.type

import ink.meodinger.lpfx.util.property.*
import ink.meodinger.lpfx.util.string.sortByDigit

import com.fasterxml.jackson.annotation.*
import javafx.beans.property.*
import javafx.collections.*
import java.io.File


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * A MEO Translation file
 */
@JsonIncludeProperties("version", "comment", "groupList", "transMap")
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.ANY)
class TransFile @JsonCreator constructor(
    @JsonProperty("version")   version:   List<Int>                     = listOf(1, 0),
    @JsonProperty("comment")   comment:   String                        = DEFAULT_COMMENT_LIST[0],
    @JsonProperty("groupList") groupList: List<TransGroup>              = emptyList(),
    @JsonProperty("transMap")  transMap:  Map<String, List<TransLabel>> = emptyMap()
)  {

    companion object {

        /**
         * 0 - LPFX;
         * 1 - LP;
         * 2 - MoeFlow;
         * 3 - MoeTra (Deprecated)
         */
        val DEFAULT_COMMENT_LIST: List<String> = arrayListOf(
            "使用 LabelPlusFX 导出",
            "Default Comment\nYou can edit me",
            "由 MoeFlow.com 导出",
            "由MoeTra.com导出"
        )

        /**
         * Default color hex list. Copy from LP
         */
        val DEFAULT_COLOR_HEX_LIST: List<String> = listOf(
            "FF0000", "0000FF", "008000",
            "1E90FF", "FFD700", "FF00FF",
            "A0522D", "FF4500", "9400D3"
        )
    }

    // region Project Files Management
    // These data are only used in runtime, will not be saved

    /**
     * This will be set while opening
     */
    lateinit var projectFolder: File

    private val fileMap = HashMap<String, File>()

    /**
     * Get the actual file of the given picture name
     * @param picName Name of target picture
     * @return null if the trans-map does not contain the given name
     */
    fun getFile(picName: String): File? {
        if (picName !in transMapObservable.keys) return null
        return fileMap[picName] ?: projectFolder.resolve(picName).also { setFile(picName, it) }
    }

    /**
     * Set the actual file of the given picture name.
     * Note that we could set even the target picture doesn't in the trans-map.
     * So make sure you set null when remove a picture.
     * @param picName Name of target picture
     * @param file Actual file of the picture, null to remove the file have set.
     */
    fun setFile(picName: String, file: File?) {
        if (file == null) fileMap.remove(picName) else fileMap[picName] = file
    }

    /**
     * Check whether files of some pictures not exist
     */
    fun checkLost(): List<String> {
        return transMapObservable.keys.filter { !getFile(it)!!.exists() }
    }

    // endregion

    // region Properties
    // Only internal use to avoid accidentally invoking their `set` methods
    // Note1: version is immutable
    // Note2: all backing list/map/set should be mutable
    // Note3: groups' properties' changes will be listened
    // Note4: transMap use LinkedHashMap to preserve key order when export.

    private val versionProperty: ReadOnlyListProperty<Int> = SimpleListProperty(FXCollections.observableList(version))
    private val commentProperty: StringProperty = SimpleStringProperty(comment)
    private val groupListProperty: ListProperty<TransGroup> = SimpleListProperty(FXCollections.observableList(ArrayList(groupList)) { arrayOf(it.nameProperty(), it.colorHexProperty()) })
    private val transMapProperty: MapProperty<String, ObservableList<TransLabel>> = SimpleMapProperty(FXCollections.observableMap((transMap.mapValuesTo(LinkedHashMap()) { FXCollections.observableArrayList(it.value) })))

    private val sortedPicNamesProperty: ReadOnlyListProperty<String> = ReadOnlyListWrapper(transMapProperty.observableKeySet().observableSorted(Collection<String>::sortByDigit)).readOnlyProperty

    // endregion

    // region Accessible Fields

    // Following properties provide JSON getters
    val version: List<Int> by versionProperty
    var comment: String by commentProperty
    val groupList: List<TransGroup> by groupListProperty
    val transMap: Map<String, List<TransLabel>> by transMapProperty

    val groupListObservable: ObservableList<TransGroup> by groupListProperty
    val transMapObservable: ObservableMap<String, ObservableList<TransLabel>> by transMapProperty
    val sortedPicNamesObservable: ObservableList<String> by sortedPicNamesProperty

    val groupCount: Int get() = groupListProperty.size
    val picCount: Int get() = transMapProperty.size
    val sortedPicNames: List<String> by sortedPicNamesProperty

    // endregion

    init {
        @Suppress("DEPRECATION") for (labels in transMap.values) for (label in labels) installLabel(label)
    }

    // TransGroup
    fun getGroupIdByName(name: String): Int {
        return groupList.first { it.name == name }.let(groupList::indexOf)
    }
    fun isGroupStillInUse(groupId: Int): Boolean {
        return transMap.values.flatten().any { label -> label.groupId == groupId }
    }

    // TransLabel
    /**
     * Install the color-property of TransLabel based on this TransFile
     */
    @Deprecated(level = DeprecationLevel.WARNING, message = "Only in Action")
    fun installLabel(transLabel: TransLabel) {
        @Suppress("DEPRECATION")
        TransLabel.installColor(transLabel, groupListProperty.valueAt(transLabel.groupIdProperty()).transform(TransGroup::color))
    }
    /**
     * Dispose the color-property of TransLabel
     */
    @Deprecated(level = DeprecationLevel.WARNING, message = "Only in Action")
    fun disposeLabel(transLabel: TransLabel) {
        @Suppress("DEPRECATION")
        TransLabel.disposeColor(transLabel)
    }

    // ----- Data ----- //

    fun getTransGroup(groupId: Int): TransGroup {
        return groupListObservable[groupId]
    }
    fun getTransList(picName: String): List<TransLabel> {
        return transMapObservable[picName]!!
    }
    fun getTransLabel(picName: String, labelIndex: Int): TransLabel {
        return getTransList(picName).first { it.index == labelIndex }
    }

    // Destructing

    operator fun component1(): List<Int>                     = version
    operator fun component2(): String                        = comment
    operator fun component3(): List<TransGroup>              = groupList
    operator fun component4(): Map<String, List<TransLabel>> = transMap

}
