package component

import info.meodinger.lpfx.component.CLabelPane
import info.meodinger.lpfx.component.CLabelPane.LabelEvent
import info.meodinger.lpfx.type.TransGroup
import info.meodinger.lpfx.type.TransLabel
import io.commonTest

import javafx.application.Application
import javafx.beans.binding.ListBinding
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.scene.Cursor
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.SplitPane
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import kotlin.math.roundToInt

/**
 * Author: Meodinger
 * Date: 2021/8/2
 * Location: component
 */
class LabelPane : Application() {

    companion object {
        private const val WIDTH = 600.0
        private const val HEIGHT = 600.0

        private const val picName = "1.jpg"

    }

    val transFile = commonTest()

    val s = SplitPane()
    val box = VBox()
    val pane = CLabelPane()

    val moveToButton = Button("Move to")
    val moveToField = TextField("1")
    val changeColorButton = Button("Change Color")
    val changeIndexButton = Button("Change Index")
    val addLayerButton = Button("Add Layer")
    val removeLayerButton = Button("Remove Layer")

    val textArea = TextArea()


    init {

        pane.minScale = 0.2
        pane.maxScale = 2.0
        pane.defaultCursor = Cursor.CROSSHAIR

        fun createColorHexBinding(): ListBinding<String> {
            return object : ListBinding<String>() {
                init {
                    for (i in 0 until transFile.groupList.size) {
                        bind(transFile.groupList[i].colorProperty)
                    }
                }

                override fun computeValue(): ObservableList<String> {
                    val list = ArrayList<String>()
                    transFile.groupList.forEach {
                        list.add(it.color)
                    }
                    return FXCollections.observableList(list)
                }
            }
        }
        pane.colorListProperty.bind(createColorHexBinding())

        pane.handleInputMode = EventHandler {
            when (it.eventType) {
                LabelEvent.LABEL_OTHER -> {
                    textArea.appendText("I other nothing")
                }
                LabelEvent.LABEL_PLACE -> {
                    textArea.appendText("I place nothing")
                }
                LabelEvent.LABEL_REMOVE -> {
                    textArea.appendText("I remove nothing")
                }
                LabelEvent.LABEL_POINTED -> {
                    textArea.appendText("I pointed ${it.labelIndex}")
                }
                LabelEvent.LABEL_CLICKED -> {
                    val transLabel = transFile.getTransLabelAt(picName, it.labelIndex)
                    textArea.appendText("I clicked ${transLabel}")
                }
            }
            textArea.appendText("\n")
        }
        pane.handleLabelMode = EventHandler {
            when (it.eventType) {
                LabelEvent.LABEL_OTHER -> {
                    textArea.appendText("L other nothing")
                }
                LabelEvent.LABEL_PLACE -> {
                    val transLabel = TransLabel(
                        transFile.getTransLabelListOf(picName).size + 1,
                        it.labelX,
                        it.labelY,
                        0,
                        "NewText@${(Math.random() * 1000).roundToInt()}"
                    )
                    transFile.getTransLabelListOf(picName).add(transLabel)
                    pane.placeLabel(transLabel)

                    // x/y is 0.0 because of bind
                    textArea.appendText("L place $transLabel")
                }
                LabelEvent.LABEL_REMOVE -> {
                    val transLabel = transFile.getTransLabelAt(picName, it.labelIndex)

                    transFile.getTransLabelListOf(picName).remove(transLabel)
                    for (label in transFile.getTransLabelListOf(picName)) {
                        if (label.index > transLabel.index) label.index -= 1
                    }

                    pane.removeLabel(transLabel)
                    textArea.appendText("L remove $transLabel")
                }
                LabelEvent.LABEL_POINTED -> {
                    textArea.appendText("L pointed ${it.labelIndex}")
                }
                LabelEvent.LABEL_CLICKED -> {
                    val transLabel = transFile.getTransLabelAt(picName, it.labelIndex)
                    textArea.appendText("L clicked ${transLabel}")
                }
            }
            textArea.appendText("\n")
        }

        pane.setupImage("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\src\\test\\resources\\sample\\1.jpg")
        pane.setupLayers(transFile.groupList.size)
        pane.setupLabels(transFile.transMap[picName]!!)

        moveToButton.setOnAction {
            pane.moveToLabel(transFile.transMap[picName]!![moveToField.text.toInt() - 1])
        }
        changeColorButton.setOnAction {
            transFile.groupList[0].color = "66ccff"
        }
        changeIndexButton.setOnAction {
            transFile.transMap[picName]!![0].index += 2
        }
        addLayerButton.setOnAction {
            // data -> bind -> add

            val group = TransGroup(color = "B3D365")
            val label = TransLabel(
                transFile.getTransLabelListOf(picName).size + 1,
                Math.random(), Math.random(),
                transFile.groupList.size,
                "New Layer Label"
            )

            transFile.groupList.add(group)
            transFile.getTransLabelListOf(picName).add(label)

            pane.colorListProperty.bind(createColorHexBinding())

            pane.placeLabelLayer()
            pane.placeLabel(label)
        }
        removeLayerButton.setOnAction {
            // data -> remove -> bind

            val id = transFile.groupList.size - 1
            val group = transFile.groupList[id]

            transFile.groupList.remove(group)
            val list = transFile.getTransLabelListOf(picName)
            val listToRemove = ArrayList<TransLabel>()
            for (label in list) {
                if (label.groupId == id) {
                    listToRemove.add(label)
                }
            }
            list.removeAll(listToRemove)

            pane.removeLabelLayer(id)

            pane.colorListProperty.bind(createColorHexBinding())
        }

        textArea.prefHeight = 600.0
        box.children.add(HBox(moveToButton, moveToField, changeColorButton, changeIndexButton, addLayerButton, removeLayerButton))
        box.children.add(textArea)

        s.orientation = Orientation.VERTICAL
        s.items.addAll(pane, box)
    }

    override fun start(primaryStage: Stage) {
        primaryStage.scene = Scene(s, WIDTH, HEIGHT)
        primaryStage.show()
    }

}