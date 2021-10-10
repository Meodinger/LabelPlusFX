package info.meodinger.lpfx.type

import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get
import info.meodinger.lpfx.util.string.sortByDigit
import info.meodinger.lpfx.util.property.getValue
import info.meodinger.lpfx.util.property.setValue

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableMap


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.type
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

        val DEFAULT_FILE = TransFile()

        object LPTransFile {
            const val PIC_START = ">>>>>>>>["
            const val PIC_END = "]<<<<<<<<"
            const val LABEL_START = "----------------["
            const val LABEL_END = "]----------------"
            const val PROP_START = "["
            const val PROP_END = "]"
            const val SPLIT = ","
            const val SEPARATOR = "-"

            val DEFAULT_COLOR_LIST = listOf(
                "FF0000", "0000FF", "008000",
                "1E90FF", "FFD700", "FF00FF",
                "A0522D", "FF4500", "9400D3"
            )
        }

        // ----- Exception ----- //

        class TransFileException(message: String) : RuntimeException(message) {
            companion object {
                fun pictureNotFound(picName: String) =
                    TransFileException(String.format(I18N["exception.trans_file.picture_not_found.format.s"], picName))

                fun transGroupNameRepeated(groupName: String) =
                    TransFileException(String.format(I18N["exception.trans_file.group_name_repeated.format.s"], groupName))
                fun transGroupIdNegative(groupId: Int) =
                    TransFileException(String.format(I18N["exception.trans_file.group_id_negative.format.i"], groupId))
                fun transGroupIdOutOfBounds(groupId: Int) =
                    TransFileException(String.format(I18N["exception.trans_file.group_id_out_of_bounds.format.i"], groupId))
                fun transGroupNotFound(groupName: String) =
                    TransFileException(String.format(I18N["exception.trans_file.group_not_found.format.s"], groupName))

                fun transLabelIndexRepeated(picName: String, index: Int) =
                    TransFileException(String.format(I18N["exception.trans_file.label_index_repeated.is"], index, picName))
                fun transLabelGroupIdOutOfBounds(groupId: Int) =
                    TransFileException(String.format(I18N["exception.trans_file.label_groupId_out_of_bounds.i"], groupId))
                fun transLabelNotFound(picName: String, index: Int) =
                    TransFileException(String.format(I18N["exception.trans_file.label_not_found.is"], index, picName))
            }
        }

    }

    // ----- Properties ----- //

    val versionProperty: ReadOnlyObjectProperty<IntArray> = SimpleObjectProperty(version)
    val commentProperty: StringProperty = SimpleStringProperty(comment)
    val groupListProperty: ObservableList<TransGroup> = FXCollections.observableList(groupList)
    val transMapProperty: ObservableMap<String, MutableList<TransLabel>> = FXCollections.observableMap(transMap)

    // ----- Accessible Fields ----- //

    val version: IntArray by versionProperty
    var comment: String by commentProperty
    val groupList: List<TransGroup> by groupListProperty
    val transMap: Map<String, List<TransLabel>> by transMapProperty

    val groupCount: Int get() = groupListProperty.size
    val groupNames: List<String> get() = List(groupListProperty.size) { groupListProperty[it].name }
    val groupColors: List<String> get() = List(groupListProperty.size) { groupListProperty[it].colorHex }
    val sortedPicNames: List<String> get() = sortByDigit(transMapProperty.keys.toList())

    // ----- TransGroup ----- //

    fun addTransGroup(transGroup: TransGroup) {
        for (group in groupListProperty)
            if (group.name == transGroup.name)
                throw TransFileException.transGroupNameRepeated(transGroup.name)

        groupListProperty.add(transGroup)
    }
    fun getTransGroup(groupName: String): TransGroup {
        for (group in groupListProperty) if (group.name == groupName) return group

        throw TransFileException.transGroupNotFound(groupName)
    }
    fun getTransGroup(groupId: Int): TransGroup {
        if (groupId < 0) throw TransFileException.transGroupIdNegative(groupId)
        if (groupId >= groupListProperty.size) throw TransFileException.transGroupIdOutOfBounds(groupId)

        return groupListProperty[groupId]
    }
    fun removeTransGroup(groupName: String) {
        var toRemove: TransGroup? = null
        for (group in groupListProperty) if (group.name == groupName) toRemove = group

        if (toRemove != null) groupListProperty.remove(toRemove)
        else throw TransFileException.transGroupNotFound(groupName)
    }
    fun removeTransGroup(groupId: Int) {
        if (groupId < 0) throw TransFileException.transGroupIdNegative(groupId)
        if (groupId >= groupListProperty.size) throw TransFileException.transGroupIdOutOfBounds(groupId)

        groupListProperty.removeAt(groupId)
    }

    fun getGroupIdByName(name: String): Int {
        groupListProperty.forEachIndexed { index, transGroup -> if (transGroup.name == name) return index }

        throw TransFileException.transGroupNotFound(name)
    }
    fun isGroupUnused(groupName: String): Boolean {
        return isGroupUnused(getGroupIdByName(groupName))
    }
    fun isGroupUnused(groupId: Int): Boolean {
        for (key in transMapProperty.keys) for (label in getTransList(key)) {
            if (label.groupId == groupId) return false
        }
        return true
    }

    // ----- TransList (TransMap) ----- //

    open fun addTransList(picName: String) {
        transMapProperty[picName] = ArrayList()
    }
    open fun getTransList(picName: String): MutableList<TransLabel> {
        return transMapProperty[picName] ?: throw TransFileException.pictureNotFound(picName)
    }
    open fun removeTransList(picName: String) {
        if (transMapProperty[picName] != null) transMapProperty.remove(picName)
        else throw TransFileException.pictureNotFound(picName)
    }

    // ----- TransLabel ----- //

    fun addTransLabel(picName: String, transLabel: TransLabel) {
        val list = getTransList(picName)

        val (index, groupId) = transLabel
        if (groupId >= groupListProperty.size) throw TransFileException.transLabelGroupIdOutOfBounds(groupId)
        for (label in list) if (label.index == index) throw TransFileException.transLabelIndexRepeated(picName, index)

        list.add(transLabel)
    }
    fun getTransLabel(picName: String, labelIndex: Int): TransLabel {
        for (label in getTransList(picName)) if (label.index == labelIndex) return label

        throw TransFileException.transLabelNotFound(picName, labelIndex)
    }
    fun removeTransLabel(picName: String, labelIndex: Int) {
        val list = getTransList(picName)
        var toRemove: TransLabel? = null
        for (label in list) if (label.index == labelIndex) toRemove = label

        if (toRemove != null) list.remove(toRemove)
        else throw TransFileException.transLabelNotFound(picName, labelIndex)
    }

    // ----- Other ----- //

    fun toJsonString(): String {
        val mapper = ObjectMapper()

        mapper.enable(SerializationFeature.INDENT_OUTPUT)
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)

        return mapper.writeValueAsString(this.clone())
    }

    fun clone(): TransFile {
        val version = this.version.clone()
        val comment = this.comment
        val groupList = MutableList(this.groupListProperty.size) { this.groupListProperty[it].clone() }
        val transMap = LinkedHashMap<String, MutableList<TransLabel>>().also { map ->
            for (key in sortedPicNames)
                map[key] = MutableList(this.transMapProperty[key]!!.size) {
                    this.transMapProperty[key]!![it].clone()
                }
        }

        return TransFile(version, comment, groupList, transMap)
    }

    override fun toString(): String = ObjectMapper().writeValueAsString(this)

}