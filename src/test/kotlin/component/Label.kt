package component

import info.meodinger.lpfx.component.CLabel
import info.meodinger.lpfx.util.color.toHex

import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.input.MouseButton
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Stage

/**
 * Author: Meodinger
 * Date: 2021/7/30
 * Location: component
 */
class Label : Application() {

    companion object {
        private const val WIDTH = 300.0
        private const val HEIGHT = 300.0
    }

    private val box = VBox()
    private val anchorPane = AnchorPane()
    private val label_1 = CLabel(1, 20.0, Color.RED)
    private val label_2 = CLabel(2, 20.0, Color.RED)
    private val label_3 = CLabel(3, 20.0, Color.RED)
    private val label_4 = CLabel(4, 20.0, Color.RED)

    private var shiftX = 0.0
    private var shiftY = 0.0

    init {
        anchorPane.setPrefSize(WIDTH, HEIGHT)
        anchorPane.children.addAll(label_1, label_2, label_3, label_4)
        anchorPane.background = Background(BackgroundFill(Color.BLACK, CornerRadii(0.0), Insets(0.0)))

        label_1.layoutX = 100.0; label_1.layoutY = 100.0
        label_2.layoutX = 200.0; label_2.layoutY = 100.0
        label_3.layoutX = 100.0; label_3.layoutY = 200.0
        label_4.layoutX = 200.0; label_4.layoutY = 200.0

        label_1.setOnMousePressed {
            println("start!")
            shiftX = label_1.layoutX - it.sceneX
            shiftY = label_1.layoutY - it.sceneY
        }
        label_1.setOnMouseDragged {
            label_1.color = Color.BLUE
            label_1.layoutX = it.sceneX + shiftX
            label_1.layoutY = it.sceneY + shiftY
        }
        label_1.setOnMouseReleased {
            println("end!")
            label_1.color = Color.RED
        }

        label_2.setOnMouseClicked {
            if (it.button == MouseButton.PRIMARY) label_2.index++
            else label_2.index--
        }

        label_3.setOnMouseClicked {
            if (it.button == MouseButton.PRIMARY) label_3.radius *= 2
            else label_3.radius /=2
        }

        label_4.setOnMouseClicked {
            if (it.button == MouseButton.PRIMARY) label_4.color = Color.web(label_4.color.toHex() + "A0")
            else label_4.color = Color.web(label_4.color.toHex() + "FF")
        }

        box.children.add(anchorPane)
    }

    override fun start(primaryStage: Stage) {
        primaryStage.scene = Scene(box, WIDTH, HEIGHT)
        primaryStage.show()
    }

}