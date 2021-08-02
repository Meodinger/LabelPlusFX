package event

import info.meodinger.lpfx.component.CLabel
import info.meodinger.lpfx.util.color.toHex

import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.input.DataFormat
import javafx.scene.input.TransferMode
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Stage

/**
 * Author: Meodinger
 * Date: 2021/7/30
 * Location: event
 */
class DragEventDemo : Application() {
    companion object {
        private const val WIDTH = 300.0
        private const val HEIGHT = 300.0
    }

    private val box = VBox()
    private val anchorPane = AnchorPane()
    private val drag = CLabel(1, 20.0)
    private val drop = CLabel(2, 20.0)

    private var shiftX = 0.0
    private var shiftY = 0.0

    init {
        anchorPane.setPrefSize(WIDTH, HEIGHT)
        anchorPane.children.addAll(drag, drop)
        anchorPane.background = Background(BackgroundFill(Color.BLACK, CornerRadii(0.0), Insets(0.0)))

        // Drag
        // ScenePos -> CursorPos; LayoutPos -> CtxPos
        // nLx = Lx + (nSx - Sx); nLy = Ly + (nSy - Sy)
        // nLx = (Lx - Sx) + nSx; nLy = (Ly - Sy) + nSy
        drag.layoutX = 100.0; drag.layoutY = 100.0
        drop.layoutX = 200.0; drop.layoutY = 100.0

        drag.setOnDragDetected {
            println("start!")
            shiftX = drag.layoutX - it.sceneX
            shiftY = drag.layoutY - it.sceneY

            val dragBoard = drag.startDragAndDrop(TransferMode.COPY)
            dragBoard.setContent(mapOf(
                DataFormat.PLAIN_TEXT to "2333"
            ))
        }
        drag.setOnDragDone {
            println("end!")

            println("""
                |$shiftX, $shiftY,
                |${it.sceneX}, ${it.sceneY}
                |${it.sceneX + shiftX}, ${it.sceneY + shiftY}
            """.trimMargin())
            drag.layoutX = it.sceneX + shiftX
            drag.layoutY = it.sceneY + shiftY
        }

        drop.setOnDragEntered {
            println("enter!")
        }
        drop.setOnDragOver {
            drop.color = Color.BLUE.toHex()
            it.acceptTransferModes(TransferMode.COPY);
        }
        drop.setOnDragExited {
            println("exit!")
            drop.color = Color.RED.toHex()
        }
        drop.setOnDragDropped {
            println("drop!")
            println(it.dragboard.getContent(DataFormat.PLAIN_TEXT))
        }

        box.children.add(anchorPane)
    }

    override fun start(primaryStage: Stage) {
        primaryStage.scene = Scene(box, WIDTH, HEIGHT)
        primaryStage.show()
    }
}