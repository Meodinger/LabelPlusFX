package ink.meodinger.lpfx.io

import ink.meodinger.lpfx.FileType
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.type.TransFile.Companion.LPTransFile
import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.using

import java.io.*
import java.nio.charset.StandardCharsets


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: ink.meodinger.lpfx.io
 */

/**
 * Load TransFile
 */
@Throws(IOException::class)
fun export(file: File, type: FileType, transFile: TransFile) {
    when(type) {
        FileType.LPFile -> exportLP(file, transFile)
        FileType.MeoFile -> exportMeo(file, transFile)
    }
}

/**
 * Export TransFile as LP format
 */
@Throws(IOException::class)
private fun exportLP(file: File, transFile: TransFile) {

    // Group count validate
    if (transFile.groupCount > 9) throw IOException(I18N["exception.exporter.too_many_groups"])

    fun buildLabel(transLabel: TransLabel): String {
        return StringBuilder()
            .append(LPTransFile.LABEL_START).append(transLabel.index).append(LPTransFile.LABEL_END)
            .append(LPTransFile.PROP_START)
            .append(String.format("%.4f", transLabel.x)).append(LPTransFile.SPLIT)
            .append(String.format("%.4f", transLabel.y)).append(LPTransFile.SPLIT)
            .append(transLabel.groupId + 1)
            .append(LPTransFile.PROP_END).append("\n")
            .append(transLabel.text).append("\n")
            .toString()
    }

    fun buildPic(picName: String): String {
        val builder = StringBuilder()

        builder.append(LPTransFile.PIC_START).append(picName).append(LPTransFile.PIC_END).append("\n")
        for (label in transFile.getTransList(picName)) {
            builder.append(buildLabel(label)).append("\n")
        }
        builder.append("\n")

        return builder.toString()
    }

    fun exportVersion(): String {
        val vBuilder = StringBuilder()
        return vBuilder
            .append(transFile.version[0])
            .append(LPTransFile.SPLIT)
            .append(transFile.version[1])
            .toString()
    }

    fun exportGroup(): String {
        val gBuilder = StringBuilder()
        for (name in transFile.groupNames) gBuilder.append(name).append("\n")
        return gBuilder.deleteCharAt(gBuilder.length - 1).toString()
    }

    fun exportTranslation(): String {
        val tBuilder = StringBuilder()
        for (picName in transFile.sortedPicNames) tBuilder.append(buildPic(picName))
        return tBuilder.toString()
    }

    val builder = StringBuilder()
    val vString = exportVersion()
    val gString = exportGroup()
    val tString = exportTranslation()

    builder.append(vString).append("\n")
        .append(LPTransFile.SEPARATOR).append("\n")
        .append(gString).append("\n")
        .append(LPTransFile.SEPARATOR).append("\n")
        .append(transFile.comment).append("\n")
        .append("\n").append("\n")
        .append(tString)

    using {
        val fos = FileOutputStream(file).autoClose()
        val writer = BufferedWriter(OutputStreamWriter(fos, StandardCharsets.UTF_8)).autoClose()

        // write BOM (EF BB BF)
        fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
        // write content
        writer.write(builder.toString())
    } catch { e : IOException ->
        throw e
    } finally {

    }
}

/**
 * Export TransFile as MEO format
 */
@Throws(IOException::class)
private fun exportMeo(file: File, transFile: TransFile) {
    using {
        val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8)).autoClose()
        writer.write(transFile.toJsonString())
    } catch { e: IOException ->
        throw e
    } finally {

    }
}