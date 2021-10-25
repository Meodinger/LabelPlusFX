package ink.meodinger.lpfx.util.resource

import ink.meodinger.lpfx.LabelPlusFX

import javafx.scene.image.Image
import java.io.InputStream
import java.net.URL
import java.util.*


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: ink.meodinger.lpfx.util
 */

/**
 * Load file in module as URL
 */
fun loadFileAsURL(fileName: String): URL {
    return LabelPlusFX::class.java.getResource(fileName)!!
}

/**
 * Load file in module as InputStream
 */
fun loadFileAsStream(fileName: String): InputStream {
    return LabelPlusFX::class.java.getResourceAsStream(fileName)!!
}

/**
 * Load file in module as ByteArray
 */
fun loadFileAsBytes(fileName: String): ByteArray {
    val inputStream = loadFileAsStream(fileName)
    val buffer = ByteArray(inputStream.available())
    inputStream.read(buffer)
    return buffer
}

/**
 * Load file in module as Image
 */
fun loadAsImage(imageFileName: String): Image {
    val imgUrl = loadFileAsURL(imageFileName)
    return Image(imgUrl.toString())
}

val ICON = loadAsImage("/image/icon.png")
val INIT_IMAGE = loadAsImage("/image/init_image.png")
val SAMPLE_IMAGE = loadAsImage("/image/sample_320x320.jpg")

val SCRIPT = loadFileAsBytes("/file/script/Meo_PS_Script")
val TEMPLATE_EN = loadFileAsBytes("/file/script/ps_script_res/en.psd")
val TEMPLATE_ZH = loadFileAsBytes("/file/script/ps_script_res/zh.psd")

val MONIKA_VOICE = loadFileAsURL("/file/audio/monika/voice.ogg")
val MONIKA_SONG = loadFileAsURL("/file/audio/monika/song.ogg")

val INFO = ResourceBundle.getBundle("ink.meodinger.lpfx.LabelPlusFX")!!
val I18N = ResourceBundle.getBundle("ink.meodinger.lpfx.Lang")!!
operator fun ResourceBundle.get(key: String): String = this.getString(key)