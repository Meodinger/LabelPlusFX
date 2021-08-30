package info.meodinger.lpfx.util

import info.meodinger.lpfx.options.Logger

import javafx.scene.control.TextFormatter
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.util
 */

/**
 * All streams want to be auto-closed should run `autoClose()`
 */
inline fun using(crossinline block: ResourceManager.() -> Unit): Catcher {
    val manager = ResourceManager()
    try {
        // use manager's auto close
        manager.use(block)
    } catch (t: Throwable) {
        manager.throwable = t
    }
    return manager.getCatcher()
}
class ResourceManager : AutoCloseable {

    private val resourceQueue = ConcurrentLinkedDeque<AutoCloseable>()
    var throwable: Throwable? = null

    fun <T: AutoCloseable> T.autoClose(): T {
        resourceQueue.addFirst(this)
        return this
    }

    override fun close() {
        for (closeable in resourceQueue) {
            try {
                closeable.close()
            } catch (t: Throwable) {
                if (this.throwable == null) {
                    this.throwable = t
                } else {
                    this.throwable!!.addSuppressed(t)
                }
            }
        }
    }

    fun getCatcher(): Catcher {
        return Catcher(this)
    }
}
class Catcher(manager: ResourceManager) {
    var throwable: Throwable? = null
    var thrown: Throwable? = null

    init {
        throwable = manager.throwable
    }

    inline infix fun <reified T : Throwable> catch(block: (T) -> Unit): Catcher {
        if (throwable is T) {
            try {
                block(throwable as T)
            } catch (thrown: Throwable) {
                this.thrown = thrown
            } finally {
                // It's been caught, so set it to null
                throwable = null
            }
        }
        return this
    }

    inline infix fun finally(block: () -> Unit) {
        try {
            block()
        } catch (thrown: Throwable) {
            if (throwable == null) {
                // we've caught the exception, or none was thrown
                if (this.thrown == null) {
                    // No exception was thrown in the catch blocks
                    throw thrown
                } else {
                    // An exception was thrown in the catch block
                    this.thrown!!.let {
                        it.addSuppressed(thrown)
                        throw it
                    }
                }
            } else {
                // We never caught the exception
                // So this.thrown is also null
                throwable!!.let {
                    it.addSuppressed(thrown)
                    throw it
                }
            }
        }

        // At this point the `finally` block did not throw an exception
        // We need to see if there are still any exceptions left to throw
        throwable?.let { t ->
            thrown?.let { t.addSuppressed(it) }
            throw t
        }
        thrown?.let { throw it }
    }
}

fun getGroupNameFormatter() = TextFormatter<String> { change ->
    change.text = change.text
        .trim()
        .replace(" ", "_")
        .replace("|", "_")
    change
}

fun sendLog(log: File) = Thread {
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
    filePart.fileName = "log.txt"

    // content
    content.addBodyPart(textPart)
    content.addBodyPart(filePart)

    // message
    message.subject = "LPFX log report - ${System.getProperty("user.name")}"
    message.setFrom(reportUser)
    message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(targetUser))
    message.setContent(content)

    try {
        Transport.send(message, reportUser, reportAuth)
    } catch (e: Exception) {
        e.printStackTrace()
        Logger.error("Log sent failed")
        Logger.exception(e)
    }
}.start()