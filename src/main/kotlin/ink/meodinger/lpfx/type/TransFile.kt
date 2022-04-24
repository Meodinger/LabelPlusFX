package ink.meodinger.lpfx.type

import ink.meodinger.lpfx.NOT_FOUND
import ink.meodinger.lpfx.I18N
import ink.meodinger.lpfx.get
import ink.meodinger.lpfx.util.property.*
import ink.meodinger.lpfx.util.string.sortByDigit

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
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
    @JsonProperty("version")   version:   List<Int>                                   = DEFAULT_VERSION,
    @JsonProperty("comment")   comment:   String                                      = DEFAULT_COMMENT,
    @JsonProperty("groupList") groupList: MutableList<TransGroup>                     = ArrayList(),
    @JsonProperty("transMap")  transMap:  MutableMap<String, MutableList<TransLabel>> = HashMap()
)  {

    companion object {

        val DEFAULT_VERSION = listOf(1, 0)

        const val DEFAULT_COMMENT = "使用 LabelPlusFX 导出"
        val DEFAULT_COMMENT_LIST = arrayListOf(
            DEFAULT_COMMENT,
            "Default Comment\nYou can edit me",
            "由 MoeFlow.com 导出",
            "由MoeTra.com导出"
        )

        object LPTransFile {
            const val PIC_START   = ">>>>>>>>["
            const val PIC_END     = "]<<<<<<<<"
            const val LABEL_START = "----------------["
            const val LABEL_END   = "]----------------"
            const val PROP_START  = "["
            const val PROP_END    = "]"
            const val SPLIT       = ","
            const val SEPARATOR   = "-"

            val DEFAULT_COLOR_HEX_LIST = listOf(
                "FF0000", "0000FF", "008000",
                "1E90FF", "FFD700", "FF00FF",
                "A0522D", "FF4500", "9400D3"
            )
        }

    }

    // ----- Exception ----- //

    class TransFileException(message: String) : RuntimeException(message) {
        companion object {
            fun transGroupNotFound(groupName: String) =
                TransFileException(String.format(I18N["exception.trans_file.group_not_found.s"], groupName))
            fun transGroupNameRepeated(groupName: String) =
                TransFileException(String.format(I18N["exception.trans_file.group_name_repeated.s"], groupName))
            fun transGroupStillInUse(groupName: String) =
                TransFileException(String.format(I18N["exception.trans_file.group_still_in_use.s"], groupName))

            fun pictureNotFound(picName: String) =
                TransFileException(String.format(I18N["exception.trans_file.picture_not_found.s"], picName))
            fun pictureNameRepeated(picName: String) =
                TransFileException(String.format(I18N["exception.trans_file.picture_name_repeated.s"], picName))
        }
    }

    // ----- Project Files Management ----- //

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
        if (!transMapObservable.keys.contains(picName)) return null
        return fileMap[picName] ?: projectFolder.resolve(picName).also { setFile(picName, it) }
    }

    /**
     * Set the actual file of the given picture name
     * @param picName Name of target picture
     * @param file Actual file of the picture, null to remove the file have set.
     */
    fun setFile(picName: String, file: File?) {
        if (!transMapObservable.keys.contains(picName)) throw TransFileException.pictureNotFound(picName)
        if (file == null) fileMap.remove(picName) else fileMap[picName] = file
    }

    /**
     * Check whether files of some pictures not exist
     */
    fun checkLost(): List<String> {
        return transMapObservable.keys.filter { getFile(it)!!.exists() }
    }

    // ----- Properties ----- //
    // Only internal use to avoid accidentally invoking their `set` methods
    // Note1: all backing list/map/set should be mutable
    // Note2: version is immutable
    // Note3: groups' properties' changes will be listened

    private val versionProperty: ReadOnlyListProperty<Int> = SimpleListProperty(FXCollections.observableList(version))
    private val commentProperty: StringProperty = SimpleStringProperty(comment)
    private val groupListProperty: ListProperty<TransGroup> = SimpleListProperty(FXCollections.observableList(groupList) { arrayOf(it.nameProperty(), it.colorHexProperty()) })
    private val transMapProperty: MapProperty<String, ObservableList<TransLabel>> = SimpleMapProperty(FXCollections.observableMap(transMap.mapValues { FXCollections.observableList(it.value) }))

    private val sortedPicNamesProperty: ReadOnlyListProperty<String> = ReadOnlyListWrapper(transMapProperty.observableKeySet().observableSorted(::sortByDigit)).readOnlyProperty

    // ----- Accessible Fields ----- //

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

    // ----- TransGroup ----- //

    fun getGroupIdByName(name: String): Int {
        val index = groupListObservable.indexOfFirst { it.name == name }
        if (index == NOT_FOUND) throw TransFileException.transGroupNotFound(name)
        return index
    }
    fun isGroupStillInUse(groupId: Int): Boolean {
        for (list in transMapObservable.values) for (label in list) if (label.groupId == groupId) return true
        return false
    }

    // ----- Data ----- //

    fun getTransGroup(groupId: Int): TransGroup {
        return groupListObservable[groupId]
    }
    fun getTransList(picName: String): List<TransLabel> {
        return transMapObservable[picName] ?: throw TransFileException.pictureNotFound(picName)
    }
    fun getTransLabel(picName: String, labelIndex: Int): TransLabel {
        return getTransList(picName).first { it.index == labelIndex }
    }

    // ----- Other ----- //

    fun sorted(): TransFile {
        return TransFile(
            version.toList(),
            comment,
            groupList.mapTo(ArrayList(), TransGroup::clone),
            sortedPicNames.associateWithTo(LinkedHashMap()) { transMap[it]!!.map(TransLabel::clone).sortedBy(TransLabel::index).toMutableList() }
        )
    }

    fun toJsonString(): String {
        return ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .writeValueAsString(this)
    }

    override fun toString(): String = ObjectMapper().writeValueAsString(this)

}
