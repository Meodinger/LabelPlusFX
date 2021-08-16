package info.meodinger.lpfx

import info.meodinger.lpfx.options.Options
import javafx.application.Application

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx
 */
fun main(vararg args: String) {
    Options.init()
    Application.launch(LabelPlusFX::class.java, *args)
}