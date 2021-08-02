package event

import javafx.application.Application
import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.layout.VBox
import javafx.stage.Stage
import kotlin.math.roundToInt

/**
 * Author: Meodinger
 * Date: 2021/8/2
 * Location: event
 */
class SetHandlerTest : Application() {

    private val box = VBox()
    private val textArea = TextArea()
    private val button = Button("Click me")
    private val updateHandlerButton = Button("Update handler")

    private val handlerProperty = SimpleObjectProperty<EventHandler<ActionEvent>>(EventHandler { println(it) })
    private var handler: EventHandler<ActionEvent>
        get() = handlerProperty.value
        set(value) {
            handlerProperty.value = value
        }

    companion object {
        private const val WIDTH = 300.0
        private const val HEIGHT = 300.0
    }

    init {

        button.onAction = EventHandler {
            handler.handle(it)
        }

        updateHandlerButton.setOnAction {
            textArea.text = ""
            val random = (Math.random() * 1000).roundToInt()
            button.addEventHandler(ActionEvent.ACTION) {
                textArea.appendText("Handler@$random By addEventHandler\n")
            }

            handler = EventHandler {
                textArea.appendText("Handler@$random By handlerProperty\n")
            }
        }

        box.alignment = Pos.TOP_CENTER
        box.children.add(button)
        box.children.add(updateHandlerButton)
        box.children.add(textArea)
    }

    override fun start(primaryStage: Stage?) {
        primaryStage!!.scene = Scene(box, WIDTH, HEIGHT)
        primaryStage.show()
    }
}