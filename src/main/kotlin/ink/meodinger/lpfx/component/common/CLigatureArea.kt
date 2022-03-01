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
        private const val LIGATURE_MARK: String = "\\"
        private const val LIGATURE_MAX_LENGTH: Int = 10
    }

    private val ligatureMarkProperty: StringProperty = SimpleStringProperty(LIGATURE_MARK)
    fun ligatureMarkProperty(): StringProperty = ligatureMarkProperty
    var ligatureMark: String by ligatureMarkProperty

    private val ligatureMaxLengthProperty: IntegerProperty = SimpleIntegerProperty(LIGATURE_MAX_LENGTH)
    fun ligatureMaxLengthProperty(): IntegerProperty = ligatureMaxLengthProperty
    var ligatureMaxLength: Int by ligatureMaxLengthProperty

    private val ligatureRulesProperty: ListProperty<Pair<String, String>> = SimpleListProperty(FXCollections.observableArrayList())
    fun ligatureRulesProperty(): ListProperty<Pair<String, String>> = ligatureRulesProperty
    var ligatureRules: ObservableList<Pair<String, String>> by ligatureRulesProperty

    private val boundTextPropertyProperty: ObjectProperty<StringProperty> = SimpleObjectProperty(null)
    val isBound: Boolean get() = boundTextPropertyProperty.get() != null

    // ----- Ligature ----- //

    private var ligaturing: Boolean = false
    private var ligatureStart: Int = 0
    private var ligatureString: String = ""
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
    private fun ligatureStart(caret: Int) {
        ligaturing = true
        ligatureStart = caret
        ligatureString = ""
    }
    private fun ligatureEnd() {
        ligaturing = false
        ligatureStart = 0
        ligatureString = ""
    }

    init {
        textFormatter = defaultTextFormatter
    }

    /**
     * Bind the StringProperty of this TextArea to another StringProperty bidirectionally.
     */
    fun bindBidirectional(property: StringProperty) {
        textProperty().bindBidirectional(property)
        boundTextPropertyProperty.set(property)
    }

    /**
     * Unbind the bound StringProperty (if not null), and clear text.
     */
    fun unbindBidirectional() {
        val bound = boundTextPropertyProperty.get() ?: return

        textProperty().unbindBidirectional(bound)
        boundTextPropertyProperty.set(null)
        text = ""
    }

}
