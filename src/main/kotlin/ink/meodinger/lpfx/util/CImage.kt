package ink.meodinger.lpfx.util.image

import ink.meodinger.lpfx.*

import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import java.io.IOException
import java.net.URL
import javax.imageio.ImageIO


/**
 * Author: Meodinger
 * Date: 2021/8/2
 * Have fun with my code!
 */

/**
 * Scale by ratio
 * @param ratio Scale ratio
 * @return scaled Image
 */
fun Image.scale(ratio: Double): Image = Image(this.url, this.width * ratio, this.height * ratio, false, true)

/**
 * Scale according to width
 * @param width Width that the image width should scale to
 * @return scaled Image
 */
fun Image.resizeByWidth(width: Double): Image = scale(width / this.width)

/**
 * Scale according to height
 * @param height Height that the image height should scale to
 * @return scaled Image
 */
fun Image.resizeByHeight(height: Double): Image = scale(height / this.height)

/**
 * Scale according to max radius
 * @param radius Radius that the image radius should scale to
 * @return scaled Image
 */
fun Image.resizeByRadius(radius: Double): Image = scale((radius * 2) / this.height.coerceAtMost(this.width))

/**
 * Resize image
 * @param width Width that the image should scale to
 * @param height Height that the image should scale to
 */
fun Image.resize(width: Double, height: Double): Image = Image(this.url, width, height, false, true)

/**
 * To greyscale image
 * @return CANNOT BE SCALE
 */
fun Image.toGreyScale(): Image {
    val width = this.width.toInt()
    val height = this.height.toInt()
    val grayImage = WritableImage(width, height)

    val reader = this.pixelReader
    val writer = grayImage.pixelWriter
    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = reader.getArgb(x, y)

            val alpha = pixel shr 24 and 0xff
            val red   = pixel shr 16 and 0xff
            val green = pixel shr  8 and 0xff
            val blue  = pixel        and 0xff

            val grayLevel = (0.2162 * red + 0.7152 * green + 0.0722 * blue).toInt()
            val gray = (alpha shl 24) + (grayLevel shl 16) + (grayLevel shl 8) + grayLevel

            writer.setArgb(x, y, gray)
        }
    }
    return grayImage
}
