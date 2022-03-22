@file:JvmName("IMEUtil")

package ink.meodinger.lpfx.util.ime

import javafx.stage.Stage

/**
 * Author: Meodinger
 * Date: 2022/3/22
 * Have fun with my code!
 */

/**
 *
 */
private external fun getWindowHandle(title: String): Long

fun getWindowHandle(stage: Stage): Long = getWindowHandle(stage.title)
