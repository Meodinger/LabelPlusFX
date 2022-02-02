package ink.meodinger.lpfx

import ink.meodinger.lpfx.io.LogSender
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Options

import javafx.application.Application
import javafx.application.Platform
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import java.awt.BorderLayout
import kotlin.system.exitProcess


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * Launcher for LabelPlusFX
 */
fun main(vararg args: String) {
    // Use System Proxies
    System.setProperty("java.net.useSystemProxies", "true")
    // Fix shake_hands issue (Client sends v1.2 but server responses v.13)
    System.setProperty("https.protocols", "TLSv1.2,TLSv1.1,SSLv3")

    // Global Uncaught Exception Handler
    Thread.setDefaultUncaughtExceptionHandler { t, e ->
        System.err.println("On thread ${t.name}: ${e.stackTraceToString()}")

        if (!Logger.isStarted) return@setDefaultUncaughtExceptionHandler

        Logger.error("Exception uncaught in Thread: ${t.name}", "Other")
        Logger.exception(e)
        LogSender.send(Logger.log)
    }

    // Immediately log exception and send if Logger started successfully
    // Note that we can only get exception information from log file if happened in this period
    // And if Options or Logger init failed, we can only get information from Swing window
    Thread.currentThread().uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, e ->
        if (Logger.isStarted) {
            Logger.fatal("Launch failed", "Main")
            Logger.exception(e)
            LogSender.sendSync(Logger.log)
        }

        // FX Thread not started, cannot use dialog or other FX things
        // Use swing as alternative alert window
        JFrame("ERROR").apply {
            contentPane.layout = BorderLayout()
            contentPane.add(JPanel().apply {
                add(JLabel("Something Fatal Happened", JLabel.CENTER))
                add(JLabel("Please Contact Meodinger For Help", JLabel.CENTER))
                add(JLabel("----------------------------------------", JLabel.CENTER))
                add(JLabel(e.javaClass.name + ": " + e.message, JLabel.CENTER))
            }, BorderLayout.CENTER)
            setSize(300, 120)
            setLocationRelativeTo(null)

            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            isVisible = true
        }
    }

    // For shutdown hooks
    Platform.setImplicitExit(false)

    // Init options
    Options.init()

    Logger.start()
    Application.launch(LabelPlusFX::class.java, *args)
    Logger.stop()

    exitProcess(0)
}
