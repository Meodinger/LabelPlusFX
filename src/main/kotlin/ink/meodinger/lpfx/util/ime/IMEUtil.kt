@file:JvmName("IMEUtil")

package ink.meodinger.lpfx.util.ime

import javafx.stage.Stage

/**
 * Author: Meodinger
 * Date: 2022/3/22
 * Have fun with my code!
 */

private external fun getWindowHandle(title: String): Long

/**
 * Get the Window Handle of the stage
 * @param stage The window whose HWND will be got.
 */
fun getWindowHandle(stage: Stage): Long = getWindowHandle(stage.title)
