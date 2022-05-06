package ink.meodinger.lpfx.component.tools

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.property.*
import ink.meodinger.lpfx.util.string.emptyString
import ink.meodinger.lpfx.util.image.resizeByRadius

import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.HPos
import javafx.geometry.Pos
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


class FormatChecker(private val state: State) : Stage() {

    private data class Typo(val regex: Regex, val autofix: String, val description: String)
    private data class TypoInfo(val picName: String, val labelIndex: Int, val typo: Typo)

    companion object {
        private val TypoList: List<Typo> = listOf(
            Typo(Regex("\n(\n)+"), "\n", I18N["format.lines"]),
            Typo(Regex("\\.(\\.)+"), "\u2026", I18N["format.dots"]),
            Typo(Regex("\u3002(\u3002)+"), "\u2026", I18N["format.dots"]),
        )
    }

    private val typoListProperty: ListProperty<TypoInfo> = SimpleListProperty(FXCollections.observableArrayList())
    private val typoList: ObservableList<TypoInfo> by typoListProperty

    private val indexProperty: IntegerProperty = SimpleIntegerProperty(NOT_FOUND)
    private var index: Int by indexProperty

    init {
        //   HBox  0    1
        // 0 Alert  description
        // 1 image Next Complete
        icons.add(ICON)
        title = I18N["checker.title"]
        width = 300.0
        height = 150.0
        isResizable = false
        scene = Scene(GridPane().apply {
            alignment = Pos.CENTER
            hgap = 16.0
            vgap = 8.0

            add(ImageView(), 0, 0, 1, 2) {
                image = IMAGE_WARNING.resizeByRadius(GENERAL_ICON_RADIUS)
            }
            add(Label(), 1, 0, 3, 1) {
                gridHAlign = HPos.CENTER
                textAlignment = TextAlignment.CENTER
                textProperty().bind(indexProperty.transform {
                    String.format(
                        I18N["checker.label.iis"],
                        it + 1,
                        typoList.size,
                        typoList.getOrNull(it)?.typo?.description ?: emptyString()
                    )
                })
            }
            add(Button(I18N["checker.continue"]), 1, 1) {
                gridHAlign = HPos.CENTER
                disableProperty().bind(indexProperty eq (typoListProperty.sizeProperty() - 1))

                does {
                    if (index + 1 < typoList.size) index++
                }
            }
            add(Button(I18N["checker.autofix"]), 2, 1) {
                gridHAlign = HPos.CENTER
                disableProperty().bind(indexProperty eq (typoListProperty.sizeProperty() - 1))

                does {
                    val typo = typoList[index].typo
                    if (typo.regex.matches(state.view.cTransArea.selectedText)) {
                        state.view.cTransArea.replaceSelection(typo.autofix)
                    }
                }
            }
            add(Button(I18N["checker.complete"]), 3, 1) {
                gridHAlign = HPos.CENTER
                does {
                    close()
                    state.controller.save(state.translationFile, true)
                }
            }
        })

        indexProperty.addListener(onNew<Number, Int> {
            if (it == NOT_FOUND) return@onNew
            val (picName, labelIndex, typo) = typoList[it]

            val match =
                if (state.currentPicName == picName && state.currentLabelIndex == labelIndex) {
                    typo.regex.find(state.view.cTransArea.text, state.view.cTransArea.caretPosition)
                } else {
                    typo.regex.find(state.transFile.getTransLabel(picName, labelIndex).text)
                } ?: return@onNew

            Logger.debug("Checker found: (${match.range}) -> `${match.value}`", "FormatChecker")

            // Select Picture
            state.currentPicName = picName
            // Select Label (Use TreeView select to set State & View at the same time)
            state.view.cTreeView.selectLabel(labelIndex, clear = true, scrollTo = true)
            // Select Range
            state.view.cTransArea.selectRange(match.range.first, match.range.last + 1)
        })
        closeOnEscape()
    }

    private fun collectTypos() {
        index = NOT_FOUND
        typoList.clear()

        for (picName in state.transFile.sortedPicNames) {
            for (label in state.transFile.getTransList(picName)) {
                var start = 0

                var found: Boolean
                while (true) {
                    found = false
                    for (typo in TypoList) {
                        val result = typo.regex.find(label.text, start) ?: continue

                        found = true
                        typoList.add(TypoInfo(picName, label.index, typo))
                        start = result.range.last
                        break
                    }
                    if (!found) break
                }
            }
        }
    }

    /**
     * Check if there are any typos in the TransFile
     * @return `true` if there is/are, `false` if there isn't
     */
    fun check(): Boolean {
        collectTypos()

        if (typoList.isEmpty()) return true

        index = 0
        return false
    }

}
