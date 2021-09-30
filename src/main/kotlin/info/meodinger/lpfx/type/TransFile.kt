package info.meodinger.lpfx.type

import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get
import info.meodinger.lpfx.util.string.sortByDigit

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIncludeProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.type
 */

/**
 * A MEO Translation file
 */
@JsonIncludeProperties("version", "comment", "groupList", "transMap")
open class TransFile {

    companion object {
        val DEFAULT_FILE = TransFile()

        val DEFAULT_VERSION = intArrayOf(1, 0)

        const val DEFAULT_COMMENT = "使用 LabelPlusFX 导出"
        val DEFAULT_COMMENT_LIST = arrayListOf(
            DEFAULT_COMMENT,
            "Default Comment\nYou can edit me",
            "由 MoeFlow.com 导出",
            "由MoeTra.com导出"
        )

        object LPTransFile {
            const val PIC_START = ">>>>>>>>["
            const val PIC_END = "]<<<<<<<<"
            const val LABEL_START = "----------------["
            const val LABEL_END = "]----------------"
            const val PROP_START = "["
            const val PROP_END = "]"
            const val SPLIT = ","
            const val SEPARATOR = "-"

            val DEFAULT_COLOR_LIST = arrayOf(
                "FF0000", "0000FF", "008000",
                "1E90FF", "FFD700", "FF00FF",
                "A0522D", "FF4500", "9400D3"
            )
        }

        fun getSortedPicList(transFile: TransFile): List<String> {
            return sortByDigit(transFile.transMap.keys.toList())
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
                fun transGroupNotFound(transGroup: TransGroup) =
                    TransFileException(String.format(I18N["exception.trans_file.group_not_found.format.g"], transGroup))

                fun transLabelIndexRepeated(picName: String, index: Int) =
                    TransFileException(String.format(I18N["exception.trans_file.label_index_repeated.is"], index, picName))
                fun transLabelGroupIdOutOfBounds(groupId: Int) =
                    TransFileException(String.format(I18N["exception.trans_file.label_groupId_out_of_bounds.i"], groupId))
                fun transLabelNotFound(picName: String, index: Int) =
                    TransFileException(String.format(I18N["exception.trans_file.label_not_found.is"], index, picName))
                fun transLabelNotFound(picName: String, transLabel: TransLabel) =
                    TransFileException(String.format(I18N["exception.trans_file.label_not_found.l_s"], transLabel, picName))
            }
        }

    }

    var version: IntArray = DEFAULT_VERSION
    var comment: String = DEFAULT_COMMENT
    @JsonAlias("group", "groups")
    var groupList: MutableList<TransGroup> = ArrayList()
    var transMap: MutableMap<String, MutableList<TransLabel>> = HashMap()

    // ----- TransGroup ----- //

    fun addTransGroup(transGroup: TransGroup) {
        for (group in groupList)
            if (group.name == transGroup.name)
                throw TransFileException.transGroupNameRepeated(transGroup.name)

        groupList.add(transGroup)
    }
    fun getTransGroup(name: String): TransGroup {
        for (group in groupList) if (group.name == name) return group

        throw TransFileException.transGroupNotFound(name)
    }
    fun getTransGroup(id: Int): TransGroup {
        if (id < 0) throw TransFileException.transGroupIdNegative(id)
        if (id > groupList.size) throw TransFileException.transGroupIdOutOfBounds(id)

        return groupList[id]
    }
    fun removeTransGroup(transGroup: TransGroup) {
        var toRemove: TransGroup? = null
        for (group in groupList) if (group == transGroup) toRemove = group

        if (toRemove != null) groupList.remove(toRemove)
        else throw TransFileException.transGroupNotFound(transGroup)
    }
    fun removeTransGroup(name: String) {
        var toRemove: TransGroup? = null
        for (group in groupList) if (group.name == name) toRemove = group

        if (toRemove != null) groupList.remove(toRemove)
        else throw TransFileException.transGroupNotFound(name)
    }
    fun removeTransGroup(id: Int) {
        if (id < 0) throw TransFileException.transGroupIdNegative(id)
        if (id >= groupList.size) throw TransFileException.transGroupIdOutOfBounds(id)

        groupList.removeAt(id)
    }

    fun getGroupIdByName(name: String): Int {
        groupList.forEachIndexed { index, transGroup -> if (transGroup.name == name) return index }

        throw TransFileException.transGroupNotFound(name)
    }

    // ----- TransList (TransMap) ----- //

    open fun addTransList(picName: String) {
        transMap[picName] = ArrayList()
    }
    open fun getTransList(picName: String): MutableList<TransLabel> {
        return transMap[picName] ?: throw TransFileException.pictureNotFound(picName)
    }
    open fun removeTransList(picName: String) {
        if (transMap[picName] != null) transMap.remove(picName)
        else throw TransFileException.pictureNotFound(picName)
    }

    // ----- TransLabel ----- //

    fun addTransLabel(picName: String, transLabel: TransLabel) {
        val list = getTransList(picName)

        val (index, groupId) = transLabel
        if (groupId >= groupList.size) throw TransFileException.transLabelGroupIdOutOfBounds(groupId)
        for (label in list) if (label.index == index) throw TransFileException.transLabelIndexRepeated(picName, index)

        list.add(transLabel)
    }
    fun getTransLabel(picName: String, labelIndex: Int): TransLabel {
        for (label in getTransList(picName)) if (label.index == labelIndex) return label

        throw TransFileException.transLabelNotFound(picName, labelIndex)
    }
    fun removeTransLabel(picName: String, transLabel: TransLabel) {
        val list = getTransList(picName)
        var toRemove: TransLabel? = null
        for (label in list) if (label == transLabel) toRemove = label

        if (toRemove != null) list.remove(toRemove)
        else throw TransFileException.transLabelNotFound(picName, transLabel)
    }
    fun removeTransLabel(picName: String, labelIndex: Int) {
        val list = getTransList(picName)
        var toRemove: TransLabel? = null
        for (label in list) if (label.index == labelIndex) toRemove = label

        if (toRemove != null) list.remove(toRemove)
        else throw TransFileException.transLabelNotFound(picName, labelIndex)
    }

    // ----- Other ----- //

    fun clone(): TransFile {
        val translation = TransFile()

        translation.version = this.version.clone()
        translation.comment = this.comment

        translation.groupList = MutableList(this.groupList.size) { TransGroup() }
        for (i in 0 until this.groupList.size) {
            translation.groupList[i].name = this.groupList[i].name
            translation.groupList[i].color = this.groupList[i].color
        }

        translation.transMap = HashMap()
        for (key in this.transMap.keys) {
            translation.transMap[key] = MutableList(this.transMap[key]!!.size) { TransLabel() }
            for (i in 0 until this.transMap[key]!!.size) {
                val label = translation.transMap[key]!![i]
                label.index = this.transMap[key]!![i].index
                label.x = this.transMap[key]!![i].x
                label.y = this.transMap[key]!![i].y
                label.groupId = this.transMap[key]!![i].groupId
                label.text = this.transMap[key]!![i].text
            }
        }
        return translation
    }

    fun toJsonString(): String {
        val cloned = this.clone()

        val sorted = getSortedPicList(cloned)
        val map = LinkedHashMap<String, MutableList<TransLabel>>()
        for (key in sorted) map[key] = cloned.transMap[key]!!
        cloned.transMap = map

        val mapper = ObjectMapper()
        mapper.enable(SerializationFeature.INDENT_OUTPUT)
        return mapper.writeValueAsString(cloned)
    }

    override fun toString(): String = ObjectMapper().writeValueAsString(this)

}