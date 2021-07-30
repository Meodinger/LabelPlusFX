package component

import info.meodinger.lpfx.component.CColorPicker

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.layout.VBox
import javafx.stage.Stage

/**
 * Author: Meodinger
 * Date: 2021/7/30
 * Location: component
 */
class ColorPicker : Application() {

    private val box = VBox()
    private val canvas = Canvas(300.0, 200.0)
    private val picker = CColorPicker()

    init {
        box.children.addAll(canvas, picker)

        picker.valueProperty().addListener { _, _, newValue ->
            println("draw")
            val gc = canvas.graphicsContext2D
            gc.fill = newValue
            gc.fillRect( 0.0, 0.0, 300.0, 200.0)
        }
    }

    override fun start(primaryStage: Stage) {
        primaryStage.scene = Scene(box, 300.0, 300.0)
        primaryStage.show()
    }

}