package component

import info.meodinger.lpfx.component.CFileChooser

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage

/**
 * Author: Meodinger
 * Date: 2021/7/30
 * Location: component
 */
class FileChooser : Application() {

    private val box = VBox()
    private val button = Button("Choose")
    private val buttonA = Button("Another")
    private val label = Label()
    private val chooser = CFileChooser()

    init {
        button.setOnAction {
            val file = chooser.showOpenDialog(null)
            label.text = file?.path
        }
        buttonA.setOnAction {
            label.text = CFileChooser().showOpenDialog(null)?.path
        }

        box.children.addAll(HBox(button, buttonA), label)
    }

    override fun start(primaryStage: Stage) {
        primaryStage.scene = Scene(box, 300.0, 50.0)
        primaryStage.show()
    }

}