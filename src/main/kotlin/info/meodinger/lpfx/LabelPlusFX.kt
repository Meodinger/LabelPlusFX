package info.meodinger.lpfx

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx
 */
class LabelPlusFX: Application() {

    override fun start(primaryStage: Stage) {
        val loader = FXMLLoader(javaClass.getResource("Window.fxml"))
        val root = loader.load<Parent>()
        val scene = Scene(root, 900.0, 600.0)

        primaryStage.scene = scene
        primaryStage.show()
    }
}