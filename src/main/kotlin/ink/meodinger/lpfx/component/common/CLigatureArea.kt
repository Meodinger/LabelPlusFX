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
 * A TextArea that can transform some text into some other text by given rules
 *
 * For examples, if we have the rule "From `foo` to `bar`" and if we type "\foo",
 * it will be transformed into "bar". Note that the ligature mark "\" can be set
 * to some other characters.
 */
class CLigatureArea: TextArea() {

    companion object {
        private const val LIGATURE_MARK: String = "\\"
        private const val LIGATURE_MAX_LENGTH: Int = 10
    }

    private val ligatureMarkProperty: StringProperty = SimpleStringProperty(LIGATURE_MARK)
    /**
     * The start mark of a ligature input, should be a char but there isn't `CharProperty`,
     * so use `StringProperty` instead. Actually use `String` as start mark is possible but
     * is hard to produce a text change adds multi chars at once (you should use copy/paste
     * or something else) so make sure this value is a single char.
     */
    fun ligatureMarkProperty(): StringProperty = ligatureMarkProperty
    /**
     * @see ligatureMarkProperty
     */
    var ligatureMark: String by ligatureMarkProperty

    private val ligatureMaxLengthProperty: IntegerProperty = SimpleIntegerProperty(LIGATURE_MAX_LENGTH)
    /**
     * The max ligature input length. If a ligature action accmulates chars more than this
     * amount, it will automatically stop its parse procedure.
     */
    fun ligatureMaxLengthProperty(): IntegerProperty = ligatureMaxLengthProperty
    /**
     * @see ligatureMaxLengthProperty
     */
    var ligatureMaxLength: Int by ligatureMaxLengthProperty

    private val ligatureRulesProperty: ListProperty<Pair<String, String>> = SimpleListProperty(FXCollections.emptyObservableList())
    /**
     * All ligature rules.
     */
    fun ligatureRulesProperty(): ListProperty<Pair<String, String>> = ligatureRulesProperty
    /**
     * @see ligatureRulesProperty
     */
    var ligatureRules: ObservableList<Pair<String, String>> by ligatureRulesProperty

    private val boundTextPropertyProperty: ObjectProperty<StringProperty> = SimpleObjectProperty(null)

    /**
     * Whether this LigatureArea is bound to a StringProperty
     */
    val isBound: Boolean by boundTextPropertyProperty.isNotNull

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
                if (ligaturing) for ((from, to) in ligatureRules) if (from == ligatureString) {
                    val ligatureEnd = caretPosition
                    val caretPosition = ligatureStart + to.length

                    text = text.replaceRange(ligatureStart, ligatureEnd, to)

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
        // Make text-formatter immutable
        textFormatterProperty().bind(ReadOnlyObjectWrapper(defaultTextFormatter))
    }

    /**
     * Bind the StringProperty of this TextArea to another StringProperty bidirectionally.
     */
    fun bindText(property: StringProperty) {
        textProperty().bindBidirectional(property)
        boundTextPropertyProperty.set(property)
    }

    /**
     * Unbind the bound StringProperty (if not null), and clear text.
     */
    fun unbindText() {
        val bound = boundTextPropertyProperty.get() ?: return

        textProperty().unbindBidirectional(bound)
        boundTextPropertyProperty.set(null)
        text = ""
    }

}
