package ink.meodinger.lpfx.io

import ink.meodinger.lpfx.LOGSRC_SENDER
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.type.LPFXTask

import jakarta.mail.*
import jakarta.mail.internet.*
import java.io.File
import java.util.*


/**
 * Author: Meodinger
 * Date: 2021/8/31
 * Have fun with my code!
 */

/**
 * An async log sender
 */
object LogSender {

    fun sendSync(log: File) {
        // properties
        val host = "smtp.163.com"
        val reportUser = "labelplusfx_report@163.com"
        val reportAuth = "SUWAYUTJSKWQNDOF"
        val targetUser = "meodinger@qq.com"
        val props = Properties()
        props.setProperty("mail.transport.protocol", "smtp")
        props.setProperty("mail.smtp.auth", "true")
        props.setProperty("mail.smtp.host", host)

        // main variables
        val textPart = MimeBodyPart()
        val filePart = MimeBodyPart()
        val content = MimeMultipart()
        val message = MimeMessage(Session.getInstance(props))

        // text part
        val builder = StringBuilder()
        builder.append(System.getProperty("os.name")).append("-")
        builder.append(System.getProperty("os.version")).append("-")
        builder.append(System.getProperty("os.arch"))
        textPart.setText(builder.toString())

        // file part
        filePart.attachFile(log)
        filePart.fileName = "${log.name}.txt"

        // content
        content.addBodyPart(textPart)
        content.addBodyPart(filePart)

        // message
        message.subject = "LPFX log report - ${System.getProperty("user.name")}"
        message.setFrom(reportUser)
        message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(targetUser))
        message.setContent(content)

        Transport.send(message, reportUser, reportAuth)
    }

    fun send(log: File) {
        val task = LPFXTask { sendSync(log) }

        task.setOnFailed {
            Logger.error("Log sent failed", LOGSRC_SENDER)
            Logger.exception(it)
        }

        task.setOnSucceeded {
            Logger.info("Sent Log ${log.name}", LOGSRC_SENDER)
        }

        task.startInNewThread()
    }
    fun send(log: File, callback: () -> Unit) {
        val task = LPFXTask { sendSync(log) }

        task.setOnFailed {
            Logger.error("Log sent failed", LOGSRC_SENDER)
            Logger.exception(it)
            callback()
        }

        task.setOnSucceeded {
            Logger.info("Sent Log ${log.name}", LOGSRC_SENDER)
            callback()
        }

        task.startInNewThread()
    }
    fun send(log: File, onFailed: () -> Unit, onSucceeded: () -> Unit) {
        val task = LPFXTask { sendSync(log) }

        task.setOnFailed {
            Logger.error("Log sent failed", LOGSRC_SENDER)
            Logger.exception(it)
            onFailed()
        }

        task.setOnSucceeded {
            Logger.info("Sent Log ${log.name}", LOGSRC_SENDER)
            onSucceeded()
        }

        task.startInNewThread()
    }
}
