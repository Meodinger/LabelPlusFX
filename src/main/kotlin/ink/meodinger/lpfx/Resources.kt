package ink.meodinger.lpfx

import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.*
import javax.imageio.ImageIO


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * Load file in module as URL
 * @param path Resource path, start with a slash
 */
fun loadAsURL(path: String): URL = LabelPlusFX::class.java.getResource(path)!!

/**
 * Load file in module as InputStream
 * @param path Resource path, start with a slash
 */
fun loadAsStream(path: String): InputStream = LabelPlusFX::class.java.getResourceAsStream(path)!!

/**
 * Load file in module as ByteArray
 * @param path Resource path, start with a slash
 */
fun loadAsBytes(path: String): ByteArray = loadAsStream(path).readAllBytes()

/**
 * Load file in module as Image
 * @param path Resource path, start with a slash
 */
fun loadAsImage(path: String): Image = Image(loadAsURL(path).toString())

// region Images

/**
 * General Icon Radius
 */
const val GENERAL_ICON_RADIUS: Double = 32.0

/**
 * Icon Image for LPFX
 */
val ICON: Image by lazy { loadAsImage("/file/image/icon.png") }

/**
 * Image indicates CONFIRM
 */
val IMAGE_CONFIRM: Image by lazy { loadAsImage("/file/image/dialog/Confirm.png") }
/**
 * Image indicates INFORMATION
 */
val IMAGE_INFO   : Image by lazy { loadAsImage("/file/image/dialog/Info.png") }
/**
 * Image indicates WARNING
 */
val IMAGE_WARNING: Image by lazy { loadAsImage("/file/image/dialog/Warning.png") }
/**
 * Image indicates ERROR
 */
val IMAGE_ERROR  : Image by lazy { loadAsImage("/file/image/dialog/Error.png") }

/**
 * The initial dafault image for LabelPane. This image should not
 * larger than 480x480 to have best view.
 */
val INIT_IMAGE: Image by lazy {
    Config.workingDir.resolve("init-image.png")
        .takeIf(File::exists)?.let {
            Image(it.toURI().toURL().toString()).takeUnless(Image::isError)
                ?: ImageIO.read(it)?.let { image -> SwingFXUtils.toFXImage(image, null) }
        } ?: loadAsImage("/file/image/init-image.png")
}

/**
 * Sample Image for label propertis preview
 */
val SAMPLE_IMAGE: Image by lazy { loadAsImage("/file/image/sample-320x320.jpg") }

// endregion

// region Files

/**
 * The PS-Script
 */
val SCRIPT     : ByteArray by lazy { loadAsBytes("/file/script/Meo_PS_Script") }
/**
 * EN Template fot PS-Script
 */
val TEMPLATE_EN: ByteArray by lazy { loadAsBytes("/file/script/ps_script_res/en.psd") }
/**
 * ZH Template fot PS-Script
 */
val TEMPLATE_ZH: ByteArray by lazy { loadAsBytes("/file/script/ps_script_res/zh.psd") }

// endregion

// region I18N

/**
 * Language
 */
val lang: Locale = when (Locale.getDefault().country) {
    "CN"             -> Locale.SIMPLIFIED_CHINESE  // zh_CN
    "HK", "MO", "TW" -> Locale.TRADITIONAL_CHINESE // zh_TW
    else -> Locale.ENGLISH
}

/**
 * General information of LPFX
 */
val INFO = ResourceBundle.getBundle("ink.meodinger.lpfx.LabelPlusFX")!!
/**
 * I18N for LPFX
 */
val I18N = ResourceBundle.getBundle("ink.meodinger.lpfx.Lang", lang)!!

/**
 * Provide map-like getter
 */
operator fun ResourceBundle.get(key: String): String = this.getString(key)

// endregion
