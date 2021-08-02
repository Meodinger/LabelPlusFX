package info.meodinger.lpfx.util

import javafx.scene.image.Image

/**
 * Author: Meodinger
 * Date: 2021/8/2
 * Location: info.meodinger.lpfx.util
 */

fun Image.resize(width: Double, height: Double) = Image(this.url, width, height, false, true)

fun Image.scale(scale: Double) = Image(this.url, this.width * scale, this.height * scale, false, true)