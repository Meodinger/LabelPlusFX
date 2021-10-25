package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.util.string.deleteTail

import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*


/**
 * Author: Meodinger
 * Date: 2021/8/28
 * Location: ink.meodinger.lpfx.options
 */

/**
 * A simple Logger for global use
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

    private lateinit var writer: Writer
    private val formatter = SimpleDateFormat("HH:mm:ss:SSS")

    val log: File
    var level: LogType = LogType.DEBUG

    init {
        val path = Options.logs.resolve(Date().time.toString())
        if (Files.notExists(path)) Files.createFile(path)
        log = path.toFile()
    }

    fun start() {
        writer = BufferedWriter(OutputStreamWriter(FileOutputStream(log), StandardCharsets.UTF_8))

        info("Logger start", "Logger")
    }

    fun stop() {
        info("Logger exit", "Logger")

        writer.close()
    }

    private fun log(type: LogType, text: String, from: String? = null) {
        if (!this::writer.isInitialized) return
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

    fun debug(message: String, lazy: Lazy<List<*>>, from: String? = null) {
        debug(message, lazy.value, from)
    }

    fun debug(message: String, list: List<*>, from: String? = null) {
        val builder = StringBuilder()
        for (e in list) builder.appendLine(e)
        if (builder.isNotEmpty()) builder.deleteTail("\n")

        log(LogType.DEBUG, "$message\n$builder", from)
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

    fun exception(e: Throwable) {
        val str = e.stackTraceToString()

        System.err.println(str)
        writer.write(str)
        writer.flush()
    }
}