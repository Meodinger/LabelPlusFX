package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.Config
import ink.meodinger.lpfx.util.file.transfer

import java.io.File

/**
 * Author: Meodinger
 * Date: 2022/5/25
 * Have fun with my code!
 */


/**
 * App config file
 */
private val CFG_File: File = Config.workingDir.resolve("app/LabelPlusFX.cfg")
private val HW_File: File = Config.workingDir.resolve("app/LabelPlusFX-HW.cfg")
private val SW_File: File = Config.workingDir.resolve("app/LabelPlusFX-SW.cfg")

/**
 * Switch Prism to software, require restart the application.
 * **This function should only be used on Windows.**
 */
fun useSoftwarePrism() {
    transfer(SW_File, CFG_File)
}

/**
 * Switch Prism to hardware, require restart the application.
 * **This function should only be used on Windows.**
 */
fun useHardwarePrism() {
    transfer(HW_File, CFG_File)
}
