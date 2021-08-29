package info.meodinger.lpfx

import info.meodinger.lpfx.options.Logger

import javafx.application.Application

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx
 */
fun main(vararg args: String) {
    Logger.info("App start")
    Application.launch(LabelPlusFX::class.java, *args)
}