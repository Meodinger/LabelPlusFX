package ink.meodinger.lpfx.component.tools

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.util.component.add
import ink.meodinger.lpfx.util.component.does
import ink.meodinger.lpfx.util.component.withContent
import ink.meodinger.lpfx.util.image.resizeByRadius
import ink.meodinger.lpfx.util.resource.ICON
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.onNew
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.resource.loadAsImage
import ink.meodinger.lpfx.util.string.emptyString
import ink.meodinger.lpfx.util.string.replaceEOL

import javafx.beans.binding.Bindings
import javafx.beans.property.IntegerProperty
import javafx.beans.property.ListProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import javafx.stage.Stage

/**
 * Author: Meodinger
 * Date: 2022/4/6
 * Have fun with my code!
 */


class TextChecker(private val state: State) : Stage() {

    companion object {
        private val TypoTexts: List<String> = listOf("\n\n", "..")
    }

    private val typoListProperty: ListProperty<Pair<Pair<String, Int>, Pair<String, Pair<Int, Int>>>> = SimpleListProperty(FXCollections.observableArrayList())
    private val typoList: MutableList<Pair<Pair<String, Int>, Pair<String, Pair<Int, Int>>>> by typoListProperty

    private val indexProperty: IntegerProperty = SimpleIntegerProperty(NOT_FOUND)
    private var index: Int by indexProperty

    init {
        //   0     1     2
        // 0 Alert #x of #total
        // 1 image Next  Complete
        icons.add(ICON)
        title = "Fix Texts"
        width = PANE_WIDTH / 2
        height = PANE_HEIGHT / 3
        isResizable = false
        scene = Scene(StackPane().withContent(GridPane()) {
            padding = Insets(COMMON_GAP)
            hgap = COMMON_GAP
            vgap = COMMON_GAP / 2
            alignment = Pos.CENTER

            add(ImageView(loadAsImage("/file/image/dialog/Alert.png").resizeByRadius(GENERAL_ICON_RADIUS)), 0, 0, 1, 2)
            add(Label(), 1, 0, 2, 1) {
                textProperty().bind(Bindings.createStringBinding(
                    {
                        if (index != NOT_FOUND)
                           "#${index + 1} of #${typoListProperty.size} (${typoList[index].second.first.replaceEOL()})"
                        else emptyString()
                    },
                    indexProperty, typoListProperty.sizeProperty()
                ))
            }
            add(Button("Next"), 1, 1) {
                does { if (index + 1 < typoList.size) index++ else close() }
            }
            add(Button("Complete"), 2, 1) {
                does { close() }
            }
        })

        indexProperty.addListener(onNew<Number, Int> {
            if (it == NOT_FOUND) return@onNew
            val (location, typo) = typoList[it]

            state.currentPicName = location.first
            state.currentLabelIndex = location.second
            state.view.cTreeView.selectionModel.clearSelection()
            state.view.cTransArea.selectRange(typo.second.first, typo.second.second)
        })
    }

    private fun collectTypos() {
        typoList.clear()

        for (picName in state.transFile.sortedPicNames) {
            for (label in state.transFile.getTransList(picName)) {
                var index = NOT_FOUND

                while (true) {
                    val (start, error) = label.text.findAnyOf(TypoTexts, index) ?: break
                    val location = picName to label.index
                    val range = start to (start + error.length)

                    typoList.add((location) to (error to range))
                    index = start + error.length
                }
            }
        }
    }

    fun check(): Boolean {
        collectTypos()
        if (typoList.isEmpty()) return true

        index = 0

        show()
        return false
    }

}
