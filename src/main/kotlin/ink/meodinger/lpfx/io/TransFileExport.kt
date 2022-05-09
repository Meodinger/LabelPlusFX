package ink.meodinger.lpfx.io

import ink.meodinger.lpfx.FileType
import ink.meodinger.lpfx.I18N
import ink.meodinger.lpfx.get
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.doNothing
import ink.meodinger.lpfx.util.string.deleteTrailingEOL
import ink.meodinger.lpfx.util.string.fixed
import ink.meodinger.lpfx.util.using

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import java.io.*
import java.nio.charset.StandardCharsets


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * Export TransFile
 * File save type is based on the extension of the file.
 */
@Throws(IOException::class)
fun export(file: File, transFile: TransFile) {
    when(FileType.getFileType(file)) {
        FileType.LPFile  -> exportLP(file, transFile)
        FileType.MeoFile -> exportMeo(file, transFile)
    }
}

/**
 * Export TransFile as LP format
 */
@Throws(IOException::class)
private fun exportLP(file: File, transFile: TransFile) {
    using {
        val stream = FileOutputStream(file).autoClose()
        val writer = BufferedWriter(OutputStreamWriter(stream, StandardCharsets.UTF_8)).autoClose()

        // UTF-8 BOM (EF BB BF)
        val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        val text = transFile.toLPString()

        stream.write(bom)
        writer.write(text)

        writer.flush()
        stream.flush()
    } catch { e : IOException ->
        throw e
    } finally ::doNothing
}

/**
 * Export TransFile as MEO format
 */
@Throws(IOException::class)
private fun exportMeo(file: File, transFile: TransFile) {
    using {
        val stream = FileOutputStream(file).autoClose()
        val writer = BufferedWriter(OutputStreamWriter(stream, StandardCharsets.UTF_8))

        writer.write(transFile.toJsonString())

        writer.flush()
        stream.flush()
    } catch { e: IOException ->
        throw e
    } finally ::doNothing
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
        .deleteTrailingEOL().toString()

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
