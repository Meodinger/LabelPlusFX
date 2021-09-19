package info.meodinger.lpfx.util.image

import javafx.scene.image.Image


/**
 * Author: Meodinger
 * Date: 2021/8/2
 * Location: info.meodinger.lpfx.util
 */

/**
 * Scale by ratio
 *
 * @param ratio Scale ratio
 */
fun Image.scale(ratio: Double) = Image(this.url, this.width * ratio, this.height * ratio, false, true)

/**
 * Scale according to width
 *
 * @param width Width that the image width should scale to
 */
fun Image.resizeByWidth(width: Double) = this.scale(width / this.width)

/**
 * Scale according to height
 *
 * @param height Height that the image height should scale to
 */
fun Image.resizeByHeight(height: Double) = this.scale(height / this.height)

/**
 * Scale according to max radius
 *
 * @param radius Radius that the image radius should scale to
 */
fun Image.resizeByRadius(radius: Double) = this.scale((radius * 2) / Math.min(this.height, this.width))

/**
 * Resize image
 *
 * @param width Width that the image should scale to
 * @param height Height that the image should scale to
 */
fun Image.resize(width: Double, height: Double) = Image(this.url, width, height, false, true)
