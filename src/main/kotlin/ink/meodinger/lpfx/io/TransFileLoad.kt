package ink.meodinger.lpfx.io

import ink.meodinger.lpfx.I18N
import ink.meodinger.lpfx.FileType
import ink.meodinger.lpfx.get
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.type.TransFile.Companion.LPTransFile
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.string.isMathematicalNatural

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.*
import java.nio.charset.StandardCharsets


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */


/**
 * Load TransFile
 */
@Throws(IOException::class)
fun load(file: File, type: FileType): TransFile {
    return try {
        when (type) {
            FileType.LPFile -> loadLP(file)
            FileType.MeoFile -> loadMeo(file)
        }
    } catch (e: IOException) {
        try {
            // Try load as another file type
            when (type) {
                FileType.LPFile -> loadMeo(file)
                FileType.MeoFile -> loadLP(file)
            }
        } catch (_: Throwable) {
            // Load as another file type failed, but we don't care
            // actually what problem occurred. Throw the original
            // exception just like we never tried.
            throw e
        }
    }
}

/**
 * Load LP File
 */
@Throws(IOException::class)
private fun loadLP(file: File): TransFile {
    // Raw FIS doesn't support mark/reset
    val stream = BufferedInputStream(FileInputStream(file)).apply { mark(3) }
    val reader = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8))

    // Remove BOM (EF BB BF as \uFEFF)
    val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
    val buf = ByteArray(3)
    val len = stream.read(buf, 0, 3)
    if (len != 3) throw IOException(I18N["exception.loader.unexpected_eof"])
    if (!bom.contentEquals(buf)) stream.reset()

    val lines = reader.readLines()
    val lineCount = lines.size

    var pointer = 0

    /**
     * Parse from current line, leave pointer on mark
     */
    fun parseText(vararg marks: String): String {
        val builder = StringBuilder()

        while (pointer < lineCount) {
            for (mark in marks) {
                if (lines[pointer].startsWith(mark)) {
                    // return when read stop mark
                    return builder.toString().replace(Regex("\n+"), "\n").trim()
                }
            }
            builder.append(lines[pointer]).append("\n")
            pointer++
        }

        // return when eof
        return builder.toString().replace(Regex("\n+"), "\n").trim()
    }

    /**
     * Parse from current line, leave pointer on next pic/label
     */
    fun parseTranLabel(index: Int): TransLabel {
        val s = lines[pointer].split(LPTransFile.LABEL_END)
        val props = s[1].replace(LPTransFile.PROP_START, "").replace(LPTransFile.PROP_END, "").split(LPTransFile.SPLIT)

        // Re-arrange label index when loading, ignore file data
        // val index = s[0].replace(LPTransFile.LABEL_START, "").trim().toInt()
        val x = props[0].trim().toDouble()
        val y = props[1].trim().toDouble()
        val groupId = props[2].trim().toInt() - 1

        // if (index < 0) throw IOException(String.format(I18N["exception.loader.invalid_index.format.i"], index))

        pointer++
        return TransLabel(index, groupId, x, y, parseText(LPTransFile.PIC_START, LPTransFile.LABEL_START))
    }

    /**
     * Parse from current line, leave pointer on label/empty
     */
    fun parsePicHead(): String {
        val picName = lines[pointer].replace(LPTransFile.PIC_START, "").replace(LPTransFile.PIC_END, "")
        pointer++
        return picName
    }

    /**
     * Parse from current line, leave pointer on next pic
     */
    fun parsePicBody(): MutableList<TransLabel> {
        var index = 0
        val transLabels = ArrayList<TransLabel>()

        while (pointer < lineCount && lines[pointer].startsWith(LPTransFile.LABEL_START)) {
            val label = parseTranLabel(++index)

            for (l in transLabels) {
                if (l.index == label.index) {
                    throw IOException(String.format(I18N["exception.loader.repeated_index.i"], label.index))
                }
            }
            transLabels.add(label)
        }

        // move to next pic
        while (pointer < lineCount && !lines[pointer].startsWith(LPTransFile.PIC_START)) pointer++

        return transLabels
    }

    // Version
    val v = lines[pointer].split(LPTransFile.SPLIT).apply {
        if (size != 2) throw IOException(String.format(I18N["exception.loader.invalid_version_head.s"], lines[pointer]))
        forEach { v -> if (!v.isMathematicalNatural()) throw IOException(String.format(I18N["exception.loader.invalid_version_text.s"], v)) }
    }
    val version = listOf(v[0].trim().toInt(), v[1].trim().toInt())
    pointer++

    // Separator
    pointer++

    // Group Info and Separator
    var groupId = 0
    val groupList = ArrayList<TransGroup>()
    while (lines[pointer] != LPTransFile.SEPARATOR && groupId < 9) {
        if (lines[pointer].isBlank()) throw IOException(I18N["exception.loader.empty_group_name"])

        val groupName = lines[pointer]
        val groupColor = LPTransFile.DEFAULT_COLOR_HEX_LIST[groupId]

        if (groupList.any { it.name == groupName }) throw IOException(String.format(I18N["exception.loader.repeated_group_name.s"], groupName))

        groupList.add(TransGroup(groupName, groupColor))

        groupId++
        pointer++
    }
    if (lines[pointer] != LPTransFile.SEPARATOR) throw IOException(I18N["exception.exporter.too_many_groups"])
    pointer++

    // Comment
    val comment = parseText(LPTransFile.PIC_START)

    // Content
    val transMap = HashMap<String, MutableList<TransLabel>>()
    while (pointer < lineCount && lines[pointer].startsWith(LPTransFile.PIC_START)) {
        val picName = parsePicHead()
        val labels = parsePicBody()

        // DO NOT USE `transMap[parsePicHead()] = parsePicBody()`
        // Parse order：
        //  - Kotlin     -> head body
        //  - JavaScript -> body head

        transMap[picName] = labels
    }

    return TransFile(version, comment, groupList, transMap)
}

/**
 * Load MEO File
 */
@Throws(IOException::class)
private fun loadMeo(file: File): TransFile {
    return ObjectMapper()
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .readValue(file, TransFile::class.java)
}
