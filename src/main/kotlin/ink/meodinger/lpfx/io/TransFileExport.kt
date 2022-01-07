package ink.meodinger.lpfx.io

import ink.meodinger.lpfx.FileType
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.type.TransFile.Companion.LPTransFile
import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.doNothing
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.using

import java.io.*
import java.nio.charset.StandardCharsets


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * Export TransFile content String in target FileType
 */
@Throws(IOException::class)
fun exportAsString(transFile: TransFile, targetType: FileType): String {

    /**
     * Build LPFile content
     * @param transFile Source TransFile
     * @return LPFile content in String
     */
    fun buildLPFile(transFile: TransFile): String {
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

        val vString = exportVersion()
        val gString = exportGroup()
        val tString = exportTranslation()

        return StringBuilder()
            .appendLine(vString)
            .appendLine(LPTransFile.SEPARATOR)
            .appendLine(gString)
            .appendLine(LPTransFile.SEPARATOR)
            .appendLine(transFile.comment)
            .appendLine("\n")
            .appendLine(tString)
            .toString()
    }

    return when(targetType) {
        FileType.LPFile  -> buildLPFile(transFile)
        FileType.MeoFile -> transFile.toJsonString()
    }
}

/**
 * Load TransFile
 */
@Throws(IOException::class)
fun export(file: File, type: FileType, transFile: TransFile) {
    when(type) {
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
        // UTF-8 BOM (EF BB BF)
        val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        val text = exportAsString(transFile, FileType.LPFile)

        val fos = FileOutputStream(file).autoClose()
        val writer = BufferedWriter(OutputStreamWriter(fos, StandardCharsets.UTF_8)).autoClose()

        fos.write(bom)
        writer.write(text)
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
        BufferedWriter(OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8)).autoClose()
            .write(exportAsString(transFile, FileType.MeoFile))
    } catch { e: IOException ->
        throw e
    } finally ::doNothing
}
