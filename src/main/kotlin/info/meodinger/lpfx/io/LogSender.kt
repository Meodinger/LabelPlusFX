package info.meodinger.lpfx.io

import info.meodinger.lpfx.options.Logger
import info.meodinger.lpfx.type.LPFXTask

import jakarta.mail.*
import jakarta.mail.internet.*
import java.io.File
import java.util.*


/**
 * Author: Meodinger
 * Date: 2021/8/31
 * Location: info.meodinger.lpfx.io
 */

/**
 * An async log sender
 */
object LogSender {

    class SendTask(log: File) : LPFXTask<Unit>({
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
    })

    fun sendLog(log: File) {
        val task = SendTask(log)

        task.setOnFailed {
            Logger.error("Log sent failed", "LogSender")
            Logger.exception(it)
        }

        task.setOnSucceeded {
            Logger.info("Sent Log ${log.name}", "LogSender")
        }

        task.startInNewThread()
    }
}
