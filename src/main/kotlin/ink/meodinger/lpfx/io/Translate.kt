package ink.meodinger.lpfx.util.translator

import com.fasterxml.jackson.databind.ObjectMapper
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Settings
import java.io.IOException
import java.net.*
import java.nio.charset.Charset
import java.security.MessageDigest
import kotlin.math.floor


/**
 * Author: Meodinger
 * Date: 2022/2/8
 * Have fun with my code!
 */

/**
 * Tool ROOT
 */
private const val ROOT = "https://fanyi-api.baidu.com/api/trans/vip/translate"
private const val KEY  = "Wo9lvMK4qjxpLVrFktt3" // 200M words per month
private const val ID   = 20220208001077250

private val utf8Charset = Charset.forName("UTF-8")
private val md5Instance = MessageDigest.getInstance("MD5")
private fun md5(text: String): String {
    return StringBuilder().apply {
        for (byte in md5Instance.digest(text.toByteArray(utf8Charset)))
            append((byte.toInt() and 0xFF).toString(16).padStart(2, '0'))
    }.toString()
}
private fun query(q: String, from: String, to: String): String {
    val salt = floor(Math.random() * 10000)
    val key = if (Settings.useCustomBaiduKey) Settings.baiduTransLateKey else KEY
    val appId = if (Settings.useCustomBaiduKey) Settings.baiduTransLateAppId else ID
    val sign = md5("$appId$q$salt$key").lowercase()
    Logger.info("TranslateAppId:${Settings.baiduTransLateAppId},TranslateAppId:${Settings.baiduTransLateKey}", "Translate")

    return "$ROOT?q=${URLEncoder.encode(q, utf8Charset)}&from=$from&to=$to&appid=$appId&salt=$salt&sign=$sign"
}

/**
 * Translate [text] from [ori] language to [dst] language
 * @param text Text to translate
 * @param ori The origin language
 * @param dst The destination language
 * @return Translated text
 */
@Throws(IOException::class)
fun translate(text: String, ori: String, dst: String): String {
    return try {
        val connection = URL(query(text, ori, dst)).openConnection().apply { connect() }
        val result = ObjectMapper().readTree(connection.getInputStream())
        result.get("error_code")?.asText() ?: result.get("trans_result").joinToString("\n") { it.get("dst").asText() }
    } catch (e: NoRouteToHostException) {
        "No Network"
    } catch (e: SocketTimeoutException) {
        "Timeout"
    } catch (e: ConnectException) {
        "Connect failed"
    }
}

/**
 * Translate Japanese to Simplified Chinese
 */
@Throws(IOException::class)
fun translateJP(text: String): String = translate(text, "jp", "zh")

/**
 * Translate Traditional Chinese to Simplified Chinese
 */
@Throws(IOException::class)
fun convert2Simplified(text: String): String = translate(text, "cht", "zh")
/**
 * Translate Simplified Chinese to Traditional Chinese
 */
@Throws(IOException::class)
fun convert2Traditional(text: String): String = translate(text, "zh", "cht")
