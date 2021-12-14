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
 * @param from File that transfer from
 * @param to   File that transfer to
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
