package ink.meodinger.lpfx.component.common

import ink.meodinger.lpfx.util.event.isDoubleClick
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.onNew
import ink.meodinger.lpfx.util.property.setValue

import javafx.beans.property.*
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.scene.input.MouseButton
import javafx.scene.layout.Pane


/**
 * Author: Meodinger
 * Date: 2021/9/26
 * Have fun with my code!
 */

/**
 * A Label when double-clicked will become a TextField
 */
class CInputLabel : Pane() {

    companion object {
        private const val DEFAULT_WIDTH: Double = 48.0
        private const val DEFAULT_HEIGHT: Double = 24.0
    }

    private val label = Label()
    private val field = TextField()

    private val textFormatterProperty: ObjectProperty<TextFormatter<*>> = field.textFormatterProperty()
    /**
     * An export to `TextField::textFormatterProperty()`
     * @see javafx.scene.control.TextInputControl.textFormatter
     */
    fun textFormatterProperty(): ObjectProperty<TextFormatter<*>> = textFormatterProperty
    /**
     * @see textFormatterProperty
     */
    var textFormatter: TextFormatter<*> by textFormatterProperty

    private val editingProperty: BooleanProperty = SimpleBooleanProperty(false)
    /**
     * Whether the CInputLabel is being edting (show as a TextField) or not (show as a Label)
     */
    fun editingProperty(): BooleanProperty = editingProperty
    /**
     * @see editingProperty
     */
    var isEditing: Boolean by editingProperty

    private val labelTextProperty: StringProperty = label.textProperty()
    /**
     * The `textproperty()` of the Label
     * @see javafx.scene.control.Labeled.textProperty
     */
    fun labelTextProperty(): StringProperty = labelTextProperty
    /**
     * @see labelTextProperty
     */
    var labelText: String by labelTextProperty

    private val fieldTextProperty: StringProperty = field.textProperty()
    /**
     * The `textproperty()` of the TextField
     * @see javafx.scene.control.TextInputControl.text
     */
    fun fieldTextProperty(): StringProperty = fieldTextProperty
    /**
     * @see fieldTextProperty
     */
    var fieldText: String by fieldTextProperty

    /**
     * Current displaying text. It is `fieldText` if editing or else `labelText`
     */
    var text: String
        get() {
            return if (isEditing) fieldText else labelText
        }
        set(value) {
            if (isEditing) fieldText = value else labelText = value
        }

    private val onChangeToFieldProperty: ObjectProperty<CInputLabel.() -> Unit> = SimpleObjectProperty {
        if (!fieldTextProperty.isBound) fieldText = labelText
    }
    /**
     * This function will be called when Label is double-clicked.
     * FieldText will be set to the return value of this function.
     * Then Label will hide and TextField will show.
     */
    fun onChangeToFieldProperty(): ObjectProperty<CInputLabel.() -> Unit> = onChangeToFieldProperty
    /**
     * @see onChangeToFieldProperty
     */
    val onChangeToField: CInputLabel.() -> Unit by onChangeToFieldProperty
    /**
     * @see onChangeToFieldProperty
     */
    fun setOnChangeToField(callback: CInputLabel.() -> Unit) = onChangeToFieldProperty.set(callback)

    private val onChangeToLabelProperty: ObjectProperty<CInputLabel.() -> Unit> = SimpleObjectProperty {
        if (!labelTextProperty.isBound) labelText = fieldText
    }
    /**
     * This function will be called when enter-key fired on TextField.
     * LabelText will be set to the return value of this function.
     * Then TextField will hide and Label will show.
     */
    fun onChangeToLabelProperty(): ObjectProperty<CInputLabel.() -> Unit> = onChangeToLabelProperty
    /**
     * @see onChangeToLabelProperty
     */
    val onChangeToLabel: CInputLabel.() -> Unit by onChangeToLabelProperty
    /**
     * @see onChangeToLabelProperty
     */
    fun setOnChangeToLabel(callback: CInputLabel.() -> Unit) = onChangeToLabelProperty.set(callback)

    init {
        setPrefSize(DEFAULT_WIDTH, DEFAULT_HEIGHT)

        label.prefWidthProperty().bind(prefWidthProperty())
        label.prefHeightProperty().bind(prefHeightProperty())
        field.prefWidthProperty().bind(prefWidthProperty())
        field.prefHeightProperty().bind(prefHeightProperty())

        editingProperty.addListener(onNew {
            children.clear()
            children.add(if (it) field else label)
        })

        label.setOnMouseClicked {
            if (it.button != MouseButton.PRIMARY) return@setOnMouseClicked
            if (!it.isDoubleClick) return@setOnMouseClicked

            onChangeToField(this)
            isEditing = true
        }
        field.setOnAction {
            onChangeToLabel(this)
            isEditing = false
        }

        children.add(label)
    }

}
