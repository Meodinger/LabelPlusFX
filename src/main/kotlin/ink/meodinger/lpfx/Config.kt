package ink.meodinger.lpfx

import java.awt.SystemTray
import java.io.File


/**
 * Author: Meodinger
 * Date: 2022/3/23
 * Have fun with my code!
 */

/**
 * Global Config for current JVM Instance
 */
object Config {

    var enableJNI:   Boolean = true
    var enableProxy: Boolean = true
    val workingDir:  File    = File(System.getProperty("user.dir"))

    val supportSysTray: Boolean = SystemTray.isSupported()

}
