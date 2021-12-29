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


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * Launcher for LabelPlusFX
 */
fun main(vararg args: String) {
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
        JFrame("ERROR").also {
            it.contentPane.layout = BorderLayout()
            it.contentPane.add(JPanel().apply {
                add(JLabel("Something Fatal Happened", JLabel.CENTER))
                add(JLabel("Please Contact Meodinger For Help", JLabel.CENTER))
                add(JLabel("----------------------------------------", JLabel.CENTER))
                add(JLabel(e.javaClass.name + ": " + e.message, JLabel.CENTER))
            }, BorderLayout.CENTER)
            it.setSize(300, 120)
            it.setLocationRelativeTo(null)

            it.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            it.isVisible = true
        }
    }

    // For shutdown hooks
    Platform.setImplicitExit(false)

    // Init options
    Options.init()

    Logger.start()
    Application.launch(LabelPlusFX::class.java, *args)
    Logger.stop()
}
