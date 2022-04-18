package ink.meodinger.lpfx

import java.awt.SystemTray
import java.io.File
import java.util.*


/**
 * Author: Meodinger
 * Date: 2022/3/23
 * Have fun with my code!
 */

/**
 * Global Config for current JVM Instance
 */
object Config {
    /**
     * OS name
     */
    private val OS: String = System.getProperty("os.name").lowercase(Locale.getDefault())

    val isWin = OS.contains("win")
    val isMac = OS.contains("mac")

    val MonoFont = if (isWin) "Terminal" else if (isMac) "Monaco" else "Monospace"
    val TextFont = if (isWin) "SimSun" else if (isMac) "" else ""

    var enableJNI:   Boolean = true
    var enableProxy: Boolean = true
    val workingDir:  File    = File(System.getProperty("user.dir"))

    val supportSysTray: Boolean = SystemTray.isSupported()

}
