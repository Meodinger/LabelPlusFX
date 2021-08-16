package info.meodinger.lpfx.util.platform

import java.util.*

/**
 * Author: Meodinger
 * Date: 2021/7/30
 * Location: info.meodinger.lpfx.util
 */
private val OS: String = System.getProperty("os.name").lowercase(Locale.getDefault())

val isWin = OS.contains("win")
val isMac = OS.contains("mac")

val MonoType = if (isWin) "Terminal" else if (isMac) "Monaco" else "Monospace"