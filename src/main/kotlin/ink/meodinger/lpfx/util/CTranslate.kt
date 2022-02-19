package ink.meodinger.lpfx.util.translator

import com.fasterxml.jackson.databind.ObjectMapper
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
private const val KEY  = "Wo9lvMK4qjxpLVrFktt3"

private val utf8Charset = Charset.forName("UTF-8")
private val md5Instance = MessageDigest.getInstance("MD5")
private fun md5(text: String): String {
    return StringBuilder().apply {
        for (byte in md5Instance.digest(text.toByteArray(utf8Charset)))
            append((byte.toInt() and 0xFF).toString(16).padStart(2, '0'))
    }.toString()
}
private fun query(q: String, from: String, to: String): String {
    val appID = 20220208001077250
    val salt  = floor(Math.random() * 10000)
    val sign  = md5("$appID$q$salt$KEY").lowercase()

    return "$ROOT?q=${URLEncoder.encode(q, utf8Charset)}&from=$from&to=$to&appid=$appID&salt=$salt&sign=$sign"
}

@Throws(IOException::class)
fun translate(text: String, from: String, to: String): String {
    return try {
        val connection = URL(query(text, from, to)).openConnection().apply { connect() }
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

@Throws(IOException::class)
fun translateJP(text: String): String = translate(text, "jp", "zh")
@Throws(IOException::class)
fun convert2Simplified(text: String): String = translate(text, "cht", "zh")
@Throws(IOException::class)
fun convert2Traditional(text: String): String = translate(text, "zh", "cht")
