package info.meodinger.lpfx.type

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get
import info.meodinger.lpfx.util.string.sortByDigit

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.type
 */
class TransFile {

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
        }

        object MeoTransFile {
            val DEFAULT_COLOR_LIST = arrayOf(
                "FF0000", "0000FF", "008000",
                "1E90FF", "FFD700", "FF00FF",
                "A0522D", "FF4500", "9400D3"
            )
        }

        fun getSortedPicList(transFile: TransFile): List<String> {
            return sortByDigit(transFile.transMap.keys.toList())
        }
    }

    var version: IntArray = DEFAULT_VERSION
    var comment: String = DEFAULT_COMMENT
    @JsonAlias("group", "groups")
    var groupList: MutableList<TransGroup> = ArrayList()
    var transMap: MutableMap<String, MutableList<TransLabel>> = HashMap()

    fun getTransGroupAt(groupId: Int): TransGroup {
        if (groupId < 0 || groupId >= groupList.size)
            throw IllegalArgumentException(String.format(I18N["exception.illegal_argument.groupId_invalid.format"], groupId))
        return groupList[groupId]
    }

    fun getTransLabelListOf(picName: String): MutableList<TransLabel> {
        return transMap[picName] ?: throw IllegalArgumentException(String.format(I18N["exception.illegal_argument.pic_not_found.format"], picName))
    }

    fun getTransLabelAt(picName: String, index: Int): TransLabel {
        val list = getTransLabelListOf(picName)
        for (transLabel in list) if (transLabel.index == index) return transLabel
        throw IllegalArgumentException(String.format(I18N["exception.illegal_argument.index_not_found.format"], index, picName))
    }

    override fun toString(): String = ObjectMapper().writeValueAsString(this)

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

}