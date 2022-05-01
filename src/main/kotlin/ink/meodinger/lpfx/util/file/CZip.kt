package ink.meodinger.lpfx.util.file

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * Zip files easier
 */
class CZip @Throws(IOException::class) constructor(zipFile: File) {
    private val zip: ZipOutputStream

    init {
        zip = try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile)))
        } catch (e: IOException) {
            throw IOException("CZip initialization aborted").initCause(e)
        }
    }

    /**
     * Zip a [file] into [path]
     * @param file File to zip
     * @param path Path in the Zip file
     */
    @Throws(IOException::class)
    fun zip(file: File, path: String) {
        if (file.isDirectory) {
            val folder = "$path/"
            zip.putNextEntry(ZipEntry(folder))

            file.listFiles()?.forEach { zip(it, folder + it) }
        } else {
            zip(FileInputStream(file), path)
        }
    }

    /**
     * Zip a [inputStream] into [path]
     * @param inputStream Source stream to zip
     * @param path Path in the Zip file
     */
    @Throws(IOException::class)
    fun zip(inputStream: InputStream, path: String) {
        zip.putNextEntry(ZipEntry(path))

        val input = BufferedInputStream(inputStream)
        val chunk = ByteArray(1024 * 4)
        var len: Int
        while (input.read(chunk).also { len = it } != -1) {
            zip.write(chunk, 0, len)
        }
        input.close()

        zip.closeEntry()
    }

    /**
     * Zip some [bytes] into [path]
     * @param bytes ByteArray to zip
     * @param path Path in the Zip file
     */
    @Throws(IOException::class)
    fun zip(bytes: ByteArray, path: String) {
        zip.putNextEntry(ZipEntry(path))
        zip.write(bytes)
        zip.closeEntry()
    }

    /**
     * Close the zip stream, complete the zip procedure
     */
    @Throws(IOException::class)
    fun close() {
        zip.close()
    }
}
