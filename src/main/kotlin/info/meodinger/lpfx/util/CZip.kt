package info.meodinger.lpfx.util

import info.meodinger.lpfx.util.dialog.showException

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.util
 */

/**
 * Zip files easier
 */
class CZip(zipFile: File) {
    private val zip: ZipOutputStream

    init {
        zip = try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile)))
        } catch (e: IOException) {
            showException(e)
            throw RuntimeException("Abort")
        }
    }

    @Throws(IOException::class)
    fun zip(file: File, path: String) {
        if (file.isDirectory) {
            val folder = "$path/"
            zip.putNextEntry(ZipEntry(folder))

            file.listFiles()?.forEach { f -> zip(f, folder + f) }
        } else {
            zip(FileInputStream(file), path)
        }
    }

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

    @Throws(IOException::class)
    fun zip(bytes: ByteArray, path: String) {
        zip.putNextEntry(ZipEntry(path))
        zip.write(bytes)
        zip.closeEntry()
    }

    @Throws(IOException::class)
    fun close() {
        zip.close()
    }
}