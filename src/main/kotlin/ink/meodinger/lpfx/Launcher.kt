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
    // FX Thread not started, cannot use dialog or other FX things

    // for shutdown hooks
    Platform.setImplicitExit(false)
    // Init options
    Options.init()

    // Logger start
    Logger.start()

    Thread.currentThread().uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, e ->
        // Immediately log exception and send (Something fatal happened)
        // Note that we can only get exception information from log file if happened in this period
        Logger.fatal("Launch failed")
        Logger.exception(e)
        LogSender.sendSync(Logger.log)

        // Use swing as alternative window
        JFrame("ERROR").also {
            it.contentPane.layout = BorderLayout()
            it.contentPane.add(JPanel().apply {
                add(JLabel("Something Fatal Happened", JLabel.CENTER))
                add(JLabel("Please Contact Meodinger For Help", JLabel.CENTER))
                add(JLabel("(User Log Has Been Automatically Sent)", JLabel.CENTER))
            }, BorderLayout.CENTER)
            it.setSize(300, 120)
            it.setLocationRelativeTo(null)

            it.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            it.isVisible = true
        }
    }
    Application.launch(LabelPlusFX::class.java, *args)

    // Logger stop
    Logger.stop()
}