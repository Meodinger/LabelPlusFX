package ink.meodinger.lpfx.util.event

import javafx.scene.input.MouseEvent


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * Whether click count equals 2
 */
val MouseEvent.isDoubleClick: Boolean get() = clickCount == 2
