package component;

import info.meodinger.lpfx.component.CLabelPane
import io.commonTest

import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.Scene
import javafx.scene.control.TextArea
import javafx.scene.layout.VBox
import javafx.stage.Stage

/**
 * Author: Meodinger
 * Date: 2021/8/2
 * Location: component
 */
class LabelPane : Application() {

    companion object {
        private const val WIDTH = 600.0
        private const val HEIGHT = 600.0
    }

    val transFile = commonTest()
    val picName = "1.jpg"

    val box = VBox()
    val pane = CLabelPane()
    val textArea = TextArea()


    init {
        val colorList = ArrayList<String>()
        transFile.groupList.forEach { colorList.add(it.color) }

        pane.minScale = 0.2
        pane.maxScale = 2.0
        pane.defaultCursor = Cursor.CROSSHAIR
        pane.colorList = colorList
        pane.onLabelClicked = EventHandler {
            textArea.text = "233"
        }
        pane.onLabelPointed = EventHandler {
            textArea.text = transFile.transMap[picName]!![it.labelIndex - 1].toString()
        }

        pane.setupImage("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\src\\test\\resources\\sample\\1.jpg")
        pane.setupLayers(transFile.groupList.size)
        pane.setupLabels(transFile.transMap["1.jpg"]!!)

        box.children.add(pane)
        box.children.add(textArea)
    }

    override fun start(primaryStage: Stage) {
        primaryStage.scene = Scene(box, WIDTH, HEIGHT)
        primaryStage.show()
    }

}