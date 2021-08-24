package info.meodinger.lpfx.util.image

import javafx.scene.image.Image

/**
 * Author: Meodinger
 * Date: 2021/8/2
 * Location: info.meodinger.lpfx.util
 */
fun Image.scale(scale: Double) = Image(this.url, this.width * scale, this.height * scale, false, true)

fun Image.resizeByWidth(width: Double) = this.scale(width / this.width)

fun Image.resizeByHeight(height: Double) = this.scale(height / this.height)

fun Image.resizeByRadius(radius: Double) = this.scale(radius / Math.min(this.height, this.width))

fun Image.resize(width: Double, height: Double) = Image(this.url, width, height, false, true)
