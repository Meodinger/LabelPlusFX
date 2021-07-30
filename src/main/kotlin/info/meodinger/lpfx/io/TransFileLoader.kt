package info.meodinger.lpfx.io

import com.fasterxml.jackson.databind.ObjectMapper
import info.meodinger.lpfx.type.TransFile
import info.meodinger.lpfx.type.TransFile.Companion.LPTransFile
import info.meodinger.lpfx.type.TransFile.Companion.MeoTransFile
import info.meodinger.lpfx.type.TransGroup
import info.meodinger.lpfx.type.TransLabel

import java.io.*
import java.nio.charset.StandardCharsets

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.io
 */
@Throws(IOException::class)
fun loadLP(file: File): TransFile {

    val transFile = TransFile()
    val reader = BufferedReader(InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8))

    // Remove BOM (EF BB BF)
    val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
    val buf = ByteArray(3)
    val fis = FileInputStream(file)
    val len = fis.read(buf, 0, 3)
    if (len != 3) throw IOException("Unexpected EOF")
    if (bom.contentEquals(buf)) reader.read(CharArray(3), 0, 1)

    val lines = reader.readLines()
    val size = lines.size

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

        val index = s[0].replace(LPTransFile.LABEL_START, "").trim().toInt()
        val x = props[0].trim().toDouble()
        val y = props[1].trim().toDouble()
        val groupId = props[2].trim().toInt() - 1

        if (index < 0) throw IOException("invalid index")

        pointer++
        return TransLabel(index, x, y, groupId, parseText(LPTransFile.PIC_START, LPTransFile.LABEL_START))
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
    fun parsePicBody(): ArrayList<TransLabel> {
        val transLabels = ArrayList<TransLabel>()

        while (pointer < size && lines[pointer].startsWith(LPTransFile.LABEL_START)) {
            val label = parseTranLabel()

            for (l in transLabels) {
                if (l.index == label.index) {
                    throw IOException("index repeated")
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
    transFile.version = intArrayOf(v[0].trim().toInt(), v[1].trim().toInt())
    pointer++

    // Separator
    pointer++

    // Group Info and Separator
    var count = 1
    while (lines[pointer] != LPTransFile.SEPARATOR && count < 10) {
        val group = TransGroup(lines[pointer], MeoTransFile.DEFAULT_COLOR_LIST[count - 1])
        transFile.groupList.forEach {
            // todo: rename
            if (it.name == group.name) throw IOException("Same Group Name")
        }
        transFile.groupList.add(group)
        count++
        pointer++
    }
    if (lines[pointer] != LPTransFile.SEPARATOR) throw IOException("Too Many Groups")
    pointer++

    // Comment
    transFile.comment = parseText(LPTransFile.PIC_START)

    // Content
    while (pointer < size && lines[pointer].startsWith(LPTransFile.PIC_START)) {
        transFile.transMap[parsePicHead()] = parsePicBody()
    }

    return transFile
}

@Throws(IOException::class)
fun loadMeo(file: File): TransFile {
    return ObjectMapper().readValue(
        BufferedInputStream(FileInputStream(file)),
        TransFile::class.java
    )
}