package info.meodinger.lpfx.util.resource

import info.meodinger.lpfx.LabelPlusFX

import javafx.scene.image.Image
import java.util.*

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.util
 */
fun loadImage(imgName: String): Image {
    val imgUrl = LabelPlusFX::class.java.getResource(imgName)!!
    return Image(imgUrl.toString())
}

fun loadFile(fileName: String): ByteArray {
    val inputStream = LabelPlusFX::class.java.getResourceAsStream(fileName)!!
    val buffer = ByteArray(inputStream.available())
    inputStream.read(buffer)
    return buffer
}

val ICON = loadImage("/image/icon.png")

val SCRIPT = loadFile("/file/Meo_PS_Script")
val TEMPLATE_EN = loadFile("/file/en.psd")
val TEMPLATE_ZH = loadFile("/file/zh.psd")

val I18N = ResourceBundle.getBundle("info.meodinger.lpfx.Lang")!!
operator fun ResourceBundle.get(key: String): String = this.getString(key)