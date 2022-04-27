package ink.meodinger.lpfx.component.tools

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.action.ActionType
import ink.meodinger.lpfx.action.ComplexAction
import ink.meodinger.lpfx.action.LabelAction
import ink.meodinger.lpfx.util.component.add
import ink.meodinger.lpfx.util.component.closeOnEscape
import ink.meodinger.lpfx.util.component.gridHAlign
import ink.meodinger.lpfx.util.component.withContent
import ink.meodinger.lpfx.util.dialog.showConfirm
import ink.meodinger.lpfx.util.dialog.showInfo
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.string.emptyString

import javafx.beans.property.BooleanProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import javafx.stage.Stage

/**
 * Author: Meodinger
 * Date: 2022/3/24
 * Have fun with my code!
 */
class SearchReplace(private val state: State) : Stage() {

    companion object {
        private const val BUTTON_WIDTH: Double = 96.0
    }

    private val searchTextProperty: StringProperty = SimpleStringProperty(emptyString())
    private val searchText: String by searchTextProperty

    private val replaceTextProperty: StringProperty = SimpleStringProperty(emptyString())
    private val replaceText: String by replaceTextProperty

    private val wrapFindProperty: BooleanProperty = SimpleBooleanProperty(true)
    private val wrapFind: Boolean by wrapFindProperty

    private val ignoreCaseProperty: BooleanProperty = SimpleBooleanProperty(false)
    private val ignoreCase: Boolean by ignoreCaseProperty

    private val foundIndexProperty: IntegerProperty = SimpleIntegerProperty(NOT_FOUND)
    private var foundIndex: Int by foundIndexProperty

    init {
        icons.add(ICON)
        title = I18N["snr.title"]
        width = PANE_WIDTH * 0.8
        height = PANE_HEIGHT / 2
        isResizable = false
        scene = Scene(StackPane().withContent(GridPane()) {
            padding = Insets(COMMON_GAP)
            hgap = COMMON_GAP
            vgap = COMMON_GAP / 2
            alignment = Pos.CENTER

            //    0        1            2
            // 0    Find: |          | FindNext
            // 1 Replace: |          | Replace
            // 2 O Backward            ReplaceAll
            // 3 O CaseInsensitive     Cancel
            add(Label(I18N["snr.text_search"]), 0, 0) {
                gridHAlign = HPos.RIGHT
            }
            add(Label(I18N["snr.text_replace"]), 0, 1) {
                gridHAlign = HPos.RIGHT
            }
            add(TextField(), 1, 0) {
                prefColumnCount = 20
                searchTextProperty.bind(textProperty())
            }
            add(TextField(), 1, 1) {
                prefColumnCount = 20
                replaceTextProperty.bind(textProperty())
            }
            add(CheckBox(I18N["snr.wrap_find"]), 0, 2, 2, 1) {
                isSelected = true
                wrapFindProperty.bind(selectedProperty())
            }
            add(CheckBox(I18N["snr.ignore_case"]), 0, 3, 2, 1) {
                isSelected = false
                ignoreCaseProperty.bind(selectedProperty())
            }

            add(Button(I18N["snr.find_next"]), 2, 0) {
                prefWidth = BUTTON_WIDTH
                disableProperty().bind(searchTextProperty.isEmpty)
                setOnAction { handleFindNext() }
            }
            add(Button(I18N["snr.replace"]), 2, 1) {
                prefWidth = BUTTON_WIDTH
                disableProperty().bind(searchTextProperty.isEmpty)
                setOnAction { handleReplace() }
            }
            add(Button(I18N["snr.replace_all"]), 2, 2) {
                prefWidth = BUTTON_WIDTH
                disableProperty().bind(searchTextProperty.isEmpty)
                setOnAction { handleReplaceAll() }
            }
            add(Button(I18N["common.cancel"]), 2, 3) {
                prefWidth = BUTTON_WIDTH
                setOnAction { hide() }
            }
        })

        closeOnEscape()
    }

    private fun findNext(wrap: Boolean): Boolean {
        // If in current label
        val currentLabel = state.transFile.getTransLabel(state.currentPicName, state.currentLabelIndex)
        val findStart = if (foundIndex != NOT_FOUND) foundIndex + searchText.length else 0
        val nextIndex = currentLabel.text.indexOf(searchText, findStart, ignoreCase)
        if (nextIndex != NOT_FOUND) {
            foundIndex = nextIndex
            state.view.cTransArea.selectRange(nextIndex, nextIndex + searchText.length)
            return true
        }

        // If in current picture
        val currentLabels = state.transFile.getTransList(state.currentPicName)
        for (i in state.currentLabelIndex until currentLabels.size) {
            val index = currentLabels[i].text.indexOf(searchText, 0, ignoreCase)
            if (index != NOT_FOUND) {
                foundIndex = index
                state.currentLabelIndex = currentLabels[i].index
                state.view.cTransArea.selectRange(index, index + searchText.length)
                return true
            }
        }

        // Find in the rest
        val picNames = state.transFile.sortedPicNames
        val picIndex = picNames.indexOf(state.currentPicName)
        for (i in picNames.indices) {
            // continue if not wrap search
            if (i < picIndex && !wrap) continue

            val labels = state.transFile.getTransList(picNames[i])
            for (label in labels) {
                val index = label.text.indexOf(searchText, 0, ignoreCase)
                if (index != NOT_FOUND) {
                    foundIndex = index
                    state.currentPicName = picNames[i]
                    state.currentLabelIndex = label.index
                    state.view.cTransArea.selectRange(foundIndex, foundIndex + searchText.length)
                    return true
                }
            }
        }

        return false
    }
    private fun replace() {
        state.view.cTransArea.replaceText(IndexRange(foundIndex, foundIndex + searchText.length), replaceText)
        foundIndex = NOT_FOUND
    }
    private fun replaceAll(): Int {
        foundIndex = NOT_FOUND

        var count = 0
        val actions = ArrayList<LabelAction>()
        for (picName in state.transFile.sortedPicNames) {
            for (label in state.transFile.getTransList(picName)) {
                var index = label.text.indexOf(searchText, 0, ignoreCase)
                if (index == NOT_FOUND) continue

                var text = label.text
                while (index != NOT_FOUND) {
                    count++
                    text = text.replaceRange(index, index + searchText.length, replaceText)
                    index = text.indexOf(searchText, index, ignoreCase)
                }
                actions.add(LabelAction(ActionType.CHANGE, state, picName, label, newText = text))
            }
        }
        state.doAction(ComplexAction(actions))
        return count
    }

    private fun handleFindNext() {
        if (!findNext(wrapFind)) {
            val result = showConfirm(this@SearchReplace, I18N["snr.not_found_re"])
            if (result.isPresent && result.get() == ButtonType.YES) {
                if (!findNext(true)) showInfo(this@SearchReplace, I18N["snr.not_found"])
            }
        }
    }
    private fun handleReplace() {
        if (foundIndex == NOT_FOUND) handleFindNext() else replace()
    }
    private fun handleReplaceAll() {
        val count = replaceAll()
        if (count == 0) {
            showInfo(this@SearchReplace, I18N["snr.not_found"])
        } else {
            showInfo(this@SearchReplace, String.format(I18N["snr.replace_count.i"], count))
        }
    }

}
