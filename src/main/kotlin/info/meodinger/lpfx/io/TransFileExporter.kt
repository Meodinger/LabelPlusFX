package info.meodinger.lpfx.io

import info.meodinger.lpfx.type.TransFile
import info.meodinger.lpfx.type.TransFile.Companion.LPTransFile
import info.meodinger.lpfx.type.TransFile.Companion.getSortedPicList
import info.meodinger.lpfx.type.TransLabel
import info.meodinger.lpfx.util.dialog.showInfo
import info.meodinger.lpfx.util.dialog.showAlert
import info.meodinger.lpfx.util.dialog.showException
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get
import info.meodinger.lpfx.util.using

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.BufferedWriter
import java.nio.charset.StandardCharsets

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.io
 */
fun exportLP(file: File, transFile: TransFile): Boolean {

    // Group count validate
    val groupCount = transFile.groupList.size
    if (groupCount > 9) {
        showAlert(I18N["alert.exporter.too_many_groups"])
        return false
    }

    fun buildLabel(transLabel: TransLabel): String {
        return StringBuilder()
            .append(LPTransFile.LABEL_START).append(transLabel.index).append(LPTransFile.LABEL_END)
            .append(LPTransFile.PROP_START)
            .append(transLabel.x).append(LPTransFile.SPLIT)
            .append(transLabel.y).append(LPTransFile.SPLIT)
            .append(transLabel.groupId + 1)
            .append(LPTransFile.PROP_END).append("\n")
            .append(transLabel.text).append("\n")
            .toString()
    }

    fun buildPic(picName: String): String {
        val builder = StringBuilder()

        builder.append(LPTransFile.PIC_START).append(picName).append(LPTransFile.PIC_END).append("\n")
        for (label in transFile.transMap[picName]!!) {
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
        for (g in transFile.groupList) {
            gBuilder.append(g.name).append("\n")
        }
        return gBuilder.deleteCharAt(gBuilder.length - 1).toString()
    }

    fun exportTranslation(): String {
        val tBuilder = StringBuilder()
        for (picName in getSortedPicList(transFile)) {
            tBuilder.append(buildPic(picName))
        }
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
    } catch { e : Exception ->
        showException(e)
        return false
    } finally {

    }

    return true
}

fun exportMeo(file: File, transFile: TransFile): Boolean {
    using {
        val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8)).autoClose()
        writer.write(transFile.toJsonString())
    } catch { e: Exception ->
        showException(e)
        return false
    } finally {

    }
    return true
}