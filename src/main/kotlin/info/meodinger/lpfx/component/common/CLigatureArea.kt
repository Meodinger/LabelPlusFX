package info.meodinger.lpfx.component.common

import info.meodinger.lpfx.util.property.getValue
import info.meodinger.lpfx.util.property.setValue
import javafx.application.Platform

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.StringProperty
import javafx.scene.control.TextArea
import javafx.scene.control.TextFormatter


/**
 * Author: Meodinger
 * Date: 2021/8/16
 * Location: info.meodinger.lpfx.component
 */

/**
 * A TextArea with a symbol ContextMenu
 */
class CLigatureArea: TextArea() {

    companion object {
        private const val LIGATURE_MAX_LENGTH: Int = 10
        private const val LIGATURE_MARK: String = "\\"
        private val DEFAULT_RULES: List<Pair<String, String>> = listOf(
            Pair("(", "「"),
            Pair(")", "」"),
            Pair("（", "『"),
            Pair("）", "』"),
            Pair("*", "※"),
            Pair("cc", "◎"),
            Pair("star", "⭐"),
            Pair("square", "♢"),
            Pair("heart", "♡"),
            Pair("music", "♪")
        )
    }

    private var ligaturing: Boolean = false
    private var ligatureStart: Int = 0
    private var ligatureString: String = ""

    private val ligatureMaxLength: Int = LIGATURE_MAX_LENGTH
    private val ligatureRules: List<Pair<String, String>> = DEFAULT_RULES
    private val ligatureMark: String = LIGATURE_MARK

    private val boundTextPropertyProperty = SimpleObjectProperty<StringProperty>(null)
    private var boundTextProperty: StringProperty? by boundTextPropertyProperty
    val isBound: Boolean get() = boundTextProperty != null

    init {
        this.textFormatter = TextFormatter<String> { change ->
            if (change.isAdded) {
                if (change.text == ligatureMark) {
                    ligatureStart(this.caretPosition)
                    return@TextFormatter change
                }

                ligatureString += change.text

                if (ligatureString.length <= ligatureMaxLength) {
                    if (ligaturing) for (rule in ligatureRules) if (rule.first == ligatureString) {
                        this.text = this.text.replaceRange(ligatureStart, this.caretPosition, rule.second)
                        change.text = ""
                        ligatureEnd()

                        Platform.runLater { positionCaret(ligatureStart + rule.second.length) }
                    }
                } else {
                    ligatureEnd()
                }
            } else if (change.isDeleted) {
                if (ligaturing) {
                    val end = ligatureString.length - 1 - change.text.length

                    if (end >= 0) {
                        ligatureString = ligatureString.substring(0, end)
                    } else {
                        ligatureEnd()
                    }
                }
            } else {
                ligatureEnd()
            }

            change
        }
    }

    fun reset() {
        unbindBidirectional()
    }
    private fun ligatureStart(startCaret: Int) {
        ligaturing = true
        ligatureStart = startCaret
        ligatureString = ""
    }
    private fun ligatureEnd() {
        ligaturing = false
        ligatureStart = 0
        ligatureString = ""
    }

    fun bindBidirectional(property: StringProperty) {
        textProperty().bindBidirectional(property)
        boundTextProperty = property
    }
    fun unbindBidirectional() {
        if (boundTextProperty == null)  return

        textProperty().unbindBidirectional(boundTextProperty)
        boundTextProperty = null
        text = ""
    }

}