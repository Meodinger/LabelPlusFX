package ink.meodinger.lpfx.util.platform

import java.util.*


/**
 * Author: Meodinger
 * Date: 2021/7/30
 * Location: ink.meodinger.lpfx.util
 */

/**
 * OS name
 */
private val OS: String = System.getProperty("os.name").lowercase(Locale.getDefault())

val isWin = OS.contains("win")
val isMac = OS.contains("mac")

val MonoFont = if (isWin) "Terminal" else if (isMac) "Monaco" else "Monospace"
val TextFont = if (isWin) "SimSun" else if (isMac) "" else ""