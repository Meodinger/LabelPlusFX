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

    private val textFormatterProperty: ObjectProperty<TextFormatter<String>> = SimpleObjectProperty()
    fun textFormatterProperty(): ObjectProperty<TextFormatter<String>> = textFormatterProperty
    var textFormatter: TextFormatter<String> by textFormatterProperty

    private val isEditingProperty: BooleanProperty = SimpleBooleanProperty(false)
    fun editingProperty(): BooleanProperty = isEditingProperty
    var isEditing: Boolean by isEditingProperty

    private val labelTextProperty: StringProperty = label.textProperty()
    fun labelTextProperty(): StringProperty = labelTextProperty
    var labelText: String by labelTextProperty

    private val fieldTextProperty: StringProperty = field.textProperty()
    fun fieldTextProperty(): StringProperty = fieldTextProperty
    var fieldText: String by fieldTextProperty

    var text: String
        get() {
            return if (isEditing) fieldText else labelText
        }
        set(value) {
            if (isEditing) fieldText = value else labelText = value
        }

    private val onChangeToFieldProperty: ObjectProperty<CInputLabel.() -> String> = SimpleObjectProperty { labelText }
    fun onChangeToFieldProperty(): ObjectProperty<CInputLabel.() -> String> = onChangeToFieldProperty
    val onChangeToField: CInputLabel.() -> String by onChangeToFieldProperty

    /**
     * This function will be called when Label is double-clicked.
     * FieldText will be set to the return value of this function.
     * Then Label will hide and TextField will show.
     */
    fun setOnChangeToField(callback: CInputLabel.() -> String) = onChangeToFieldProperty.set(callback)

    private val onChangeToLabelProperty: ObjectProperty<CInputLabel.() -> String> = SimpleObjectProperty { fieldText }
    fun onChangeToLabelProperty(): ObjectProperty<CInputLabel.() -> String> = onChangeToLabelProperty
    val onChangeToLabel: CInputLabel.() -> String by onChangeToLabelProperty

    /**
     * This function will be called when enter-key fired on TextField.
     * LabelText will be set to the return value of this function.
     * Then TextField will hide and Label will show.
     */
    fun setOnChangeToLabel(callback: CInputLabel.() -> String) = onChangeToLabelProperty.set(callback)

    init {
        setPrefSize(DEFAULT_WIDTH, DEFAULT_HEIGHT)

        label.prefWidthProperty().bind(prefWidthProperty())
        label.prefHeightProperty().bind(prefHeightProperty())
        field.prefWidthProperty().bind(prefWidthProperty())
        field.prefHeightProperty().bind(prefHeightProperty())
        field.textFormatterProperty().bind(textFormatterProperty)

        isEditingProperty.addListener(onNew {
            children.clear()
            children.add(if (it) field else label)
        })

        label.setOnMouseClicked {
            if (it.button != MouseButton.PRIMARY) return@setOnMouseClicked
            if (!it.isDoubleClick) return@setOnMouseClicked

            fieldText = onChangeToField(this)
            isEditing = true
        }
        field.setOnAction {
            labelText = onChangeToLabel(this)
            isEditing = false
        }

        children.add(label)
    }
}
