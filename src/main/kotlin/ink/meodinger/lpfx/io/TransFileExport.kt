package ink.meodinger.lpfx.io

import ink.meodinger.lpfx.FileType
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.util.doNothing
import ink.meodinger.lpfx.util.using

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
