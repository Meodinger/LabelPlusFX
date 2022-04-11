package ink.meodinger.lpfx.component.tools

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.image.resizeByRadius
import ink.meodinger.lpfx.util.resource.ICON
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.onNew
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.resource.loadAsImage
import ink.meodinger.lpfx.util.string.emptyString

import javafx.beans.binding.Bindings
import javafx.beans.property.IntegerProperty
import javafx.beans.property.ListProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.text.TextAlignment
import javafx.stage.Stage

/**
 * Author: Meodinger
 * Date: 2022/4/6
 * Have fun with my code!
 */


class TextChecker(private val state: State) : Stage() {

    companion object {
        private val Typos: Map<String, String> = mapOf(
            "\n\n" to "Empty Lines",
            ".."   to "Incorrect Dots"
        )
    }

    private val typoListProperty: ListProperty<Pair<Pair<String, Int>, String>> = SimpleListProperty(FXCollections.observableArrayList())
    private val typoList: MutableList<Pair<Pair<String, Int>, String>> by typoListProperty

    private val indexProperty: IntegerProperty = SimpleIntegerProperty(NOT_FOUND)
    private var index: Int by indexProperty

    init {
        //   HBox  0     1
        // 0 Alert #x of #total
        // 1 image Next  Complete
        icons.add(ICON)
        title = "Fix Texts"
        width = PANE_WIDTH / 2
        height = PANE_HEIGHT / 3
        isResizable = false
        scene = Scene(StackPane().withContent(HBox()) {
            padding = Insets(COMMON_GAP)

            add(ImageView(loadAsImage("/file/image/dialog/Alert.png").resizeByRadius(GENERAL_ICON_RADIUS)))
            add(GridPane()) {
                hgap = COMMON_GAP
                vgap = COMMON_GAP / 2

                // Why 4 GAPs? Stage.width != Scene.width
                val colWidth = (this@TextChecker.width - GENERAL_ICON_RADIUS * 2 - 4 * COMMON_GAP) / 2
                columnConstraints.addAll(ColumnConstraints(colWidth), ColumnConstraints(colWidth))

                add(Label(), 0, 0, 2, 1) {
                    gridHAlign = HPos.CENTER
                    textAlignment = TextAlignment.CENTER
                    textProperty().bind(Bindings.createStringBinding(
                        {
                            if (index != NOT_FOUND)
                                """
                                #${index + 1} of #${typoListProperty.size}
                                ==> ${Typos[typoList[index].second]} <==
                                """.trimIndent()
                            else emptyString()
                        },
                        indexProperty, typoListProperty.sizeProperty()
                    ))
                }
                add(Button("Next One"), 0, 1) {
                    gridHAlign = HPos.CENTER
                    does { if (index + 1 < typoList.size) index++ else close() }
                }
                add(Button("Complete"), 1, 1) {
                    gridHAlign = HPos.CENTER
                    does { close() }
                }
            }
        })

        indexProperty.addListener(onNew<Number, Int> {
            if (it == NOT_FOUND) return@onNew
            val (location, typo) = typoList[it]
            val (picName, labelIndex) = location

            val index: Int
            if (state.currentPicName == picName && state.currentLabelIndex == labelIndex) {
                val caret = if (state.view.cTransArea.selectedText != typo) 0 else state.view.cTransArea.caretPosition
                index = state.view.cTransArea.text.indexOf(typo, caret)

                if (index == NOT_FOUND) return@onNew
            } else {
                index = state.transFile.getTransLabel(location.first, location.second).text.indexOf(typo)
            }

            state.currentPicName = picName
            state.currentLabelIndex = labelIndex
            state.view.cTreeView.selectionModel.clearSelection()
            state.view.cTransArea.selectRange(index, index + typo.length)
        })
    }

    private fun collectTypos() {
        typoList.clear()

        for (picName in state.transFile.sortedPicNames) {
            for (label in state.transFile.getTransList(picName)) {
                var index = NOT_FOUND

                while (true) {
                    val (start, error) = label.text.findAnyOf(Typos.keys, index) ?: break
                    val location = picName to label.index

                    typoList.add(location to error)
                    index = start + error.length
                }
            }
        }
    }

    fun check(): Boolean {
        index = NOT_FOUND
        collectTypos()

        if (typoList.isEmpty()) return true

        index = 0
        return false
    }

}
