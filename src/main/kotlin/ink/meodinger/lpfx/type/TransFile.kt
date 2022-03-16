package ink.meodinger.lpfx.type

import ink.meodinger.lpfx.NOT_FOUND
import ink.meodinger.lpfx.util.file.notExists
import ink.meodinger.lpfx.util.ReLazy
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.string.sortByDigit
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import javafx.beans.InvalidationListener
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
    @JsonProperty("version")   version:   IntArray                                    = DEFAULT_VERSION,
    @JsonProperty("comment")   comment:   String                                      = DEFAULT_COMMENT,
    @JsonProperty("groupList") groupList: MutableList<TransGroup>                     = ArrayList(),
    @JsonProperty("transMap")  transMap:  MutableMap<String, MutableList<TransLabel>> = HashMap()
) {

    companion object {

        val DEFAULT_VERSION = intArrayOf(1, 0)

        const val DEFAULT_COMMENT = "使用 LabelPlusFX 导出"
        val DEFAULT_COMMENT_LIST = arrayListOf(
            DEFAULT_COMMENT,
            "Default Comment\nYou can edit me",
            "由 MoeFlow.com 导出",
            "由MoeTra.com导出"
        )

        val DEFAULT_TRANS_FILE = TransFile()

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
    fun getFile(picName: String): File {
        if (!transMapObservable.keys.contains(picName)) throw TransFileException.pictureNotFound(picName)
        return fileMap[picName] ?: projectFolder.resolve(picName).also { setFile(picName, it) }
    }
    fun setFile(picName: String, file: File) {
        if (!transMapObservable.keys.contains(picName)) throw TransFileException.pictureNotFound(picName)
        fileMap[picName] = file
    }
    fun checkLost(): List<String> {
        return picNames.filter { getFile(it).notExists() }
    }

    // ----- Properties ----- //

    val versionProperty: ReadOnlyObjectProperty<IntArray> = SimpleObjectProperty(version)
    val commentProperty: StringProperty = SimpleStringProperty(comment)
    val groupListProperty: ListProperty<TransGroup> = SimpleListProperty(FXCollections.observableArrayList(groupList))
    val transMapProperty: MapProperty<String, ObservableList<TransLabel>> = SimpleMapProperty(FXCollections.observableMap(transMap.mapValues { FXCollections.observableArrayList(it.value) }))

    // ----- Lazy ---- //

    private val sortedPicNamesLazy: ReLazy<List<String>> = ReLazy { sortByDigit(transMapObservable.keys.toList()) } // copy
    val sortedPicNames: List<String> by sortedPicNamesLazy

    // ----- Accessible Fields ----- //

    val version: IntArray by versionProperty
    var comment: String by commentProperty
    val groupListObservable: ObservableList<TransGroup> by groupListProperty
    val transMapObservable: ObservableMap<String, ObservableList<TransLabel>> by transMapProperty

    val groupCount: Int get() = groupListObservable.size
    val groupNames: List<String> get() = List(groupListObservable.size) { getTransGroup(it).name }
    val groupColors: List<String> get() = List(groupListObservable.size) { getTransGroup(it).colorHex }

    val picCount: Int get() = transMapObservable.size
    val picNames: List<String> get() = transMapObservable.keys.toList() // copy

    // ----- JSON Getters ----- //

    @Suppress("unused") protected val groupList: List<TransGroup> by groupListObservable
    @Suppress("unused") protected val transMap: Map<String, List<TransLabel>> by transMapObservable

    // ----- Init ----- //

    init {
        transMapObservable.addListener(InvalidationListener { sortedPicNamesLazy.refresh() })
    }

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
        val version = this.version.clone()
        val comment = this.comment
        val groupList = MutableList(groupListObservable.size) { groupListObservable[it].clone() }
        val transMap = LinkedHashMap<String, MutableList<TransLabel>>().apply {
            putAll(sortedPicNames.map { it to transMapObservable[it]!!.sorted { l1, l2 -> l1.index - l2.index } })
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
