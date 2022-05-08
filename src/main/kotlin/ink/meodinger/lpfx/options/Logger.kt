package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.V
import ink.meodinger.lpfx.type.LPFXTask
import ink.meodinger.lpfx.util.once

import jakarta.mail.*
import jakarta.mail.internet.*
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
            .append("[").append(type.type.padEnd(11)).append("] ")
            .appendLine(text)
            .toString()

        if (type == LogLevel.DEBUG) System.err.print(log) else print(log)

        writer.write(log)
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

        System.err.println("<Logger>: $str")
        writer.write(str)
        writer.flush()
    }

    fun sendLogSync(logFile: File = log) {
        // Account owned by Meodinger Wang
        // DO NOT USE FOR PRIVATE, I trust you.
        val reportUser = "labelplusfx_report@163.com"
        val reportAuth = "SUWAYUTJSKWQNDOF"
        val targetUser = "meodinger@qq.com"

        // properties
        val props = Properties()
        props.setProperty("mail.transport.protocol", "smtp")
        props.setProperty("mail.smtp.auth", "true")
        props.setProperty("mail.smtp.host", "smtp.163.com")

        // content
        val content = MimeMultipart()
        content.addBodyPart(MimeBodyPart().apply { setText("Got a problem! (or not)\nFrom LPFX $V") })
        content.addBodyPart(MimeBodyPart().apply { attachFile(logFile) })

        // message
        val message = MimeMessage(Session.getInstance(props))
        message.subject = "LPFX log report - ${System.getProperty("user.name")}"
        message.setFrom(reportUser)
        message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(targetUser))
        message.setContent(content)

        Transport.send(message, reportUser, reportAuth)
    }
    fun sendLog(logFile: File = log, onSucceeded: () -> Unit = {}, onFailed: (Throwable) -> Unit = {}) {
        val task = LPFXTask.createTask<Unit> { sendLogSync(logFile) }

        task.setOnFailed {
            error("Log sent failed", "Logger")
            exception(it)
            onFailed(it)
        }

        task.setOnSucceeded {
            info("Sent Log ${logFile.name}", "Logger")
            onSucceeded()
        }

        task.startInNewThread()
    }

}
