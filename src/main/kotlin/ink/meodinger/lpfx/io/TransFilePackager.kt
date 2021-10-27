package ink.meodinger.lpfx.io

import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.util.CZip
import ink.meodinger.lpfx.util.resource.*

import java.io.File
import java.io.IOException


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: ink.meodinger.lpfx.io
 */

/**
 * Export a pic pack with Meo_PS_Script
 */
@Throws(IOException::class)
fun pack(target: File, source: String, transFile: TransFile) {
    val zip = CZip(target)

    zip.zip(SCRIPT, "Meo_PS_Script.jsx")
    zip.zip(TEMPLATE_ZH, "/ps_script_res/zh.psd")
    zip.zip(TEMPLATE_EN, "/ps_script_res/en.psd")

    zip.zip(transFile.toJsonString().toByteArray(), "/images/translation.json")

    for (picName in transFile.sortedPicNames) {
        zip.zip(File("$source/$picName"), "/images/$picName")
    }

    zip.close()
}