package ink.meodinger.lpfx

import ink.meodinger.lpfx.util.image.imageFromFile

import javafx.scene.image.Image
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.*


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * Load file in module as URL
 */
fun loadAsURL(fileName: String): URL = LabelPlusFX::class.java.getResource(fileName)!!

/**
 * Load file in module as InputStream
 */
fun loadAsStream(fileName: String): InputStream = LabelPlusFX::class.java.getResourceAsStream(fileName)!!

/**
 * Load file in module as ByteArray
 */
fun loadAsBytes(fileName: String): ByteArray = loadAsStream(fileName).readAllBytes()

/**
 * Load file in module as Image
 */
fun loadAsImage(imageFileName: String): Image = Image(loadAsURL(imageFileName).toString())

val ICON         = loadAsImage("/file/image/icon.png")
val SAMPLE_IMAGE = loadAsImage("/file/image/sample_320x320.jpg")

// NOTE: Should not larger than 480x480
val INIT_IMAGE   = Config.workingDir.resolve("init-image.png").takeIf(File::exists)?.let(::imageFromFile)
    ?: loadAsImage("/file/image/init_image.png")

val SCRIPT      = loadAsBytes("/file/script/Meo_PS_Script")
val TEMPLATE_EN = loadAsBytes("/file/script/ps_script_res/en.psd")
val TEMPLATE_ZH = loadAsBytes("/file/script/ps_script_res/zh.psd")

/**
 * Language
 */
val lang: Locale = when (Locale.getDefault().country) {
    "CN"             -> Locale.SIMPLIFIED_CHINESE  // zh_CN
    "HK", "MO", "TW" -> Locale.TRADITIONAL_CHINESE // zh_TW
    else -> Locale.ENGLISH
}

val INFO = ResourceBundle.getBundle("ink.meodinger.lpfx.LabelPlusFX")!!
val I18N = ResourceBundle.getBundle("ink.meodinger.lpfx.Lang", lang)!!
operator fun ResourceBundle.get(key: String): String = this.getString(key)
