package info.meodinger.lpfx.io

import info.meodinger.lpfx.type.TransFile
import info.meodinger.lpfx.type.TransFile.Companion.getSortedPicList
import info.meodinger.lpfx.util.CZip
import info.meodinger.lpfx.util.dialog.showException
import info.meodinger.lpfx.util.dialog.showInfo
import info.meodinger.lpfx.util.resource.*

import java.io.File

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.io
 */
fun pack(target: String, source: String, transFile: TransFile) {
    try {
        val zip = CZip(target)

        zip.zip(SCRIPT, "Meo_PS_Script.jsx")
        zip.zip(TEMPLATE_ZH, "/ps_script_res/zh.psd")
        zip.zip(TEMPLATE_EN, "/ps_script_res/en.psd")

        zip.zip(transFile.toJsonString().toByteArray(), "/images/translation.json")

        for (picName in getSortedPicList(transFile)) {
            zip.zip(File("$source/$picName"), "/images/$picName")
        }

        zip.close()
        showInfo(I18N["info.exported_successful"])
    } catch (e: Exception) {
        showException(e)
    }
}