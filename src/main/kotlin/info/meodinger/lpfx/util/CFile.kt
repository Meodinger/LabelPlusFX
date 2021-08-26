package info.meodinger.lpfx.util.file

import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * Author: Meodinger
 * Date: 2021/8/24
 * Location: info.meodinger.lpfx.util
 */

/**
 * Transfer a File to another File
 */
@Throws(IOException::class)
fun transfer(from: File, to: File) {
    if (from.isDirectory || to.isDirectory) throw IOException(I18N["exception.io.cannot_transfer_directory"])

    val input = FileInputStream(from).channel
    val output = FileOutputStream(to).channel

    output.transferFrom(input, 0, input.size())

    input.close()
    output.close()
}