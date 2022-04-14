package ink.meodinger.lpfx.util.file

import ink.meodinger.lpfx.I18N
import ink.meodinger.lpfx.get

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
fun transfer(ori: File, dst: File, overwrite: Boolean = true) {
    if (ori.notExists()) throw IOException(String.format(I18N["util.io.file_not_exists.s"], ori))
    if (ori.isDirectory || dst.isDirectory) throw IOException(I18N["util.io.cannot_transfer_directory"])
    if (!overwrite && dst.exists()) throw IOException(String.format(I18N["util.io.overwrite_disable.s"], dst))

    val input = FileInputStream(ori).channel
    val output = FileOutputStream(dst).channel

    output.transferFrom(input, 0, input.size())

    input.close()
    output.close()
}

/**
 * Whether this file exists. `null` is treat as not exist.
 */
@Throws(SecurityException::class)
fun File?.exists(): Boolean = this != null && exists()

/**
 * Whether this file not null and exists. If not, return null
 */
@Throws(SecurityException::class)
fun File?.existsOrNull(): File? = takeIf(File?::exists)

/**
 * Whether this file not null and exists. If not, return default
 */
@Throws(SecurityException::class)
fun File?.existsOrElse(default: File): File = existsOrNull() ?: default

/**
 * Alias for !File?.exists()
 */
@Throws(SecurityException::class)
fun File?.notExists(): Boolean = !exists()
