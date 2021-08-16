import javafx.application.Application

import component.*
import event.*
import info.meodinger.lpfx.util.dialog.initDialogOwner
import io.*
import options.*
import type.*
import util.*
import javafx.stage.Stage
import kotlin.system.exitProcess

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location:
 */
class App : Application() {
    override fun start(primaryStage: Stage) {
        initDialogOwner(null)

        //loaderTest()
        //exporterTest()
        //packTest()
        //fileTest()
        //optionsTest()
        //utilTest()

        dialogExceptionTest()

        exitProcess(0)
    }

}

fun main(vararg args: String) {
    Application.launch(App::class.java, *args)
    //eventTest()
    //compTest()
}

fun utilTest() {
    //zipTest()
}

fun compTest(vararg args: String) {
    Application.launch(LabelPane::class.java, *args)
}

fun eventTest(vararg args: String) {
    Application.launch(DragEventDemo::class.java, *args)
}