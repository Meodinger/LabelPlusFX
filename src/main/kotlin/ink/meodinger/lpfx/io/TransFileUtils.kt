package ink.meodinger.lpfx.io

import ink.meodinger.lpfx.I18N
import ink.meodinger.lpfx.get
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.string.deleteTailEOL
import ink.meodinger.lpfx.util.string.fixed

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import java.io.IOException

/**
 * Author: Meodinger
 * Date: 2022/4/26
 * Have fun with my code!
 */

/**
 * LPTransFile Contants
 */
internal object LPTransFile {
    const val PIC_START   = ">>>>>>>>["
    const val PIC_END     = "]<<<<<<<<"
    const val LABEL_START = "----------------["
    const val LABEL_END   = "]----------------"
    const val PROP_START  = "["
    const val PROP_END    = "]"
    const val SPLIT       = ","
    const val SEPARATOR   = "-"
}

/**
 * Build LPFile content
 * @return LPFile content in String
 */
fun TransFile.toLPString(): String {
    // Group count validate
    if (groupCount > 9) throw IOException(I18N["exception.loader.too_many_groups"])

    // <--
    // ----------------[index]----------------[xxxx,yyyy,g]
    // text
    //
    // -->
    fun buildLabel(transLabel: TransLabel): String {
        return StringBuilder()
            .append(LPTransFile.LABEL_START).append(transLabel.index).append(LPTransFile.LABEL_END)
            .append(LPTransFile.PROP_START)
            .append(transLabel.x.fixed(4)).append(LPTransFile.SPLIT)
            .append(transLabel.y.fixed(4)).append(LPTransFile.SPLIT)
            .append(transLabel.groupId + 1)
            .append(LPTransFile.PROP_END).append("\n")
            .append(transLabel.text).append("\n")
            .toString()
    }

    // <--
    // >>>>>>>>[name]<<<<<<<<
    // label1
    // label2
    // ...
    //
    // -->
    fun buildPic(picName: String): String {
        return StringBuilder()
            .append(LPTransFile.PIC_START).append(picName).append(LPTransFile.PIC_END).append("\n")
            .apply { for (label in getTransList(picName)) append(buildLabel(label)).append("\n") }.append("\n")
            .toString()
    }

    // <--
    // 1, 0
    // -->
    val vString = StringBuilder()
        .append(version[0])
        .append(LPTransFile.SPLIT)
        .append(version[1])
        .toString()

    // <--
    // group1
    // group2
    // -->
    val gString = StringBuilder()
        .apply { for (group in groupList) append(group.name).append("\n") }
        .deleteTailEOL().toString()

    val tString = StringBuilder()
        .apply { for (name in sortedPicNames) append(buildPic(name)) }
        .toString()

    return StringBuilder()
        .appendLine(vString)
        .appendLine(LPTransFile.SEPARATOR)
        .appendLine(gString)
        .appendLine(LPTransFile.SEPARATOR)
        .appendLine(comment)
        .appendLine("\n")
        .appendLine(tString)
        .toString()
}

/**
 * Build MeoFile content (sorted)
 * @return MeoFile content in String
 */
fun TransFile.toJsonString(): String {
    return ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .writeValueAsString(TransFile(
            version,
            comment,
            groupList.toMutableList(),
            sortedPicNames.associateWithTo(LinkedHashMap()) {
                transMap[it]!!.sortedBy(TransLabel::index).toMutableList()
            }
        ))
}
