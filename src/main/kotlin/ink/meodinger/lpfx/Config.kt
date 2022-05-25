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

    /**
     * Whether current OS is Windows
     */
    val isWin: Boolean = OS.contains("win")
    /**
     * Whether current OS is macOS
     */
    val isMac: Boolean = OS.contains("mac")

    /**
     * Whether SystemProxy enabled
     */
    var enableProxy: Boolean = true
    /**
     * Whether JNI enabled
     */
    var enableJNI: Boolean = true

    /**
     * Monospace font. Used in CLabel/CLabelPane
     */
    val MonoFont: String = if (isWin) "Terminal" else if (isMac) "Monaco" else "Monospace"
    /**
     * Text font. Used in common text display
     */
    val TextFont: String = if (isWin) "SimSun" else if (isMac) "" else ""

    /**
     * Whether SystemTray is enabled. Used in BOSS key
     */
    val supportSysTray: Boolean = SystemTray.isSupported()

    /**
     * The working dir of current process
     */
    val workingDir: File = File(System.getProperty("user.dir"))

    /**
     * Whether IME assistance enabled
     */
    val enableIMEAssistance: Boolean get() = isWin && enableJNI

    /**
     * Whether using software prism
     */
    val usingSWPrism: Boolean = System.getProperty("prism.order")?.equals("sw") ?: false

}
