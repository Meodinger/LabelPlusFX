package component

import info.meodinger.lpfx.component.CComboBox
import javafx.application.Application
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage

/**
 * Author: Meodinger
 * Date: 2021/7/30
 * Location: component
 */
class ComboBox : Application() {

    private val vBox = VBox()

    init {
        val list = arrayListOf("1", "2", "3", "4")

        val label1 = Label("default")
        val cBox1 = CComboBox<String>()
        cBox1.isWrapped = false
        cBox1.setList(list)
        val box1 = HBox(cBox1, label1)
        box1.alignment = Pos.CENTER_LEFT


        val label2 = Label("wrap")
        val cBox2 = CComboBox<String>()
        cBox2.isWrapped = true
        cBox2.setList(list)
        val box2 = HBox(cBox2, label2)
        box2.alignment = Pos.CENTER_LEFT

        val moveTo = TextField()
        moveTo.onAction = EventHandler { cBox1.moveTo(moveTo.text.toInt()) }

        vBox.children.addAll(box1, box2, moveTo)
    }

    override fun start(primaryStage: Stage) {
        primaryStage.scene = Scene(vBox, 300.0, 75.0)
        primaryStage.show()
    }
}