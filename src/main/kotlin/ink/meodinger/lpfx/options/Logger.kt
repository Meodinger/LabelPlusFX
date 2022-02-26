package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.LOGSRC_LOGGER
import ink.meodinger.lpfx.V

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

    private lateinit var writer: Writer
    private val formatter = SimpleDateFormat("HH:mm:ss:SSS")

    // Maybe use for
    // private var depth = 0
    // private var mark = depth

    val log: File
    var level: LogLevel = LogLevel.DEBUG
    var isStarted: Boolean = false

    init {
        val path = Options.logs.resolve(Date().time.toString())
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
        builder.append("\n============== End ==============")
        info(builder.toString(), "Logger Init")

        info("Logger start", LOGSRC_LOGGER)
    }
    fun stop() {
        if (!isStarted) return

        info("Logger exit", LOGSRC_LOGGER)

        writer.close()
        isStarted = false

        println("<Logger>: Exit")
    }

    private fun log(type: LogLevel, text: String, from: String) {
        if (!isStarted || type < level) return

        val logHead = StringBuilder()
            .append("[").append(formatter.format(Date())).append("] ")
            .append("[").append(String.format("%-7s", type)).append("] ")
            .append("[").append(String.format("%-11s", from)).append("] ")
            .toString()

        val logText = StringBuilder()
            .append(logHead)
          //.append("--".repeat(depth)).append("> ")
            .appendLine(text)
            .toString()

        print("<Logger>: $logText")

        writer.write(logText)
        writer.flush()
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

        System.err.println("<Logger>: ")
        System.err.println(str)
        writer.write(str)
        writer.flush()
    }
}
