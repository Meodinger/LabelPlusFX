package crash

import DemoController
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

/**
 * Author: Meodinger
 * Date: 2021/8/23
 * Location:
 */
class DemoApp : Application() {
    override fun start(primaryStage: Stage) {
        println("Start")
        val loader = FXMLLoader(javaClass.getResource("window.fxml")).also {
            println("Loader init start")
            it.setControllerFactory {
                println("Controller Factory")
                DemoController
            }
        }
        println("Loader init end")
        println("Loader load")
        val root = loader.load<Parent>()
        println("Loader loaded")
        val scene = Scene(root, 400.0, 600.0)

        primaryStage.scene = scene
        primaryStage.show()
    }

    fun main(vararg args: String) {
        launch(*args)
    }
}