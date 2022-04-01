package ink.meodinger.lpfx.type

import ink.meodinger.lpfx.NOT_FOUND
import ink.meodinger.lpfx.util.file.notExists
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.keysProperty
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.property.sorted
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.string.sortByDigit

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
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
open class TransFile @JsonCreator constructor(
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

            fun pictureNotFound(picName: String) =
                TransFileException(String.format(I18N["exception.trans_file.picture_not_found.s"], picName))
            fun pictureNameRepeated(picName: String) =
                TransFileException(String.format(I18N["exception.trans_file.picture_name_repeated.s"], picName))
        }
    }

    // ----- Project Files Management ----- //

    lateinit var projectFolder: File

    private val fileMap = HashMap<String, File>()
    fun getFile(picName: String): File? {
        if (!transMapObservable.keys.contains(picName)) return null
        return fileMap[picName] ?: projectFolder.resolve(picName).also { setFile(picName, it) }
    }
    fun setFile(picName: String, file: File) {
        if (!transMapObservable.keys.contains(picName)) throw TransFileException.pictureNotFound(picName)
        fileMap[picName] = file
    }
    fun checkLost(): List<String> {
        return transMapObservable.keys.filter { getFile(it).notExists() }
    }

    // ----- Properties ----- //
    // Properties can only be used for bindings' values;

    val versionProperty: ReadOnlyListProperty<Int> = SimpleListProperty(FXCollections.observableList(version))
    val commentProperty: StringProperty = SimpleStringProperty(comment)
    val groupListProperty: ListProperty<TransGroup> = SimpleListProperty(FXCollections.observableArrayList(groupList))
    val transMapProperty: MapProperty<String, ObservableList<TransLabel>> = SimpleMapProperty(FXCollections.observableMap(transMap.mapValues { FXCollections.observableArrayList(it.value) }))

    val picNamesProperty: ReadOnlySetProperty<String> = transMapProperty.keysProperty()
    val sortedPicNamesProperty: ReadOnlyListProperty<String> = ReadOnlyListWrapper(picNamesProperty.sorted(::sortByDigit)).readOnlyProperty

    // ----- Accessible Fields ----- //
    // Observables can be used for bindings' dependencies and data edit;
    // Raw values can be used as simple data, they are all immutable except comment;

    val groupListObservable: ObservableList<TransGroup> by groupListProperty
    val transMapObservable: ObservableMap<String, ObservableList<TransLabel>> by transMapProperty
    val sortedPicNamesObservable: ObservableList<String> by sortedPicNamesProperty

    val version: List<Int> by versionProperty
    var comment: String by commentProperty
    val groupList: List<TransGroup> by groupListObservable
    val transMap: Map<String, List<TransLabel>> by transMapObservable
    val sortedPicNames: List<String> by sortedPicNamesObservable

    val groupCount: Int get() = groupListObservable.size
    val picCount: Int get() = transMapObservable.size

    // ----- TransGroup ----- //

    fun getGroupIdByName(name: String): Int {
        val index = groupListObservable.indexOfFirst { it.name == name }
        if (index == NOT_FOUND) throw TransFileException.transGroupNotFound(name)
        return index
    }
    fun isGroupUnused(groupId: Int): Boolean {
        for (list in transMapObservable.values) for (label in list) if (label.groupId == groupId) return false
        return true
    }

    // ----- DATA ----- //

    fun getTransGroup(groupId: Int): TransGroup {
        return groupListObservable[groupId]
    }
    open fun getTransList(picName: String): List<TransLabel> {
        return transMapObservable[picName] ?: throw TransFileException.pictureNotFound(picName)
    }
    fun getTransLabel(picName: String, labelIndex: Int): TransLabel {
        return getTransList(picName).first { it.index == labelIndex }
    }

    // ----- Other ----- //

    fun sorted(): TransFile {
        val version = this.version.toList()
        val comment = this.comment
        val groupList = MutableList(groupListObservable.size) { groupListObservable[it].clone() }
        val transMap = LinkedHashMap<String, MutableList<TransLabel>>().apply {
            putAll(sortedPicNamesObservable.map { it to transMapObservable[it]!!.sorted { l1, l2 -> l1.index - l2.index } })
        }

        return TransFile(version, comment, groupList, transMap)
    }

    fun toJsonString(): String {
        return ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .writeValueAsString(this)
    }

    override fun toString(): String = ObjectMapper().writeValueAsString(this)

}
