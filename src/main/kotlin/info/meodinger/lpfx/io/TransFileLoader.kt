package info.meodinger.lpfx.io

import info.meodinger.lpfx.FileType
import info.meodinger.lpfx.type.TransFile
import info.meodinger.lpfx.type.TransFile.Companion.LPTransFile
import info.meodinger.lpfx.type.TransGroup
import info.meodinger.lpfx.type.TransLabel
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.*
import java.nio.charset.StandardCharsets


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.io
 */


/**
 * Load TransFile
 */
@Throws(IOException::class)
fun load(file: File, type: FileType): TransFile {
    return when(type) {
        FileType.LPFile -> loadLP(file)
        FileType.MeoFile -> loadMeo(file)
    }
}

/**
 * Load LP File
 */
@Throws(IOException::class)
private fun loadLP(file: File): TransFile {
    val reader = BufferedReader(InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8))

    // Remove BOM (EF BB BF)
    val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
    val buf = ByteArray(3)
    val fis = FileInputStream(file)
    val len = fis.read(buf, 0, 3)
    if (len != 3) throw IOException(I18N["exception.loader.unexpected_eof"])
    if (bom.contentEquals(buf)) reader.read(CharArray(3), 0, 1)

    val lines = reader.readLines()
    val size = lines.size

    var index = 0
    var pointer = 0

    /**
     * Parse from current line, leave pointer on mark
     */
    fun parseText(vararg marks: String): String {
        val builder = StringBuilder()

        while (pointer < size) {
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
    fun parseTranLabel(): TransLabel {
        val s = lines[pointer].split(LPTransFile.LABEL_END)
        val props = s[1].replace(LPTransFile.PROP_START, "").replace(LPTransFile.PROP_END, "").split(LPTransFile.SPLIT)

        /*
           Re-arrange label index when loading, ignore file data
           Line  54: index define
           Line 100: index inc
           Line 169: index reset
         */

        // val index = s[0].replace(LPTransFile.LABEL_START, "").trim().toInt()
        val x = props[0].trim().toDouble()
        val y = props[1].trim().toDouble()
        val groupId = props[2].trim().toInt() - 1

        // if (index < 0) throw IOException(String.format(I18N["exception.loader.invalid_index.format.i"], index))

        pointer++
        return TransLabel(++index, groupId, x, y, parseText(LPTransFile.PIC_START, LPTransFile.LABEL_START))
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
        val transLabels = ArrayList<TransLabel>()

        while (pointer < size && lines[pointer].startsWith(LPTransFile.LABEL_START)) {
            val label = parseTranLabel()

            for (l in transLabels) {
                if (l.index == label.index) {
                    throw IOException(String.format(I18N["exception.loader.repeated_index.format.i"], label.index))
                }
            }
            transLabels.add(label)
        }

        // move to next pic
        while (pointer < size && !lines[pointer].startsWith(LPTransFile.PIC_START)) pointer++

        return transLabels
    }

    // Version
    val v = lines[pointer].split(LPTransFile.SPLIT)
    val version = intArrayOf(v[0].trim().toInt(), v[1].trim().toInt())
    pointer++

    // Separator
    pointer++

    // Group Info and Separator
    var groupCount = 1
    val groupList = ArrayList<TransGroup>()
    while (lines[pointer] != LPTransFile.SEPARATOR && groupCount < 10) {
        if (lines[pointer].isBlank()) throw IOException(I18N["exception.loader.empty_group_name"])

        val group = TransGroup(lines[pointer], LPTransFile.DEFAULT_COLOR_LIST[groupCount - 1])

        groupList.forEach {
            if (it.name == group.name) throw IOException(String.format(I18N["exception.loader.repeated_group_name.format.s"], group.name))
        }
        groupList.add(group)

        groupCount++
        pointer++
    }
    if (lines[pointer] != LPTransFile.SEPARATOR) throw IOException(I18N["exception.exporter.too_many_groups"])
    pointer++

    // Comment
    val comment = parseText(LPTransFile.PIC_START)

    // Content
    val transMap = HashMap<String, MutableList<TransLabel>>()
    while (pointer < size && lines[pointer].startsWith(LPTransFile.PIC_START)) {
        index = 0

        // Parse order：
        //  - Kotlin     -> head body
        //  - JavaScript -> body head
        // transMap[parsePicHead()] = parsePicBody()

        val picName = parsePicHead()
        val labels = parsePicBody()

        transMap[picName] = labels
    }

    return TransFile(version, comment, groupList, transMap)
}

/**
 * Load MEO File
 */
@Throws(IOException::class)
private fun loadMeo(file: File): TransFile {
    val mapper = ObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
    val json = mapper.readTree(file)

    return mapper.treeToValue(json, TransFile::class.java)
}