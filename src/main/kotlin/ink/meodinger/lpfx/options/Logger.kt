package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.LOGSRC_LOGGER
import ink.meodinger.lpfx.LOGSRC_SENDER
import ink.meodinger.lpfx.V
import ink.meodinger.lpfx.type.LPFXTask
import ink.meodinger.lpfx.util.assignOnce
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart

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

    private var writer: Writer by assignOnce()
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

        info("Logger start", LOGSRC_LOGGER)
    }
    fun stop() {
        if (!isStarted) return

        info("Logger exit", LOGSRC_LOGGER)

        writer.close()
        isStarted = false

        println("<Logger>: Exit")
    }

    private var time: Long = 0
    fun tic() {
        time = Date().time
    }
    fun toc() {
        info("Used ${Date().time - time}ms", LOGSRC_LOGGER)
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

        // text part
        val textPart = MimeBodyPart()
        textPart.setText("Got a problem! (or not)\nFrom LPFX $V")

        // file part
        val filePart = MimeBodyPart()
        filePart.attachFile(logFile)
        filePart.fileName = "${logFile.name}.txt"

        // content
        val content = MimeMultipart()
        content.addBodyPart(textPart)
        content.addBodyPart(filePart)

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
            error("Log sent failed", LOGSRC_SENDER)
            exception(it)
            onFailed(it)
        }

        task.setOnSucceeded {
            info("Sent Log ${logFile.name}", LOGSRC_SENDER)
            onSucceeded()
        }

        task.startInNewThread()
    }

}
