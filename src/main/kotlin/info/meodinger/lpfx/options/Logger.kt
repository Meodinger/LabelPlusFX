package info.meodinger.lpfx.options

import info.meodinger.lpfx.util.string.deleteTail

import java.io.*
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*


/**
 * Author: Meodinger
 * Date: 2021/8/28
 * Location: info.meodinger.lpfx.options
 */
object Logger {

    enum class LogType(val type: String) {
        DEBUG("DEBUG"),
        INFO("INFO"),
        WARNING("WARNING"),
        ERROR("ERROR"),
        FATAL("FATAL");

        override fun toString(): String = type
    }

    private val log: File
    private val writer: Writer
    private val formatter = SimpleDateFormat("HH:mm:ss:SSS")

    var level: LogType = LogType.INFO

    init {
        val path = Options.logs.resolve(SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Date()))
        if (Files.notExists(path)) Files.createFile(path)
        log = path.toFile()

        writer = BufferedWriter(OutputStreamWriter(FileOutputStream(log)))

        info("Logger start")
    }

    private fun log(type: LogType, text: String, from: String? = null) {
        if (type < level) return

        val builder = StringBuilder()

        builder.append("[").append(formatter.format(Date())).append("] ")

        builder.append("[")
        if (from != null) builder.append(from).append("/")
        builder.append(type)
        builder.append("] ")

        builder.appendLine(text)

        writer.write(builder.toString())
        writer.flush()
    }

    fun debug(message: String, list: List<*>, from: String? = null) {
        val builder = StringBuilder()
        for (e in list) builder.appendLine(e)
        if (builder.isNotEmpty()) builder.deleteTail("\n")

        debug("$message\n$builder", from)
    }

    fun debug(message: String, from: String? = null) {
        log(LogType.DEBUG, message, from)
    }

    fun info(message: String, from: String? = null) {
        log(LogType.INFO, message, from)
    }

    fun warning(message: String, from: String? = null) {
        log(LogType.WARNING, message, from)
    }

    fun error(message: String, from: String? = null) {
        log(LogType.ERROR, message, from)
    }

    fun fatal(message: String, from: String? = null) {
        log(LogType.FATAL, message, from)
    }

    fun exception(e: Exception) {
        e.printStackTrace(PrintWriter(writer))
    }
}