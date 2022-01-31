package ink.meodinger.lpfx.util.file

import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


/**
 * Author: Meodinger
 * Date: 2021/8/24
 * Have fun with my code!
 */

/**
 * Transfer a File to another File
 * @param ori File that transfer from
 * @param dst File that transfer to
 */
@Throws(IOException::class)
fun transfer(ori: File, dst: File) {
    if (!ori.exists() || !dst.exists()) throw IOException(I18N["exception.io.file_not_exists.s"])
    if (ori.isDirectory || dst.isDirectory) throw IOException(I18N["exception.io.cannot_transfer_directory"])

    val input = FileInputStream(ori).channel
    val output = FileOutputStream(dst).channel

    output.transferFrom(input, 0, input.size())

    input.close()
    output.close()
}

/**
 * Whether this file exists. If not, return null
 */
@Throws(IOException::class)
fun File?.existsOrNull(): File? = takeIf { this != null && this.exists() }

/**
 * Whether this file exists. If not, return default
 */
@Throws(IOException::class)
fun File?.existsOrElse(default: File): File = existsOrNull() ?: default

