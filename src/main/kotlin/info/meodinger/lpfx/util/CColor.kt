package info.meodinger.lpfx.util.color

import javafx.scene.paint.Color

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.util
 */
fun toHex(color: Color): String {
    return color.toString().substring(2, 8).uppercase()
}
