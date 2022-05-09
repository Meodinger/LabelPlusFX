@file:JvmName("IMEUtil")

package ink.meodinger.lpfx.ime

import javafx.stage.Stage

/**
 * Author: Meodinger
 * Date: 2022/3/22
 * Have fun with my code!
 */

private external fun getActiveWindow(): Long
private external fun getWindowHandle(title: String): Long

/**
 * Get the current working window
 * @return hWnd in Long
 */
fun getCurrentWindow(): Long = getActiveWindow()

/**
 * Get the Window Handle of the stage.
 * @param stage The stage whose hWnd will be got.
 * @return hWnd in Long
 */
fun getWindowHandle(stage: Stage): Long = getWindowHandle(stage.title)
