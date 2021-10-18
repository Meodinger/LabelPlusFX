package info.meodinger.lpfx

import info.meodinger.lpfx.options.Logger
import info.meodinger.lpfx.options.Options

import javafx.application.Application
import javafx.application.Platform
import kotlin.system.exitProcess


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx
 */

/**
 * Launcher for LabelPlusFX
 */
fun main(vararg args: String) {
    Platform.setImplicitExit(false)

    Options.init()

    Logger.start()
    Application.launch(LabelPlusFX::class.java, *args)
    Logger.stop()

    exitProcess(0)
}