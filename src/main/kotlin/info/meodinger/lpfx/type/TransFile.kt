package info.meodinger.lpfx.type

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import info.meodinger.lpfx.util.char.repeat
import info.meodinger.lpfx.util.string.isDigit
import info.meodinger.lpfx.util.string.trimSame

import java.util.TreeSet

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.type
 */
class TransFile {

    companion object {
        val DEFAULT_VERSION = intArrayOf(1, 0)
        val DEFAULT_COMMENT = "使用 LabelPlusFX 导出"
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
            val trimmed = trimSame(transFile.transMap.keys.toList())
            if (trimmed.size > 2) {
                var canCastToNumberList = true
                for (i in 2 until trimmed.size) {
                    if (!trimmed[i].isDigit()) {
                        canCastToNumberList = false
                        break
                    }
                }
                if (canCastToNumberList) {
                    val map = HashMap<Int, Int>()
                    val integerList = ArrayList<Int>()
                    for (i in 2 until trimmed.size) {
                        val num = trimmed[i].toInt()
                        integerList.add(num)
                        map[num] = i
                    }
                    integerList.sortWith(Comparator.naturalOrder())

                    var numberLength: Int
                    var complementLength: Int
                    val list = ArrayList<String>()
                    for (integer in integerList) {
                        numberLength = integer.toString().length
                        complementLength = trimmed[map[integer]!!].length - numberLength
                        list.add(trimmed[0] + '0'.repeat(complementLength) + integer + trimmed[1])
                    }
                    return list
                }
            }

            // default
            val sorted = TreeSet<String>(Comparator.naturalOrder())
            sorted.addAll(transFile.transMap.keys)
            return ArrayList(sorted)
        }
    }

    var version: IntArray = DEFAULT_VERSION
    var comment: String = DEFAULT_COMMENT
    @JsonAlias("group", "groups")
    var groupList: MutableList<TransGroup> = ArrayList(0)
    var transMap: MutableMap<String, MutableList<TransLabel>> = HashMap()

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