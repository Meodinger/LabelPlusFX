import javafx.application.Application

import component.*
import event.*
import io.*
import javafx.stage.Stage
import type.*
import util.*
import kotlin.system.exitProcess

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location:
 */
class App : Application() {
    override fun start(primaryStage: Stage) {
        // do nothing
        // loaderTest()
        // exporterTest()
        transTest()
        exitProcess(0)
    }

}

fun main(vararg args: String) {
    Application.launch(App::class.java, *args)
}

fun transTest() {
    labelTest()
    groupTest()
    fileTest()
}

fun utilTest() {
    zipTest()
}

fun compTest(vararg args: String) {
    Application.launch(ComboBox::class.java, *args)
}

fun eventTest(vararg args: String) {
    Application.launch(DragEventDemo::class.java, *args)
}