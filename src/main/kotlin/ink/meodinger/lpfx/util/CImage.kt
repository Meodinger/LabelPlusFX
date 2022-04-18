package ink.meodinger.lpfx.util.image

import ink.meodinger.lpfx.*

import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import java.io.File
import java.io.FileInputStream
import java.io.IOException
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
fun Image.scale(ratio: Double) = Image(this.url, this.width * ratio, this.height * ratio, false, true)

/**
 * Scale according to width
 * @param width Width that the image width should scale to
 * @return scaled Image
 */
fun Image.resizeByWidth(width: Double) = scale(width / this.width)

/**
 * Scale according to height
 * @param height Height that the image height should scale to
 * @return scaled Image
 */
fun Image.resizeByHeight(height: Double) = scale(height / this.height)

/**
 * Scale according to max radius
 * @param radius Radius that the image radius should scale to
 * @return scaled Image
 */
fun Image.resizeByRadius(radius: Double) = scale((radius * 2) / this.height.coerceAtMost(this.width))

/**
 * Resize image
 * @param width Width that the image should scale to
 * @param height Height that the image should scale to
 */
fun Image.resize(width: Double, height: Double) = Image(this.url, width, height, false, true)

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

/**
 * Load an image from a file. Default uses JFX::Image, if file type
 * not supported by JFX, use ImageIO instead.
 * @return null if load failed
 * @throws IOException if ImageIO load failed
 */
@Throws(IOException::class)
fun imageFromFile(file: File): Image {
    return when (file.extension) {
        EXTENSION_PIC_PNG, EXTENSION_PIC_JPG, EXTENSION_PIC_JPEG, EXTENSION_PIC_GIF, EXTENSION_PIC_BMP -> {
            // JFX supported image types
            Image(file.toURI().toURL().toString())
        }
        else -> {
            // JFX not support, use ImageIO.
            ImageIO.read(FileInputStream(file))?.let {
                WritableImage(it.width, it.height).apply { SwingFXUtils.toFXImage(it, this) }
            } ?: throw IOException(I18N["util.image.unsupported"])
        }
    }
}
