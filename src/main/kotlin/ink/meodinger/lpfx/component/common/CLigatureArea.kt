package ink.meodinger.lpfx.component.common

import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue

import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.TextArea
import javafx.scene.control.TextFormatter


/**
 * Author: Meodinger
 * Date: 2021/8/16
 * Have fun with my code!
 */

/**
 * A TextArea with a symbol ContextMenu
 */
class CLigatureArea: TextArea() {

    companion object {
        private const val LIGATURE_MAX_LENGTH: Int = 10
        private const val LIGATURE_MARK: String = "\\"
        private val DEFAULT_RULES: List<Pair<String, String>> = listOf("cc" to "◎")
    }

    private var ligaturing: Boolean = false
    private var ligatureStart: Int = 0
    private var ligatureString: String = ""

    private val ligatureMaxLengthProperty: IntegerProperty = SimpleIntegerProperty(LIGATURE_MAX_LENGTH)
    fun ligatureMaxLengthProperty(): IntegerProperty = ligatureMaxLengthProperty
    var ligatureMaxLength: Int by ligatureMaxLengthProperty

    private val ligatureRulesProperty: ListProperty<Pair<String, String>> = SimpleListProperty(FXCollections.observableArrayList(DEFAULT_RULES))
    fun ligatureRulesProperty(): ListProperty<Pair<String, String>> = ligatureRulesProperty
    var ligatureRules: ObservableList<Pair<String, String>> by ligatureRulesProperty

    private val ligatureMarkProperty: StringProperty = SimpleStringProperty(LIGATURE_MARK)
    fun ligatureMarkProperty(): StringProperty = ligatureMarkProperty
    var ligatureMark: String by ligatureMarkProperty

    private val boundTextPropertyProperty = SimpleObjectProperty<StringProperty>(null)
    private var boundTextProperty: StringProperty? by boundTextPropertyProperty
    val boundProperty: StringProperty? get() = boundTextProperty
    val isBound: Boolean get() = boundTextProperty != null

    private val defaultTextFormatter = TextFormatter<String> { change ->
        if (change.isAdded) {
            if (change.text == ligatureMark) {
                ligatureStart(caretPosition)
                return@TextFormatter change
            }

            ligatureString += change.text

            if (ligatureString.length <= ligatureMaxLength) {
                if (ligaturing) for (rule in ligatureRules) if (rule.first == ligatureString) {
                    val ligatureEnd = caretPosition
                    val caretPosition = ligatureStart + rule.second.length

                    text = text.replaceRange(ligatureStart, ligatureEnd, rule.second)

                    change.text = ""
                    change.setRange(caretPosition, caretPosition)
                    change.caretPosition = caretPosition
                    change.anchor = caretPosition

                    ligatureEnd()
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

    init {
        resetFormatter()
    }

    fun reset() {
        unbindBidirectional()
    }
    fun resetFormatter() {
        textFormatter = defaultTextFormatter
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
