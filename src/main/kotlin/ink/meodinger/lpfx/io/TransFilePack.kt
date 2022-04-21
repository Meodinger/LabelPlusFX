package ink.meodinger.lpfx.io

import ink.meodinger.lpfx.FileType
import ink.meodinger.lpfx.SCRIPT
import ink.meodinger.lpfx.TEMPLATE_EN
import ink.meodinger.lpfx.TEMPLATE_ZH
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.util.file.CZip

import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * Export a pic pack with Meo_PS_Script
 */
@Throws(IOException::class)
fun pack(target: File, transFile: TransFile) {
    val zip = CZip(target)

    zip.zip(SCRIPT, "Meo_PS_Script.jsx")
    zip.zip(TEMPLATE_ZH, "/ps_script_res/zh.psd")
    zip.zip(TEMPLATE_EN, "/ps_script_res/en.psd")

    val content = exportAsString(transFile, FileType.getFileType(target))
    zip.zip(content.toByteArray(StandardCharsets.UTF_8), "/images/translation.${target.extension}")

    for (picName in transFile.sortedPicNamesObservable) zip.zip(transFile.getFile(picName)!!, "/images/$picName")

    zip.close()
}
