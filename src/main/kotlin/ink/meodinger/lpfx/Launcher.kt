package ink.meodinger.lpfx

import ink.meodinger.lpfx.io.sendMailSync
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Options
import ink.meodinger.lpfx.util.doNothing

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
    // CLI commands to App args
    val appArgs = ArrayList<String>()
    for (arg in args) when (arg) {
        "--disable-proxy" -> Config.enableProxy = false
        "--disable-jni"   -> Config.enableJNI   = false
        else -> appArgs.add(arg)
    }

    // Use System Proxies
    if (Config.enableProxy) {
        System.setProperty("java.net.useSystemProxies", "true")
        System.err.println("Enabled System Proxies")
    }

    // Load IME-related jni library
    if (Config.enableJNI) {
        if (Config.isWin) System.loadLibrary("IMEWrapper")
        System.err.println("Loaded JNI Libraries")
    }

    // Global Uncaught Exception Handler
    Thread.setDefaultUncaughtExceptionHandler { t, e ->
        System.err.println("<UncaughtHandler>: On thread ${t.name}:\n${e.stackTraceToString()}")

        if (Logger.isStarted) {
            Logger.error("Exception uncaught in Thread: ${t.name}", "Other")
            Logger.exception(e)
        }
    }

    // Immediately log exception and send if Logger started successfully
    // Note that we can only get exception information from log file if happened in this period
    // And if Options or Logger init failed, we can only get information from Swing window
    Thread.currentThread().setUncaughtExceptionHandler { _, e ->
        if (Logger.isStarted) {
            Logger.fatal("Launch failed", "Main")
            Logger.exception(e)
            try {
                sendMailSync("Fatal Error!", Logger.log)
            } catch (_: Throwable) {
                doNothing()
            }
        }

        // FX Thread not started, cannot use dialog or other FX things
        // Use swing as alternative alert window
        JFrame("ERROR").apply {
            setLocationRelativeTo(null)
            contentPane.layout = BorderLayout()
            contentPane.add(JPanel().apply {
                add(JLabel("Something Fatal Happened", JLabel.CENTER))
                add(JLabel("Please Contact Meodinger (meodinger@qq.com) For Help", JLabel.CENTER))
            }, BorderLayout.CENTER)
            pack()

            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            isResizable = false
            isVisible = true
        }
    }

    // Let FX Thread keep running when Stage closed by BOSS Key
    Platform.setImplicitExit(false)

    // Init options
    Options.init()

    Logger.start()
    Application.launch(LabelPlusFX::class.java, *appArgs.toTypedArray())
    Logger.stop()

    exitProcess(0)
}
