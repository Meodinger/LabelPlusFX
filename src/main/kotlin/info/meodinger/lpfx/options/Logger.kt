package info.meodinger.lpfx.options

import info.meodinger.lpfx.util.string.deleteTail

import java.io.*
import java.nio.charset.StandardCharsets
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

        companion object {
            fun getType(type: String): LogType = when (type) {
                DEBUG.type -> DEBUG
                INFO.type -> INFO
                WARNING.type -> WARNING
                ERROR.type -> ERROR
                FATAL.type -> FATAL
                else -> throw IllegalArgumentException("No such log type")
            }
        }
    }

    private val writer: Writer
    private val formatter = SimpleDateFormat("HH:mm:ss:SSS")

    val log: File
    var level: LogType = LogType.DEBUG

    init {
        val path = Options.logs.resolve(SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Date()))
        if (Files.notExists(path)) Files.createFile(path)
        log = path.toFile()

        writer = BufferedWriter(OutputStreamWriter(FileOutputStream(log), StandardCharsets.UTF_8))
    }

    fun start() {
        info("Logger start", "Logger")
    }

    fun stop() {
        info("Logger exit", "Logger")

        writer.close()
    }

    private fun log(time: Long, type: LogType, text: String, from: String? = null) {
        if (type < level) return

        val builder = StringBuilder()

        builder.append("[").append(formatter.format(Date(time))).append("] ")

        builder.append("[")
        if (from != null) builder.append(from).append("/")
        builder.append(type)
        builder.append("] ")

        builder.appendLine(text)

        writer.write(builder.toString())
        writer.flush()
    }

    fun debug(message: String, list: List<*>, from: String? = null) {
        val time = Date().time

        val builder = StringBuilder()
        for (e in list) builder.appendLine(e)
        if (builder.isNotEmpty()) builder.deleteTail("\n")

        log(time, LogType.DEBUG, "$message\n$builder", from)
    }

    fun debug(message: String, from: String? = null) {
        log(Date().time, LogType.DEBUG, message, from)
    }

    fun info(message: String, from: String? = null) {
        log(Date().time, LogType.INFO, message, from)
    }

    fun warning(message: String, from: String? = null) {
        log(Date().time, LogType.WARNING, message, from)
    }

    fun error(message: String, from: String? = null) {
        log(Date().time, LogType.ERROR, message, from)
    }

    fun fatal(message: String, from: String? = null) {
        log(Date().time, LogType.FATAL, message, from)
    }

    fun exception(e: Throwable) {
        writer.write(e.stackTraceToString())
        writer.flush()
    }
}