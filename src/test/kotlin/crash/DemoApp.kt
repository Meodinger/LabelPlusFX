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

        val loader = FXMLLoader(javaClass.getResource("window.fxml")).also { it.setControllerFactory { Controller } }
        val root = loader.load<Parent>()
        val scene = Scene(root, 4600.0, 900.0)

        primaryStage.scene = scene
        primaryStage.show()
    }

    fun main(vararg args: String) {
        launch(*args)
    }
}