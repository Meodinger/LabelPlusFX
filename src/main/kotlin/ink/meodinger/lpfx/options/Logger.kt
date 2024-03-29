package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.Config
import ink.meodinger.lpfx.V
import ink.meodinger.lpfx.util.once

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

    enum class LogLevel(val type: String) {
        DEBUG("DEBUG"),
        INFO("INFO"),
        WARNING("WARNING"),
        ERROR("ERROR"),
        FATAL("FATAL");

        override fun toString(): String = type
    }

    private var writer: Writer by once()
    private val formatter = SimpleDateFormat("HH:mm:ss:SSS")

    // Maybe
    // private var depth = 0
    // private var mark = depth

    /**
     * Actual log file. Name: {timestamp}.txt
     */
    val log: File

    /**
     * Log Level
     * @see LogLevel
     */
    var level: LogLevel = LogLevel.DEBUG

    /**
     * Whether the Logger started
     */
    var isStarted: Boolean = false
        private set

    init {
        val path = Options.logs.resolve("${Date().time}.txt")
        if (Files.notExists(path)) Files.createFile(path)
        log = path.toFile()
    }

    fun start() {
        if (isStarted) return

        println("<Logger>: Start")

        writer = BufferedWriter(OutputStreamWriter(FileOutputStream(log), StandardCharsets.UTF_8))
        isStarted = true

        val builder = StringBuilder()
        builder.append("\n========== System Info ==========")
        builder.append("\n")
            .append("OS Name: ").append(System.getProperty("os.name")).append(", ")
            .append("Version: ").append(System.getProperty("os.version")).append(", ")
            .append("Arch: ").append(System.getProperty("os.arch")).append(";")
        builder.append("\nApplication Version: ").append(V).append(";")
        builder.append("\nProxy: ${Config.enableProxy}, JNI: ${Config.enableJNI}, Tray: ${Config.supportSysTray}")
        builder.append("\n============== End ==============")
        info(builder.toString(), "Logger Init")

        info("Logger start", "Logger")
    }
    fun stop() {
        if (!isStarted) return

        info("Logger exit", "Logger")

        writer.close()
        isStarted = false

        println("<Logger>: Exit")
    }

    private var time: Long = 0
    fun tic() {
        time = Date().time
    }
    fun toc() {
        info("Used ${Date().time - time}ms", "Logger")
    }

    private fun log(type: LogLevel, text: String, from: String) {
        if (!isStarted || type < level) return

        val log = StringBuilder()
            .append("<Logger>: ")
            .append("[").append(formatter.format(Date())).append("] ")
            .append("[").append(type.type.padEnd(7)).append("] ")
            .append("[").append(from.padEnd(11)).append("] ")
            .appendLine(text)
            .toString()

        if (type == LogLevel.DEBUG) System.err.print(log) else print(log)

        writer.write(log)
        writer.flush()
    }

    fun debug(anything: Any?, from: String) {
        debug(anything.toString(), from)
    }
    fun debug(message: String, from: String) {
        log(LogLevel.DEBUG, message, from)
    }
    fun info(message: String, from: String) {
        log(LogLevel.INFO, message, from)
    }
    fun warning(message: String, from: String) {
        log(LogLevel.WARNING, message, from)
    }
    fun error(message: String, from: String) {
        log(LogLevel.ERROR, message, from)
    }
    fun fatal(message: String, from: String) {
        log(LogLevel.FATAL, message, from)
    }

    fun exception(e: Throwable) {
        val str = e.stackTraceToString()

        System.err.println("<Logger>: $str")
        writer.write(str)
        writer.flush()
    }

}
