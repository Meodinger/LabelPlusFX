package info.meodinger.lpfx

import info.meodinger.lpfx.util.dialog.initDialogOwner
import info.meodinger.lpfx.util.resource.ICON
import info.meodinger.lpfx.util.resource.INFO
import info.meodinger.lpfx.util.resource.get

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.input.KeyEvent
import javafx.stage.Stage

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx
 */
class LabelPlusFX: Application() {

    override fun start(primaryStage: Stage) {
        State.application = this
        State.stage = primaryStage

        val loader = FXMLLoader(javaClass.getResource("Window.fxml")).also {
            it.setControllerFactory {
                Controller
            }
        }
        val controller = loader.getController<Controller>()
        val root = loader.load<Parent>()

        primaryStage.setOnCloseRequest { controller.close() }
        primaryStage.title = INFO["application.name"]
        primaryStage.icons.add(ICON)
        primaryStage.scene = Scene(root, WIDTH, HEIGHT).also {
            // Global event catch, prevent mnemonic parsing
            it.addEventHandler(KeyEvent.KEY_PRESSED) { event ->
                if (event.isAltDown) event.consume()
            }
        }
        primaryStage.show()

        State.controller = controller
        initDialogOwner(primaryStage)
    }
}