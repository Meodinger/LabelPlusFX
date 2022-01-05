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
        textPart.setText("Got a problem! (or not)\nFrom LPFX ${UpdateChecker.V}")

        // file part
        val filePart = MimeBodyPart()
        filePart.attachFile(log)
        filePart.fileName = "${log.name}.txt"

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
    fun send(log: File, onSucceeded: () -> Unit, onFailed: (Throwable) -> Unit) {
        val task = LPFXTask { sendSync(log) }

        task.setOnFailed {
            Logger.error("Log sent failed", LOGSRC_SENDER)
            Logger.exception(it)
            onFailed(it)
        }

        task.setOnSucceeded {
            Logger.info("Sent Log ${log.name}", LOGSRC_SENDER)
            onSucceeded()
        }

        task.startInNewThread()
    }
}
