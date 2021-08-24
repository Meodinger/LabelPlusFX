package info.meodinger.lpfx.util.image

import javafx.scene.image.Image

/**
 * Author: Meodinger
 * Date: 2021/8/2
 * Location: info.meodinger.lpfx.util
 */

/**
 * Scale by ratio
 */
fun Image.scale(scale: Double) = Image(this.url, this.width * scale, this.height * scale, false, true)

/**
 * Scale according to width
 */
fun Image.resizeByWidth(width: Double) = this.scale(width / this.width)

/**
 * Scale according to height
 */
fun Image.resizeByHeight(height: Double) = this.scale(height / this.height)

/**
 * Scale according to max radius
 */
fun Image.resizeByRadius(radius: Double) = this.scale((radius * 2) / Math.min(this.height, this.width))

/**
 * Resize image
 */
fun Image.resize(width: Double, height: Double) = Image(this.url, width, height, false, true)
