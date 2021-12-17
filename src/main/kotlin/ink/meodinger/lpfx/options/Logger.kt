package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.LOGSRC_LOGGER
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.string.deleteTail

import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*


/**
 * Author: Meodinger
 * Date: 2021/8/28
 * Have fun with my code!
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
                else -> throw IllegalArgumentException(String.format(I18N["exception.log_type.invalid_log_type.s"], type))
            }
        }
    }

    private lateinit var writer: Writer
    private val formatter = SimpleDateFormat("HH:mm:ss:SSS")

    private var depth = 0
    private var mark = depth

    val log: File
    var level: LogType = LogType.DEBUG
    val isStarted: Boolean get() = this::writer.isInitialized

    init {
        val path = Options.logs.resolve(Date().time.toString())
        if (Files.notExists(path)) Files.createFile(path)
        log = path.toFile()
    }

    fun start() {
        writer = BufferedWriter(OutputStreamWriter(FileOutputStream(log), StandardCharsets.UTF_8))

        val builder = StringBuilder()
        builder.append("\n")
        builder.append("RT Version: ").append(Runtime.version()).append(";\n")
        builder.append("OS Name: ").append(System.getProperty("os.name")).append(", ")
        builder.append("OS Version: ").append(System.getProperty("os.version")).append(", ")
        builder.append("OS Arch: ").append(System.getProperty("os.arch")).append(";\n")
        info(builder.toString())

        info("Logger start", LOGSRC_LOGGER)
    }
    fun stop() {
        info("Logger exit", LOGSRC_LOGGER)

        writer.close()
    }

    private fun log(type: LogType, text: String, from: String? = null) {
        if (!this::writer.isInitialized) return
        if (type < level) return

        val logHead = StringBuilder()
            .append("[").append(formatter.format(Date())).append("] ")
            .append("[").append(String.format("%-7s", type)).append("] ")
        if (from != null) logHead
            .append("[").append(String.format("%-11s", from)).append("] ")
            .toString()

        val logText = StringBuilder()
            .append(logHead)
            .append("--".repeat(depth)).append("> ")
            .appendLine(text)
            .toString()

        writer.write(logText)
        writer.flush()
    }

    fun debug(message: String, list: List<*>, from: String? = null) {
        val builder = StringBuilder()
        for (e in list) builder.appendLine(e)
        if (builder.isNotEmpty()) builder.deleteTail("\n")

        log(LogType.DEBUG, "$message\n$builder", from)
    }
    fun debug(message: String, lazy: Lazy<List<*>>, from: String? = null) {
        debug(message, lazy.value, from)
    }

    fun debug(message: String, from: String? = null) {
        log(LogType.DEBUG, message, from)
    }
    fun debug(obj: Any?, from: String? = null) {
        debug(obj.toString(), from)
    }

    fun info(message: String, from: String? = null) {
        log(LogType.INFO, message, from)
    }
    fun info(obj: Any?, from: String? = null) {
        info(obj.toString(), from)
    }

    fun warning(message: String, from: String? = null) {
        log(LogType.WARNING, message, from)
    }
    fun warning(obj: Any?, from: String? = null) {
        warning(obj.toString(), from)
    }

    fun error(message: String, from: String? = null) {
        log(LogType.ERROR, message, from)
    }
    fun error(obj: Any?, from: String? = null) {
        error(obj.toString(), from)
    }

    fun fatal(message: String, from: String? = null) {
        log(LogType.FATAL, message, from)
    }
    fun fatal(obj: Any?, from: String? = null) {
        fatal(obj.toString(), from)
    }

    fun exception(e: Throwable) {
        val str = e.stackTraceToString()

        System.err.println(str)
        writer.write(str)
        writer.flush()
    }
}
